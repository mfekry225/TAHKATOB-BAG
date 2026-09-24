package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SpeechRepository
import com.example.data.local.AppDatabase
import com.example.data.local.ChildEntity
import com.example.data.local.SessionLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Content
import com.example.data.api.Part
import com.example.data.api.GenerationConfig
import com.example.data.api.ImageConfig
import com.example.data.api.RetrofitGeminiClient

sealed class ImageState {
    object Idle : ImageState()
    object Loading : ImageState()
    data class Success(val bitmap: Bitmap) : ImageState()
    data class Error(val message: String) : ImageState()
}

data class ExerciseImageMetadata(
    val id: Int,
    val title: String,
    val imagePath: String,
    val medicalDescription: String
)

class SpeechViewModel(application: Application) : AndroidViewModel(application) {
    private val speechDao = AppDatabase.getDatabase(application).speechDao()
    private val repository = SpeechRepository(speechDao)

    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    // Static and Medical Image Data from JSON
    private val _speechExerciseImages = MutableStateFlow<Map<Int, ExerciseImageMetadata>>(emptyMap())
    val speechExerciseImages: StateFlow<Map<Int, ExerciseImageMetadata>> = _speechExerciseImages.asStateFlow()

    private val _lispExerciseImages = MutableStateFlow<Map<Int, ExerciseImageMetadata>>(emptyMap())
    val lispExerciseImages: StateFlow<Map<Int, ExerciseImageMetadata>> = _lispExerciseImages.asStateFlow()

    init {
        loadExerciseImages()
    }

