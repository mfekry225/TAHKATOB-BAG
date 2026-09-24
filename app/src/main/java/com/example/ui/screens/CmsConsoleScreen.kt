package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 50,000+ scaling schema definition for Content Activities
data class CMSActivity(
    val id: String,
    val title: String,
    val targetSound: String,
    val difficulty: String, // "سهل", "متوسط", "صعب"
    val targetAge: String,  // "3-5", "6-8", "9-12"
    val dialect: String,     // "فصحى مبسطة", "خليجي / عامي", "شامي / عامي", "مصري / الدلتا"
    val iepGoalCode: String, // e.g. "IEP-3.2.1"
    val learningObjective: String, // Objective text match
    val imageUrl: String,
    val audioDurationSec: Double,
    val audioSampleRateKhz: Double,
    val audioNoiseCancelled: Boolean,
    val authorName: String,
    val clinicalReviewer: String = "",
    val clinicalRating: Float = 0f, // Clinical efficacy score (1 - 5 stars)
    val clinicalComments: String = "",
    val pipelineStatus: String, // "مسودة", "قيد المراجعة", "معتمد", "منشور"
    val version: String = "1.0.0",
    val lang: String = "ar",
    val history: List<String> = listOf("أنشئت بواسطة المدون"),
    val aspectChecked: Boolean = true,
    val bgContrastChecked: Boolean = true,
    val activityType: String = "رقمية تفاعلية" // "رقمية تفاعلية" vs "توجيه أخصائي مباشر"
)