    private fun loadExerciseImages() {
        try {
            val jsonString = getApplication<Application>().assets.open("exercise_images.json")
                .bufferedReader().use { it.readText() }
            val root = org.json.JSONObject(jsonString)
            
            val speechArray = root.getJSONArray("speech_exercises")
            val speechMap = mutableMapOf<Int, ExerciseImageMetadata>()
            for (i in 0 until speechArray.length()) {
                val obj = speechArray.getJSONObject(i)
                val id = obj.getInt("id")
                speechMap[id] = ExerciseImageMetadata(
                    id = id,
                    title = obj.getString("title"),
                    imagePath = obj.getString("image_path"),
                    medicalDescription = obj.getString("medical_description")
                )
            }
            _speechExerciseImages.value = speechMap

            val lispArray = root.getJSONArray("lisp_exercises")
            val lispMap = mutableMapOf<Int, ExerciseImageMetadata>()
            for (i in 0 until lispArray.length()) {
                val obj = lispArray.getJSONObject(i)
                val id = obj.getInt("id")
                lispMap[id] = ExerciseImageMetadata(
                    id = id,
                    title = obj.getString("title"),
                    imagePath = obj.getString("image_path"),
                    medicalDescription = obj.getString("medical_description")
                )
            }
            _lispExerciseImages.value = lispMap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Persistent muted preference flow
    private val _isMuted = MutableStateFlow(prefs.getBoolean("is_muted", false))
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    fun toggleMute() {
        val newVal = !_isMuted.value
        _isMuted.value = newVal
        prefs.edit().putBoolean("is_muted", newVal).apply()
    }

    // All registered children
    val childrenList: StateFlow<List<ChildEntity>> = repository.allChildren
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected child in session
    private val _selectedChildId = MutableStateFlow<Int?>(null)
    val selectedChildId: StateFlow<Int?> = _selectedChildId.asStateFlow()

    // Active selected child details
    val activeChild: StateFlow<ChildEntity?> = _selectedChildId.flatMapLatest { id ->
        if (id != null) {
            repository.getChildById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Logs for the active child
    val activeChildLogs: StateFlow<List<SessionLogEntity>> = _selectedChildId.flatMapLatest { id ->
        if (id != null) {
            repository.getSessionLogsForChild(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Shared state for selected template in drawing/writing screen
    private val _selectedDrawingTemplateIndex = MutableStateFlow<Int>(0)
    val selectedDrawingTemplateIndex: StateFlow<Int> = _selectedDrawingTemplateIndex.asStateFlow()

    fun setSelectedDrawingTemplateIndex(index: Int) {
        _selectedDrawingTemplateIndex.value = index
    }

    // Action: Select active child
    fun selectChild(childId: Int?) {
        _selectedChildId.value = childId
    }

    // Action: Add new child profile
    fun addNewChild(name: String, age: Int, mentalAge: Int, notes: String, avatarColor: Int, country: String = "") {
        viewModelScope.launch {
            repository.insertChild(
                ChildEntity(
                    name = name,
                    age = age,
                    mentalAge = mentalAge,
                    notes = notes,
                    country = country,
                    avatarColor = avatarColor
                )
            )
        }
    }

    // Action: Delete kid
    fun deleteChild(child: ChildEntity) {
        viewModelScope.launch {
            if (_selectedChildId.value == child.id) {
                _selectedChildId.value = null
            }
            repository.deleteChild(child)
        }
    }

    // Action: Log speech therapy activity
    fun logSessionActivity(activityName: String, score: Int, notes: String) {
        val childId = _selectedChildId.value ?: return
        viewModelScope.launch {
            repository.insertSessionLog(
                SessionLogEntity(
                    childId = childId,
                    activityName = activityName,
                    score = score,
                    notes = notes
                )
            )
        }
    }

    // Action: Delete single session log
    fun deleteLog(log: SessionLogEntity) {
        viewModelScope.launch {
            repository.deleteSessionLog(log)
        }
    }

    // AI Imagen State Management
    private val _imageStates = MutableStateFlow<Map<Int, ImageState>>(emptyMap())
    val imageStates: StateFlow<Map<Int, ImageState>> = _imageStates.asStateFlow()

    private val _lispImageStates = MutableStateFlow<Map<Int, ImageState>>(emptyMap())
    val lispImageStates: StateFlow<Map<Int, ImageState>> = _lispImageStates.asStateFlow()

    private fun getEnglishExercisePrompt(mouthStyle: String, id: Int): String {
        val description = when (mouthStyle) {
            "puckerLips" -> "puckered lips kissing pose, lips pressed together and pushed forward"
            "smileTeeth" -> "a wide smile showing all teeth clearly, lips pulled back, cheerful mouth pose"
            "smileClosed" -> "a wide smile with lips completely closed, friendly mouth pose"
            "closeLips" -> "mouth completely closed, lips gently pressed together in a straight line, relaxed pose"
            "tongueOut" -> "open mouth with tongue sticking out, showing tongue extended outwards over the lower lip"
            "tongueUp" -> "open mouth with the tip of the tongue raised up, licking the upper lip or touching the upper palate"
            "tongueDown" -> "open mouth with the tongue pointing down towards the chin, showing the tongue extended downwards over the lower lip"
            "openWide" -> "mouth opened wide vertically in an open 'O' shape, showing teeth and deep mouth cavity"
            "jawRight" -> "mouth open with the lower jaw shifted completely to the right side, asymmetrical face pose"
            "jawLeft" -> "mouth open with the lower jaw shifted completely to the left side, asymmetrical face pose"
            "puffCheeks" -> "puffed cheeks filled with air, closed lips tightly sealed, rounded face"
            "blowCandle" -> "lips puckered blowing out air in a circular shape, breathing out stream of air pose"
            else -> "open mouth showing tongue and teeth"
        }
        
        val specificDetail = when (id) {
            6 -> "open mouth with tongue pointing sideways towards the corner of the mouth, lateral movement"
            9 -> "open mouth with tongue tip circling inside the lips, licking movement"
            10 -> "open mouth with tongue tip pressed against the roof of the mouth, palate touch"
            11 -> "open mouth with tongue tip touching the back molar teeth, inside view"
            18 -> "cheeks fully puffed like a balloon, lips tightly sealed, face view"
            else -> ""
        }
        
        val coreAction = if (specificDetail.isNotEmpty()) specificDetail else description
        
        return "A clean, educational flat vector illustration of a human mouth showing articulation: $coreAction. " +
               "Style: Minimalist flat graphic design, solid shapes, soft bold clean outlines, no 3D rendering, " +
               "no complex shading, no gradients, child-friendly anatomy. Background: Solid light cream beige background (#FFF7ED). " +
               "Centered, fully visible, complete."
    }

    fun generateImageForStep(stepId: Int, promptText: String, mouthStyle: String) {
        val currentState = _imageStates.value[stepId]
        if (currentState is ImageState.Loading || currentState is ImageState.Success) return

        _imageStates.value = _imageStates.value + (stepId to ImageState.Loading)

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _imageStates.value = _imageStates.value + (stepId to ImageState.Error("يرجى إعداد مفتاح API الخاص بك (GEMINI_API_KEY) في لوحة Secrets في AI Studio."))
                    return@launch
                }

                // Map to educational flat vector illustration prompt
                val fullPrompt = getEnglishExercisePrompt(mouthStyle, stepId)
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = fullPrompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = "1K"),
                        responseModalities = listOf("TEXT", "IMAGE")
                    )
                )

                val response = RetrofitGeminiClient.service.generateImage(apiKey, request)
                val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }
                val base64Data = part?.inlineData?.data

                if (base64Data != null) {
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        _imageStates.value = _imageStates.value + (stepId to ImageState.Success(bitmap))
                    } else {
                        _imageStates.value = _imageStates.value + (stepId to ImageState.Error("فشل تحويل الصورة المستلمة."))
                    }
                } else {
                    _imageStates.value = _imageStates.value + (stepId to ImageState.Error("لم يتم العثور على بيانات الصورة في استجابة خادم الذكاء الاصطناعي."))
                }
            } catch (e: Exception) {
                _imageStates.value = _imageStates.value + (stepId to ImageState.Error("خطأ في الاتصال: ${e.localizedMessage ?: "حدث خطأ غير متوقع"}"))
            }
        }
    }

    fun generateImageForLispStep(stepId: Int, promptText: String, mouthStyle: String) {
        val currentState = _lispImageStates.value[stepId]
        if (currentState is ImageState.Loading || currentState is ImageState.Success) return

        _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Loading)

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Error("يرجى إعداد مفتاح API الخاص بك (GEMINI_API_KEY) في لوحة Secrets في AI Studio."))
                    return@launch
                }

                // Map to educational flat vector illustration prompt
                val fullPrompt = getEnglishExercisePrompt(mouthStyle, stepId)
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = fullPrompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = "1K"),
                        responseModalities = listOf("TEXT", "IMAGE")
                    )
                )

                val response = RetrofitGeminiClient.service.generateImage(apiKey, request)
                val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }
                val base64Data = part?.inlineData?.data

                if (base64Data != null) {
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Success(bitmap))
                    } else {
                        _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Error("فشل تحويل الصورة المستلمة."))
                    }
                } else {
                    _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Error("لم يتم العثور على بيانات الصورة في استجابة خادم الذكاء الاصطناعي."))
                }
            } catch (e: Exception) {
                _lispImageStates.value = _lispImageStates.value + (stepId to ImageState.Error("خطأ في الاتصال: ${e.localizedMessage ?: "حدث خطأ غير متوقع"}"))
            }
        }
    }
}