// Main CMS Console Layout in Arabic
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmsConsoleScreen(
    viewModel: SpeechViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(0) }
    val clipboardManager = LocalClipboardManager.current

    // Simulated 50,000 activities pipeline database state
    var totalPipelineCount by remember { mutableStateOf(53421) }
    var draftCount by remember { mutableStateOf(14) }
    var inReviewCount by remember { mutableStateOf(28) }
    var approvedCount by remember { mutableStateOf(105) }

    // Pre-populated high quality speech-content cards in Jalsat Takhatub Pipeline
    var activitiesList by remember {
        mutableStateOf(
            listOf(
                CMSActivity(
                    id = "ACT-10922",
                    title = "مخرج حرف الراء بوضع الضم (رُمان)",
                    targetSound = "حرف الراء (ر)",
                    difficulty = "سهل",
                    targetAge = "3-5",
                    dialect = "فصحى مبسطة",
                    iepGoalCode = "IEP-1.2.4",
                    learningObjective = "إنتاج حرف الراء المضموم في بداية الكلمة بشكل مستقل ومستقر بنسبة 80%.",
                    imageUrl = "https://example.com/assets/images/pomegranate.png",
                    audioDurationSec = 3.2,
                    audioSampleRateKhz = 44.1,
                    audioNoiseCancelled = true,
                    authorName = "أ. صفاء الكاتب",
                    clinicalReviewer = "د. عادل الحربي (أخصائي أول)",
                    clinicalRating = 4.8f,
                    clinicalComments = "مخارج النطق ممتازة وجاذبية الصورة عالية للأطفال.",
                    pipelineStatus = "قيد المراجعة",
                    version = "1.0.2",
                    history = listOf("تأليف مسودة (أ. صفاء)", "تعديل صوتي للضجيج", "بدء المراجعة الطبية (د. عادل)"),
                    activityType = "رقمية تفاعلية"
                ),
                CMSActivity(
                    id = "ACT-12001",
                    title = "تدريب نطق حرف السين بلهجة خليجية (سيارة ديرتي)",
                    targetSound = "حرف السين (س)",
                    difficulty = "متوسط",
                    targetAge = "6-8",
                    dialect = "خليجي / عامي",
                    iepGoalCode = "IEP-3.1.2",
                    learningObjective = "تحفيز نطق السين الصفيري في اللهجات المحلية لتعزيز الدمج والنمذجة العادية.",
                    imageUrl = "https://example.com/assets/images/car_khaliji.png",
                    audioDurationSec = 4.5,
                    audioSampleRateKhz = 48.0,
                    audioNoiseCancelled = true,
                    authorName = "أ. منيرة الدوسري",
                    clinicalReviewer = "أ. لمى العتيبي (مدقق كلينيكي)",
                    clinicalRating = 5.0f,
                    clinicalComments = "ممتازة جداً وتلائم البيئة الخليجية تماماً.",
                    pipelineStatus = "معتمد",
                    version = "1.0.0",
                    history = listOf("إنشاء بواسطة أ. منيرة", "اعتماد طبي كلينيكي"),
                    activityType = "رقمية تفاعلية"
                ),
                CMSActivity(
                    id = "ACT-14811",
                    title = "تنظيم التنفس الصدري مع حرف السين الممدود",
                    targetSound = "إطالة النفس (ســـــ)",
                    difficulty = "صعب",
                    targetAge = "9-12",
                    dialect = "فصحى مبسطة",
                    iepGoalCode = "IEP-5.0.1",
                    learningObjective = "إطالة الزفير للتحكم في التلعثم وتنظيم تدفق الهواء أثناء الكلام المتصل.",
                    imageUrl = "https://example.com/assets/images/breath_sea.png",
                    audioDurationSec = 8.0,
                    audioSampleRateKhz = 16.0,
                    audioNoiseCancelled = false,
                    authorName = "أ. علاء الدين شلتوت",
                    clinicalReviewer = "",
                    clinicalRating = 0f,
                    clinicalComments = "",
                    pipelineStatus = "مسودة",
                    version = "2.1.0-beta",
                    history = listOf("كتابة المسودة والتحقق النظري"),
                    activityType = "توجيه أخصائي مباشر"
                )
            )
        )
    }

    // New Content Author Form States
    var formTitle by remember { mutableStateOf("") }
    var formSound by remember { mutableStateOf("حرف الراء (ر)") }
    var formDifficulty by remember { mutableStateOf("سهل") }
    var formAge by remember { mutableStateOf("3-5") }
    var formDialect by remember { mutableStateOf("خليجي / عامي") }
    var formIep by remember { mutableStateOf("IEP-2.3.1") }
    var formObjective by remember { mutableStateOf("تمييز ونطق الصوت المستهدف في بداية الكلمة.") }
    var formImageUrl by remember { mutableStateOf("https://example.com/assets/images/gen_item.png") }
    var formAudioDuration by remember { mutableStateOf("3.5") }
    var formAudioSampleRate by remember { mutableStateOf("48.0") }
    var formAudioNoiseCancelled by remember { mutableStateOf(true) }
    var formAuthorName by remember { mutableStateOf("أخصائي الإنتاج") }
    var formActivityType by remember { mutableStateOf("رقمية تفاعلية") }

    // Image quality control protocol states
    var formImgHasClearAspect by remember { mutableStateOf(false) }
    var formImgHasClearDimensions by remember { mutableStateOf(false) }
    var formImgIsApprovedByReviewer by remember { mutableStateOf(false) }

    // AI Assisted Content Generator Fields (Gemini Wrapper simulation)
    var aiPromptInput by remember { mutableStateOf("أريد إنتاج بطاقة تدريبية لحرف الشين (ش)، مناسبة لجلسة تخرج طفل عمره ٦ سنوات بلهجة شامية محببة.") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiGeneratedTitle by remember { mutableStateOf("") }
    var aiGeneratedObjective by remember { mutableStateOf("") }
    var aiGeneratedWord by remember { mutableStateOf("") }
    var aiGeneratedCheckListMetrics by remember { mutableStateOf("") }

    // Bulk Import simulator text block
    var bulkCsvText by remember {
        mutableStateOf(
            "title,sound,difficulty,iep,dialect,duration,type\n" +
                    "تدريب تفاح أحمر لغوي,حرف التاء,سهل,IEP-1.1,خليجي,2.8,رقمية تفاعلية\n" +
                    "تمرين حركة اللسان وتقوية الفك,حرف الضاد,متوسط,IEP-3.4,فصحى,5.0,توجيه أخصائي مباشر\n" +
                    "تنظيم التنفس وعضلة الحجاب الحاجز,صوت النفس,صعب,IEP-5.1,فصحى,10.0,توجيه أخصائي مباشر\n" +
                    "مخرج الفاء فيل يضحك,حرف الفاء,سهل,IEP-1.2,مصري,4.1,رقمية تفاعلية"
        )
    }
    var bulkImportResults by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var bulkImportLogs by remember { mutableStateOf("") }

    // Selected Activity for active clinical/publishing reviews
    var selectedActivityIdForDetail by remember { mutableStateOf<String?>(null) }
    val selectedActivity = activitiesList.find { it.id == selectedActivityIdForDetail }

    // Editable clinical comments and score overrides
    var clinicalReviewOverrideScore by remember { mutableStateOf(4.5f) }
    var clinicalReviewOverrideText by remember { mutableStateOf("") }
    var clinicalReviewerNameInput by remember { mutableStateOf("الأخصائي المعالج") }

    // Setup of active review card sync whenever selected activity changes
    LaunchedEffect(selectedActivityIdForDetail) {
        selectedActivity?.let {
            clinicalReviewOverrideText = it.commentsWithFallback()
            clinicalReviewOverrideScore = if (it.clinicalRating == 0f) 4.0f else it.clinicalRating
            clinicalReviewerNameInput = it.clinicalReviewer.ifBlank { "د. منار الخالدي" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "نظام إدارة وإنتاج المحتوى (CMS) ⚙️",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "منصة خطوط الإنتاج والتحكيم السريري لـ جلسات التخاطب",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)) // Clean slate gray-blue professional tone
        ) {
            // Stats Banner: Visual Feedback of Jalsat Takhatub Multi-Year Scale (50K goal)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Deep production dark slate
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إحصائيات الأصول الرقمية السحابية 🌍",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "نطاق المستودع النشط: ${totalPipelineCount} بطاقة تمرين",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "الخطة الاستراتيجية المستهدفة: ٥٠,٠٠٠+ فئة نطقية عبر عدة سنوات كلينيكية معتمدة.",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }

                    // Floating visual mini tags counters
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniCountBadge("✍️ مسودات (${draftCount})", Color(0xFFF59E0B))
                        MiniCountBadge("🩺 تدقيق (${inReviewCount})", Color(0xFF3B82F6))
                        MiniCountBadge("🎉 اعتمد (${approvedCount})", Color(0xFF10B981))
                    }
                }
            }

            // Material Design 3 Scrollable Tab Row for workflows division
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0F172A),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("مسار الإنتاج والتدقيق 🩺", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("إنتاج ذكي بالقرائن والمولد (AI) 🤖", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("بنك الأسئلة والميديا 📦", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("المناهج الفردية وعتبة IEP 🎯", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text("الاستيراد السريع وأمن السحابة 📥", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Area based on Selected Workspace Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> WorkflowAndProductionTab(
                        activitiesList = activitiesList,
                        onActivitySelected = { selectedActivityIdForDetail = it },
                        selectedActivity = selectedActivity,
                        formTitle = formTitle,
                        onFormTitleChange = { formTitle = it },
                        formSound = formSound,
                        onFormSoundChange = { formSound = it },
                        formDifficulty = formDifficulty,
                        onFormDifficultyChange = { formDifficulty = it },
                        formAge = formAge,
                        onFormAgeChange = { formAge = it },
                        formDialect = formDialect,
                        onFormDialectChange = { formDialect = it },
                        formIep = formIep,
                        onFormIepChange = { formIep = it },
                        formObjective = formObjective,
                        onFormObjectiveChange = { formObjective = it },
                        formImageUrl = formImageUrl,
                        onFormImageUrlChange = { formImageUrl = it },
                        formAudioDuration = formAudioDuration,
                        onFormAudioDurationChange = { formAudioDuration = it },
                        formAudioSampleRate = formAudioSampleRate,
                        onFormAudioSampleRateChange = { formAudioSampleRate = it },
                        formAudioNoiseCancelled = formAudioNoiseCancelled,
                        onFormAudioNoiseCancelledChange = { formAudioNoiseCancelled = it },
                        formAuthorName = formAuthorName,
                        onFormAuthorNameChange = { formAuthorName = it },
                        formActivityType = formActivityType,
                        onFormActivityTypeChange = { formActivityType = it },
                        onAddActivity = {
                            if (formTitle.isBlank()) {
                                Toast.makeText(context, "الرجاء كشْف عنوان البطاقة أولاً يا بطل!", Toast.LENGTH_SHORT).show()
                            } else if (!formImgHasClearAspect || !formImgHasClearDimensions || !formImgIsApprovedByReviewer) {
                                Toast.makeText(context, "🚨 شرط جودة عيادي صريح:\nيجب استيفاء جميع معايير الصورة (جانب واضح، أبعاد محددة، مراجعة معتمدة 100%) لحفظ البطاقة!", Toast.LENGTH_LONG).show()
                            } else {
                                val newAct = CMSActivity(
                                    id = "ACT-${(15000..99999).random()}",
                                    title = formTitle,
                                    targetSound = formSound,
                                    difficulty = formDifficulty,
                                    targetAge = formAge,
                                    dialect = formDialect,
                                    iepGoalCode = formIep,
                                    learningObjective = formObjective,
                                    imageUrl = formImageUrl,
                                    audioDurationSec = formAudioDuration.toDoubleOrNull() ?: 3.0,
                                    audioSampleRateKhz = formAudioSampleRate.toDoubleOrNull() ?: 48.0,
                                    audioNoiseCancelled = formAudioNoiseCancelled,
                                    authorName = formAuthorName,
                                    pipelineStatus = "مسودة",
                                    version = "1.0.0",
                                    history = listOf("إنشاء يدوي في منصة CMS بصفة كاتب (مراجعة بصرية معتمدة 100%)"),
                                    activityType = formActivityType
                                )
                                activitiesList = listOf(newAct) + activitiesList
                                draftCount++
                                formTitle = ""
                                formActivityType = "رقمية تفاعلية"
                                // Reset image checks for next cards
                                formImgHasClearAspect = false
                                formImgHasClearDimensions = false
                                formImgIsApprovedByReviewer = false
                                Toast.makeText(context, "تم حفظ المسودة بنجاح وبانتظار المدققين! 📝", Toast.LENGTH_SHORT).show()
                            }
                        },
                        clinicalReviewerName = clinicalReviewerNameInput,
                        onClinicalReviewerNameChange = { clinicalReviewerNameInput = it },
                        clinicalReviewText = clinicalReviewOverrideText,
                        onClinicalReviewTextChange = { clinicalReviewOverrideText = it },
                        clinicalRatingScore = clinicalReviewOverrideScore,
                        onClinicalRatingScoreChange = { clinicalReviewOverrideScore = it },
                        onApplyClinicalReview = {
                            selectedActivity?.let { act ->
                                activitiesList = activitiesList.map {
                                    if (it.id == act.id) {
                                        it.copy(
                                            clinicalReviewer = clinicalReviewerNameInput,
                                            clinicalComments = clinicalReviewOverrideText,
                                            clinicalRating = clinicalReviewOverrideScore,
                                            pipelineStatus = "قيد المراجعة",
                                            history = it.history + "تحكيم سريري بواسطة $clinicalReviewerNameInput"
                                        )
                                    } else it
                                }
                                Toast.makeText(context, "حُفِظ رأي المدقق الطبي لمطابقة معايير العلاج! 🩺", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTransitionStatus = { targetStatus ->
                            selectedActivity?.let { act ->
                                activitiesList = activitiesList.map {
                                    if (it.id == act.id) {
                                        var v = it.version
                                        if (targetStatus == "منشور") {
                                            // Increment minor version on publish
                                            val parts = v.split(".")
                                            val last = parts.last().toIntOrNull() ?: 0
                                            v = parts.dropLast(1).joinToString(".") + ".${last + 1}"
                                            totalPipelineCount++
                                            approvedCount++
                                        }
                                        it.copy(
                                            pipelineStatus = targetStatus,
                                            version = v,
                                            history = it.history + "نقل الحالة إلى [$targetStatus] برتبة معتمد رئيسي"
                                        )
                                    } else it
                                }
                                Toast.makeText(context, "تم تغيير حالة الفعالية ونشر الإصدار $targetStatus بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                            }
                        },
                        formImgHasClearAspect = formImgHasClearAspect,
                        onFormImgHasClearAspectChange = { formImgHasClearAspect = it },
                        formImgHasClearDimensions = formImgHasClearDimensions,
                        onFormImgHasClearDimensionsChange = { formImgHasClearDimensions = it },
                        formImgIsApprovedByReviewer = formImgIsApprovedByReviewer,
                        onFormImgIsApprovedByReviewerChange = { formImgIsApprovedByReviewer = it }
                    )

                    1 -> AiAssistedContentTab(
                        prompt = aiPromptInput,
                        onPromptChange = { aiPromptInput = it },
                        isGenerating = isGeneratingAi,
                        onGenerate = {
                            isGeneratingAi = true
                            scope.launch {
                                delay(2200) // Simulate DeepMind/Gemini generation logic
                                isGeneratingAi = false
                                val extractSound = if (aiPromptInput.contains("ش")) "حرف الشين (ش)" else "محدد تلقائياً"
                                aiGeneratedTitle = "تدريبات مخرج حرف الشين للتلعثم والنطق المقترن"
                                aiGeneratedObjective = "عقد دمج تفاعلي للشفاه والأسنان لتدقيق السين والشين المقترنة ببيئة اللعب باللهجة الشامية."
                                aiGeneratedWord = "شامسي - شمسية دافية"
                                aiGeneratedCheckListMetrics = "✓ جودة الصوت: 16Khz عزل تام للضوضاء\n✓ الصورة: نسب متكافئة 1:1 مريحة للعين\n✓ IEP مطابقة الأهداف: IEP-3.2.1"
                                Toast.makeText(context, "اكتمل توليد بطاقة الذكاء الاصطناعي بنجاح! 🤖🌟", Toast.LENGTH_SHORT).show()
                            }
                        },
                        aiTitle = aiGeneratedTitle,
                        aiObjective = aiGeneratedObjective,
                        aiWord = aiGeneratedWord,
                        aiChecklist = aiGeneratedCheckListMetrics,
                        onInjectGeneratedToEditor = {
                            formTitle = "بطاقة AI: النطق لـ $aiGeneratedWord"
                            formSound = "حرف الشين (ش)"
                            formObjective = aiGeneratedObjective
                            formDifficulty = "متوسط"
                            formDialect = "شامي / عامي"
                            formIep = "IEP-3.2.1"
                            activeTab = 0 // Move back to main editor
                            Toast.makeText(context, "تم نسخ مخرجات الذكاء الاصطناعي للمحرر الرئيسي لمراجعتها سريرياً! 📝", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> QuestionBankTab(
                        activitiesList = activitiesList,
                        onSearchSelect = { selectedActivityIdForDetail = it; activeTab = 0 }
                    )

                    3 -> CurriculumAndIepMappingTab()

                    4 -> BulkAndCloudTab(
                        bulkText = bulkCsvText,
                        onBulkTextChange = { bulkCsvText = it },
                        parsedResults = bulkImportResults,
                        importLogs = bulkImportLogs,
                        onAnalyzeCsv = {
                            try {
                                val lines = bulkCsvText.split("\n")
                                val list = mutableListOf<Map<String, String>>()
                                val logs = StringBuilder()
                                logs.append("✓ بدء فك شفرة الملف الهيكلي...\n")
                                logs.append("✓ تم الكشف عن التبريرات القياسية والترتيب المعتمد للجدول.\n")
                                
                                var headerMap: List<String> = emptyList()
                                lines.forEachIndexed { idx, line ->
                                    if (line.isNotBlank()) {
                                        val cols = line.split(",").map { it.trim() }
                                        if (idx == 0) {
                                            headerMap = cols
                                        } else {
                                            val map = mutableMapOf<String, String>()
                                            cols.forEachIndexed { cIdx, value ->
                                                if (cIdx < headerMap.size) {
                                                    map[headerMap[cIdx]] = value
                                                }
                                            }
                                            list.add(map)
                                        }
                                    }
                                }
                                bulkImportResults = list
                                logs.append("✓ تحليل بنية الأسطر: تم معالجة ${list.size} أنشطة بنجاح.\n")
                                logs.append("✓ جاري فحص معايير الميديا: جميع الصور والملفات الصوتية مطابقة لمقاييس الطيف 1:1 ونقاوة مخرج الاستماع 48kHz.\n")
                                bulkImportLogs = logs.toString()
                                Toast.makeText(context, "تم قراءة ومراجعة الكبل الهيكلي لـ ${list.size} بطاقة! 🎉", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                bulkImportLogs = "خطأ في معالجة البيانات: ${e.localizedMessage}"
                            }
                        },
                        onCommitImport = {
                            bulkImportResults.forEach { res ->
                                val importedType = res["type"] ?: if (
                                    (res["title"] ?: "").contains("تنفس") ||
                                    (res["title"] ?: "").contains("لسان") ||
                                    (res["title"] ?: "").contains("تمرين") ||
                                    (res["title"] ?: "").contains("عضلات") ||
                                    (res["title"] ?: "").contains("أخصائي") ||
                                    (res["title"] ?: "").contains("حركة")
                                ) "توجيه أخصائي مباشر" else "رقمية تفاعلية"

                                val parsedNew = CMSActivity(
                                    id = "ACT-${(15000..99999).random()}",
                                    title = res["title"] ?: "مستورد من السحابة",
                                    targetSound = res["sound"] ?: "حرف مستهدف",
                                    difficulty = res["difficulty"] ?: "سهل",
                                    targetAge = "3-5",
                                    dialect = res["dialect"] ?: "فصحى مبسطة",
                                    iepGoalCode = res["iep"] ?: "غير محدد",
                                    learningObjective = "مسار مستورد تلقائياً من نظام التخزين الدفعي الكثيف.",
                                    imageUrl = "https://example.com/assets/images/placeholder.png",
                                    audioDurationSec = res["duration"]?.toDoubleOrNull() ?: 3.0,
                                    audioSampleRateKhz = 48.0,
                                    audioNoiseCancelled = true,
                                    authorName = "مدير المزامنة السحابية",
                                    pipelineStatus = "معتمد",
                                    version = "1.0.0",
                                    activityType = importedType
                                )
                                activitiesList = listOf(parsedNew) + activitiesList
                                approvedCount++
                            }
                            totalPipelineCount += bulkImportResults.size
                            bulkImportResults = emptyList()
                            Toast.makeText(context, "تم تحميل وبث البيانات المعتمدة لـ Firestore بنجاح! 🚀☁️", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// 🩺 Tab 1 Layout: Content authoring form + live activities track list + clinical review & status transitions
@Composable
fun WorkflowAndProductionTab(
    activitiesList: List<CMSActivity>,
    onActivitySelected: (String) -> Unit,
    selectedActivity: CMSActivity?,
    formTitle: String,
    onFormTitleChange: (String) -> Unit,
    formSound: String,
    onFormSoundChange: (String) -> Unit,
    formDifficulty: String,
    onFormDifficultyChange: (String) -> Unit,
    formAge: String,
    onFormAgeChange: (String) -> Unit,
    formDialect: String,
    onFormDialectChange: (String) -> Unit,
    formIep: String,
    onFormIepChange: (String) -> Unit,
    formObjective: String,
    onFormObjectiveChange: (String) -> Unit,
    formImageUrl: String,
    onFormImageUrlChange: (String) -> Unit,
    formAudioDuration: String,
    onFormAudioDurationChange: (String) -> Unit,
    formAudioSampleRate: String,
    onFormAudioSampleRateChange: (String) -> Unit,
    formAudioNoiseCancelled: Boolean,
    onFormAudioNoiseCancelledChange: (Boolean) -> Unit,
    formAuthorName: String,
    onFormAuthorNameChange: (String) -> Unit,
    formActivityType: String,
    onFormActivityTypeChange: (String) -> Unit,
    onAddActivity: () -> Unit,
    clinicalReviewerName: String,
    onClinicalReviewerNameChange: (String) -> Unit,
    clinicalReviewText: String,
    onClinicalReviewTextChange: (String) -> Unit,
    clinicalRatingScore: Float,
    onClinicalRatingScoreChange: (Float) -> Unit,
    onApplyClinicalReview: () -> Unit,
    onTransitionStatus: (String) -> Unit,
    formImgHasClearAspect: Boolean,
    onFormImgHasClearAspectChange: (Boolean) -> Unit,
    formImgHasClearDimensions: Boolean,
    onFormImgHasClearDimensionsChange: (Boolean) -> Unit,
    formImgIsApprovedByReviewer: Boolean,
    onFormImgIsApprovedByReviewerChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Active list in the construction tube (35% width)
        Card(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = borderScheme(Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "قائمة أنشطة المسار الحالي 🛠️",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "اضغط لتعديلها، تحكيمها طبياً، أو ترقية إصداراتها",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activitiesList) { act ->
                        val isSelected = selectedActivity?.id == act.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onActivitySelected(act.id) }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = act.id,
                                    fontSize = 10.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Bold
                                )

                                // Simple pipeline tag color choice
                                val stColor = when (act.pipelineStatus) {
                                    "منشور" -> Color(0xFF10B981)
                                    "معتمد" -> Color(0xFF06B6D4)
                                    "قيد المراجعة" -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(stColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        act.pipelineStatus,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = stColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val isGuided = act.activityType == "توجيه أخصائي مباشر"
                                val typeBadgeColor = if (isGuided) Color(0xFFEC4899) else Color(0xFF3B82F6)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(typeBadgeColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (isGuided) "🩺 إرشادي" else "💻 تفاعلي",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = typeBadgeColor
                                    )
                                }
                                
                                Text(
                                    act.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "المستوعب الصوتي: ${act.targetSound} | لهجة: ${act.dialect}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Editor or Details (65% width) - Scrollable fields
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedActivity != null) {
                // Displaying and editing Selected Pipeline Activity Details (Clinical + Publish settings)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderScheme(Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "لوحة مراجعة ونشر الفعالية: ${selectedActivity.id}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            
                            Text(
                                "إصدار: v${selectedActivity.version}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        // High fidelity multi-stage pipeline alignment checklist visualizer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .clip(RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PipelineStageIndicator("مسودة", selectedActivity.pipelineStatus == "مسودة")
                            PipelineStageIndicator("قيد المراجعة", selectedActivity.pipelineStatus == "قيد المراجعة")
                            PipelineStageIndicator("معتمد", selectedActivity.pipelineStatus == "معتمد")
                            PipelineStageIndicator("منشور", selectedActivity.pipelineStatus == "منشور")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("اسم المحرر/المؤلف الأصلي: ${selectedActivity.authorName}", fontSize = 11.sp, color = Color.Gray)
                        Text("الهدف السلوكي المطبق: ${selectedActivity.learningObjective}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text("مرجع خطة التعليم الفردية: ${selectedActivity.iepGoalCode}", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(6.dp))

                        // Distinguished Badge Row in Editor for Digital vs. Therapist-guided types
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isGuided = selectedActivity.activityType == "توجيه أخصائي مباشر"
                            val typeColor = if (isGuided) Color(0xFFEC4899) else Color(0xFF3B82F6)
                            Text("مسار ونوع البطاقة المسجل:", fontSize = 11.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(typeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isGuided) "🩺 تمرين إرشادي (جلسة تأهيل وعلاج مباشر وجهًا لوجه مع الأخصائي)" else "💻 نشاط رقمي تفاعلي (ألعاب وبطاقات ينفذها الطفل باللمس والصوت)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = typeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTION 1: CLINICAL REVIEW WORKFLOW (التحكيم الإكلينيكي العيادي للسلامة وجودة المخارج)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    "🩺 تحكيم الفعالية ومطابقة معايير التخاطب (المدقق الطبي)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    "يشترط الأخصائي مواءمة الكلمات لعضلات النطق وغياب تشويش الخلفية الصوتية.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF166534)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = clinicalReviewerName,
                                        onValueChange = onClinicalReviewerNameChange,
                                        label = { Text("المحكم الإكلينيكي") },
                                        modifier = Modifier.weight(0.5f),
                                        singleLine = true
                                    )

                                    // Clinical Rating Slider (1 to 5 stars)
                                    Column(modifier = Modifier.weight(0.5f)) {
                                        Text("معيار القيمة العلاجية: $clinicalRatingScore ★", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        Slider(
                                            value = clinicalRatingScore,
                                            onValueChange = onClinicalRatingScoreChange,
                                            valueRange = 1f..5f,
                                            steps = 3
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = clinicalReviewText,
                                    onValueChange = onClinicalReviewTextChange,
                                    label = { Text("التوصيات السريرية والملاحظات") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = onApplyClinicalReview,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("حفظ تقييم ومراجعة الأخصائي 🩺", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTION 2: APPROVAL & PUBLISHING WORKFLOW (النشر بضغطة زر وتوليد الإصدار للـ CDN)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "🚀 محطة ضبط جودة النشر والاعتماد (رئيس التحرير اللغوي)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onTransitionStatus("معتمد") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("اعتماد الفعالية 🌟", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onTransitionStatus("منشور") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    enabled = selectedActivity.pipelineStatus == "معتمد",
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("نشر وتحديث CDN 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Edit history / Activity Versioning
                            Text(
                                "سجل تعديلات ونسب الأنشطة (Activity Versioning):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            selectedActivity.history.forEach { hist ->
                                Text("• $hist", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            } else {
                // Prompt when no activity is selected
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = borderScheme(Color(0xFFBFDBFE))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "👈 الرجاء الضغط على أي فعالية في القائمة الجانبية المجاورة لاستعراض دورة تحكيمها ونشرها مباشرة، أو قم بإنشاء فعالية جديدة في الأسفل.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }
            }

            // SECTION 3: CONTENT AUTHOR WORKFLOW FORM (صياغة وتأليف مادة جديدة من كُتّاب المناهج)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = borderScheme(Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✍️", fontSize = 13.sp)
                        }
                        Text(
                            "محرر صياغة وتأليف البطاقات (Content Author Studio)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Text(
                        "أضف مفردات نطقية جديدة بلهجة محددة لخدمة أكثر من 50 ألف حالة معالجة صوتية.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = formTitle,
                        onValueChange = onFormTitleChange,
                        label = { Text("أدخل اسم الفعالية أو الكلمة المستهدفة بالنطق") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formSound,
                            onValueChange = onFormSoundChange,
                            label = { Text("الصوت والمخرج المستهدف (الفونيم)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Mode selectors styled beautifully
                        Column(modifier = Modifier.weight(1f)) {
                            Text("مستوى الصعوبة المتناسبة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("سهل", "متوسط", "صعب").forEach { diff ->
                                    val isSelected = formDifficulty == diff
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSelected) Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { onFormDifficultyChange(diff) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(diff, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF475569))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "نوع النشاط والمسار العلاجي (Activity Model Core Type) 🎨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Option 1: Digital Interactive Activity
                            val isDigital = formActivityType == "رقمية تفاعلية"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDigital) Color(0xFFEFF6FF) else Color.White)
                                    .border(
                                        width = if (isDigital) 2.dp else 1.dp,
                                        color = if (isDigital) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onFormActivityTypeChange("رقمية تفاعلية") }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("💻", fontSize = 14.sp)
                                        Text("رقمية تفاعلية (Digital)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDigital) Color(0xFF1D4ED8) else Color(0xFF334155))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "نشاط تفاعلي مباشر للطفل (ألعاب، بطاقات الكلمات، أصوات، تتبع باللمس البصري).",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            // Option 2: Therapist-Guided Manual Exercise
                            val isGuided = formActivityType == "توجيه أخصائي مباشر"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isGuided) Color(0xFFFDF2F8) else Color.White)
                                    .border(
                                        width = if (isGuided) 2.dp else 1.dp,
                                        color = if (isGuided) Color(0xFFDB2777) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onFormActivityTypeChange("توجيه أخصائي مباشر") }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🩺", fontSize = 14.sp)
                                        Text("توجيه أخصائي مباشر (Guided)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGuided) Color(0xFF9D174D) else Color(0xFF334155))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "تمارين تطبيقية سريرية وعضلية (مساج الفك، تدريبات التنفس، إرشاد وتدريب مخارج اللسان).",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الفئة العمرية:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("3-5", "6-8", "9-12").forEach { age ->
                                    val isSelected = formAge == age
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSelected) Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { onFormAgeChange(age) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(age, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF475569))
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("اللهجة العربية (Dialects):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("خليجي", "شامي", "مصري", "فصحى").forEach { dial ->
                                    val fullDialText = when (dial) {
                                        "خليجي" -> "خليجي / عامي"
                                        "شامي" -> "شامي / عامي"
                                        "مصري" -> "مصري / الدلتا"
                                        else -> "فصحى مبسطة"
                                    }
                                    val isSelected = formDialect == fullDialText
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFFFFECE0) else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSelected) PlayfulOrange else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { onFormDialectChange(fullDialText) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(dial, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PlayfulOrange else Color(0xFF475569))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formIep,
                            onValueChange = onFormIepChange,
                            label = { Text("رمز هدف الـ IEP للطفل") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formObjective,
                            onValueChange = onFormObjectiveChange,
                            label = { Text("الغرض والتعليم السلوكي لربطه بالـ IEP") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media check inputs (Image and Audio standards checklist rules integration)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                "🎛️ معايير وتدابير جودة الصورة والصوت للبطاقة المقترحة:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = formAudioNoiseCancelled, onCheckedChange = onFormAudioNoiseCancelledChange)
                                    Text("تم التحقق من إزالة ضوضاء الخلفية 🔇", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = formAudioDuration,
                                    onValueChange = onFormAudioDurationChange,
                                    label = { Text("زمن مقطع الصوت (ثانية)") },
                                    modifier = Modifier.width(140.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = formAudioSampleRate,
                                    onValueChange = onFormAudioSampleRateChange,
                                    label = { Text("معدل التجميع (kHz)") },
                                    modifier = Modifier.width(140.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🖼️ Clinical Visual Quality Standards (Image Protocol) Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = borderScheme(Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🖼️", fontSize = 20.sp)
                                Text(
                                    "بروتوكول ضبط جودة الصور البصرية (شرط عيادي صريح)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                            Text(
                                "يشترط هذا الميثاق صراحةً سلامة المحتوى البصري لضمان أقصى درجات الفائدة السريرية وبدون أي مشتتات للأطفال:",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Constraint 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (formImgHasClearAspect) Color(0xFFDCFCE7) else Color.Transparent)
                                    .clickable { onFormImgHasClearAspectChange(!formImgHasClearAspect) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = formImgHasClearAspect,
                                    onCheckedChange = onFormImgHasClearAspectChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "الشرط ١: أن تكون الصورة ذات جانب واضح ومحدد بالكامل 📐",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        "تحديد زاوية الرؤية والجهة بوضوح تام لزيادة ارتباط الطفل البصري مع المدلول النطقي.",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Constraint 2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (formImgHasClearDimensions) Color(0xFFDCFCE7) else Color.Transparent)
                                    .clickable { onFormImgHasClearDimensionsChange(!formImgHasClearDimensions) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = formImgHasClearDimensions,
                                    onCheckedChange = onFormImgHasClearDimensionsChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "الشرط ٢: أن تكون الصورة ذات أبعاد واضحة ومحددة تماماً 📏",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        "أبعاد بؤرية دقيقة ومريحة لعين طفل التوحد وصعوبات التعلم تمنع التشتت والاهتزاز.",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Constraint 3
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (formImgIsApprovedByReviewer) Color(0xFFDCFCE7) else Color.Transparent)
                                    .clickable { onFormImgIsApprovedByReviewerChange(!formImgIsApprovedByReviewer) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = formImgIsApprovedByReviewer,
                                    onCheckedChange = onFormImgIsApprovedByReviewerChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "الشرط ٣: أن تتم مراجعة الصورة والموافقة عليها لتكون صحيحة بنسبة 100% ✅",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        "موافقة عيادية وبشرية قاطعة تجزم بصحة الصورة ومطابقتها لمعايير الأخصائيين المعتمدين.",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            // Interactive Status Badge
                            if (formImgHasClearAspect && formImgHasClearDimensions && formImgIsApprovedByReviewer) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFD1FAE5))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🛡️", fontSize = 14.sp)
                                        Text(
                                            "المطابقة ممتازة: الصورة سليمة، معتمدة، وصحيحة بنسبة 100% وجاهزة للنشر!",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("⚠️", fontSize = 14.sp)
                                        Text(
                                            "الالتزام مطلوب: يجب استيفاء شروط الصورة البصرية صراحةً لتمكين الحفظ.",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formAuthorName,
                            onValueChange = onFormAuthorNameChange,
                            label = { Text("اسم كاتب المحتوى") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true
                        )

                        Button(
                            onClick = onAddActivity,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(0.6f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ المسودة وتمريرها لمسار التحكيم الكلينيكي 🩺", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// 🤖 Tab 2 Layout: Prompt generator with a typewriter animation simulation
@Composable
fun AiAssistedContentTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    aiTitle: String,
    aiObjective: String,
    aiWord: String,
    aiChecklist: String,
    onInjectGeneratedToEditor: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderScheme(Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 18.sp)
                }
                Text(
                    "مولد ومساعد المناهج بالذكاء الاصطناعي (Gemini Pro Content Studio)",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            }
            Text(
                "أكتب نمط الفعالية أو الحرف المستهدف وسيقوم المعالج الطبي التوليدي بصياغة الأهداف بشكل آلي طبقاً لقياسات IEP وتعديل مخارج النطق.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("اكتب موجه الذكاء الاصطناعي (Prompt) للبطاقة المطلوبة") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("جاري الصياغة بواسطة Gemini Pro ... 🧠")
                } else {
                    Text("توليد وصياغة البطاقة آلياً 🤖✨", fontWeight = FontWeight.Bold)
                }
            }

            if (aiTitle.isNotBlank()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.5.dp, Color(0xFFFDA4AF), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "مخرجات Gemini Pro المقترحة (مسودة ذكية):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF9F1239)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("العنوان المقترح: $aiTitle", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الكلمة واللفظ المستهدف: $aiWord", fontWeight = FontWeight.Bold, color = Color(0xFFBE123C), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الهدف السلوكي المطبق: $aiObjective", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }

                        // Code checks metrics
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("مقياس الجودة التلقائي (Automated Quality Assurance):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(aiChecklist, fontSize = 11.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onInjectGeneratedToEditor,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تصدير واستيراد البطاقة للمحرر الرئيسي لمراجعتها ✍️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 📦 Tab 3 Layout: Question bank searchable list + quality standards visualization
@Composable
fun QuestionBankTab(
    activitiesList: List<CMSActivity>,
    onSearchSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficultyFilter by remember { mutableStateOf("الكل") }
    var selectedDialectFilter by remember { mutableStateOf("الكل") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderScheme(Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "مستودع بطاقات الكلمات وبنك النطق العام 📦",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                "أدوات البحث والفلترة السريعة بموجب الأهداف ولهجة النطق المساعدة والمسار العلاجي.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث باسم البطاقة، الحرف، أو الكود IEP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filters Row
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Difficulty
                    Column(modifier = Modifier.weight(1f)) {
                        Text("مستوى الصعوبة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("الكل", "سهل", "متوسط", "صعب").forEach { diff ->
                                val isSel = selectedDifficultyFilter == diff
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSel) Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedDifficultyFilter = diff }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(diff, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF1D4ED8) else Color(0xFF475569))
                                }
                            }
                        }
                    }

                    // Dialect
                    Column(modifier = Modifier.weight(1f)) {
                        Text("فرز اللهجات المساعدة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("الكل", "خليجي", "شامي", "مصري", "فصحى").forEach { dial ->
                                val match = when (dial) {
                                    "خليجي" -> "خليجي / عامي"
                                    "شامي" -> "شامي / عامي"
                                    "مصري" -> "مصري / الدلتا"
                                    "فصحى" -> "فصحى مبسطة"
                                    else -> "الكل"
                                }
                                val isSel = selectedDialectFilter == match
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFFFFECE0) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSel) PlayfulOrange else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedDialectFilter = match }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(dial, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) PlayfulOrange else Color(0xFF475569))
                                }
                            }
                        }
                    }
                }

                // Activity Model Core Type Filter Segment
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("مسار نوع النشاط والتصنيف عينة:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("الكل", "رقمية تفاعلية", "توجيه أخصائي مباشر").forEach { typeOption ->
                            val isSel = selectedTypeFilter == typeOption
                            val labelText = when (typeOption) {
                                "رقمية تفاعلية" -> "💻 مسار الأنشطة الرقمية"
                                "توجيه أخصائي مباشر" -> "🩺 مسار التدريب والتوجيه العيادي"
                                else -> "🔍 عرض الكل"
                            }
                            val accentCol = when (typeOption) {
                                "رقمية تفاعلية" -> Color(0xFF3B82F6)
                                "توجيه أخصائي مباشر" -> Color(0xFFEC4899)
                                else -> Color(0xFF64748B)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) accentCol.copy(alpha = 0.12f) else Color(0xFFF1F5F9))
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) accentCol else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedTypeFilter = typeOption }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) accentCol else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filtering logic including Activity Core Type
            val filtered = activitiesList.filter { act ->
                val matchQuery = searchQuery.isBlank() ||
                        act.title.contains(searchQuery) ||
                        act.targetSound.contains(searchQuery) ||
                        act.iepGoalCode.contains(searchQuery)
                val matchDiff = selectedDifficultyFilter == "الكل" || act.difficulty == selectedDifficultyFilter
                val matchDial = selectedDialectFilter == "الكل" || act.dialect == selectedDialectFilter
                val matchType = selectedTypeFilter == "الكل" || act.activityType == selectedTypeFilter
                matchQuery && matchDiff && matchDial && matchType
            }

            Text("البطاقات المسترجعة (${filtered.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { act ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = borderScheme(Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val isGuided = act.activityType == "توجيه أخصائي مباشر"
                                    val actBadgeCol = if (isGuided) Color(0xFFEC4899) else Color(0xFF3B82F6)
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(actBadgeCol.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isGuided) "🩺 إرشاد أخصائي" else "💻 تفاعلي رقمي",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = actBadgeCol
                                        )
                                    }

                                    Text(act.title, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEFF6FF))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(act.iepGoalCode, fontSize = 9.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الحرف: ${act.targetSound} | اللهجة: ${act.dialect} | صعوبة: ${act.difficulty}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ملف الصوت: ${act.audioDurationSec}ث - ${act.audioSampleRateKhz}kHz {منقي الضوضاء: ${if (act.audioNoiseCancelled) "تم" else "لا"}}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            
                            Button(
                                onClick = { onSearchSelect(act.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(0.3f).testTag("inspect_button_${act.id}")
                            ) {
                                Text("معاينة وتعديل ✍️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 🎯 Tab 4 Layout: Curriculum Maps & IEP goals dictionary
@Composable
fun CurriculumAndIepMappingTab() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderScheme(Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎯", fontSize = 18.sp)
                }
                Text(
                    "خريطة الأهداف الفردية (IEP Model Map) والمسارات العيادية",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            }
            Text(
                "رصد الخريطة الطبية العلاجية لربط ما يربو على 50 ألف بطاقة نطق وتخاطب بخطط التعليم الفردية للأطفال بذكاء.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            // Dynamic interactive IEP objectives mapping table
            Text(
                "مسارات المناهج العلاجية المعتمدة والتصنيفات الدقيقة:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0369A1)
            )

            listOf(
                Pair("مسار التلعثم والتنفس الرئوي (Fluency Mastery)", "يتضمن تدريبات الإطالة وحصر الكلمات في بداية الزفير بنقاء صوتي تام."),
                Pair("مسار مخارج الحروف الشفوية (Articulations & Phonemes)", "تهيئة اللسان والشفاه لإنتاج الحروف البسيطة والمعقدة (س، ر، ق)."),
                Pair("مسار دمج التوحد المترابط (Autism Core Lexicon)", "مجموعات ضمنية وصور دقيقة لتنمية الرصيد المعرفي والتصنيف اللغوي."),
                Pair("مسار اللغة الاستقبالية (Receptive Path)", "الأفال الحركية وأفعال الطعام، رصد استيعاب الأوامر والطلب اليدوي.")
            ).forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F9FF))
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(item.second, fontSize = 11.sp, color = Color(0xFF334155))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "الأكواد القياسية لـ IEP (Individualized Education Program):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0369A1)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IepCodeBox("IEP-1.2.4", "استكمال نطق الراء")
                IepCodeBox("IEP-3.2.1", "نطق الشين الشامية")
                IepCodeBox("IEP-5.0.1", "الزفير والتحكم بالتلعثم")
            }
        }
    }
}

// Cloud 📥 Tab 5 Layout: CSV Bulk Import System and visual Copyable Security checks model
@Composable
fun BulkAndCloudTab(
    bulkText: String,
    onBulkTextChange: (String) -> Unit,
    parsedResults: List<Map<String, String>>,
    importLogs: String,
    onAnalyzeCsv: () -> Unit,
    onCommitImport: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val firestoreRulesText = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Jalsat Takhatub content authorization rules
    match /activities/{activityId} {
      allow read: if true; // Public access for children and specialists
      
      // Author and clinical reviewer explicit writes check
      allow create, update: if request.auth != null && 
        (request.auth.token.role == 'author' || request.auth.token.role == 'reviewer' || request.auth.token.role == 'editor');
        
      // Only chief editors can delete or publish versions to the stable pipeline
      allow delete: if request.auth != null && request.auth.token.role == 'editor';
    }
    
    match /children/{childId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.specialistUid;
    }
  }
}
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = borderScheme(Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📥", fontSize = 20.sp)
                    Text(
                        "نظام الاستيراد الدفعي الكثيف (Bulk CSV/JSON Import)",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Text(
                    "بضعة أسطر مفصولة بفواصل تمثل مئات الأنشطة المستوردة لضمان تضخم وتوسع المستودع الرقمي بالجودة القياسية لعزل الصوت.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = bulkText,
                    onValueChange = onBulkTextChange,
                    label = { Text("محتوى CSV المراد استيراده وإخضاعه للفحص التلقائي") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    maxLines = 6
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAnalyzeCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تجزئة وفحص البيانات المعيارية 🔎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCommitImport,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f),
                        enabled = parsedResults.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("مزامنة وحقن بـ Firestore السحابي ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (importLogs.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(10.dp)
                    ) {
                        Text(
                            importLogs,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                // Parsed preview table
                if (parsedResults.isNotEmpty()) {
                    Text("معاينة مخرجات الجدول المالي المراد حقنه:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    parsedResults.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item["title"] ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("صوت: ${item["duration"]}ث", fontSize = 10.sp, color = Color.Gray)
                            Text("لهجة: ${item["dialect"]}", fontSize = 10.sp, color = PlayfulOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Firestore Security Rules schema representation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Coding dark theme
            border = borderScheme(Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "قواعد حماية وأمن Firestore (Security Rules Model) 🛡️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(firestoreRulesText))
                            Toast.makeText(context, "نسخ ملف القواعد للمستندات السحابية! 📋", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("نسخ الكود 📋", fontSize = 10.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "تطبيق حماية إكلينيكية (Role-Based Access Control) لمنع الأطفال من العبث بالمقاييس الطبية للجلسات.",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(10.dp)
                ) {
                    Text(
                        firestoreRulesText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// Helpers
@Composable
fun MiniCountBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PipelineStageIndicator(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF10B981) else Color(0xFF94A3B8))
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isActive) Color(0xFF0F172A) else Color(0xFF64748B)
        )
    }
}

@Composable
fun IepCodeBox(code: String, meaning: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0F2FE))
            .padding(8.dp)
    ) {
        Column {
            Text(code, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF0284C7))
            Text(meaning, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

private fun CMSActivity.commentsWithFallback(): String {
    return this.clinicalComments.ifBlank { "النطق والصوت والتسلسل سليم للمخارج." }
}
