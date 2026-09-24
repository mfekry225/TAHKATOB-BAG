package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SpellingPuzzle(val word: String, val emoji: String, val letterPool: List<String>, val correctOrder: List<String>)
data class QuantityPuzzle(val sideAEmojis: String, val sideACount: Int, val sideBEmojis: String, val sideBCount: Int, val question: String)
data class ShadowPuzzle(val original: String, val originalEmoji: String, val shadowLabel: String, val options: List<String>, val correctOption: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartGamesScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeSubGame by remember { mutableStateOf(0) } // 0: Spell Words, 1: Larger/Smaller, 2: Shadow matching
    val isMuted by viewModel.isMuted.collectAsState()

    // 1. Spelling Puzzles list
    val spellingPuzzles = remember {
        listOf(
            SpellingPuzzle("موز", "🍌", listOf("ز", "م", "و"), listOf("م", "و", "ز")),
            SpellingPuzzle("أسد", "🦁", listOf("د", "أ", "س"), listOf("أ", "س", "د")),
            SpellingPuzzle("بحر", "🌊", listOf("ح", "ب", "ر"), listOf("ب", "ح", "ر")),
            SpellingPuzzle("برتقال", "🍊", listOf("ل", "ب", "ر", "ت", "ق", "ا"), listOf("ب", "ر", "ت", "ق", "ا", "ل")),
            SpellingPuzzle("عين", "👁️", listOf("ن", "ع", "ي"), listOf("ع", "ي", "ن"))
        )
    }
    var currentSpellingIndex by remember { mutableStateOf(0) }
    val spellingPuzzle = spellingPuzzles[currentSpellingIndex]
    val spelledLetters = remember { mutableStateListOf<String>() }

    // 2. Quantity Puzzles
    val quantityPuzzles = remember {
        listOf(
            QuantityPuzzle("🍎🍎🍎", 3, "🍎", 1, "أي الجانبين يحتوي على كمية أكبـر؟"),
            QuantityPuzzle("🎈", 1, "🎈🎈🎈🎈🎈", 5, "أي الجانبين يحتوي على كمية أصغـر؟"),
            QuantityPuzzle("🚗🚗🚗🚗", 4, "🚗🚗", 2, "أي الجانبين يحتوي على كمية أكبـر؟"),
            QuantityPuzzle("⭐", 1, "⭐⭐⭐", 3, "أي الجانبين يحتوي على كمية أصغـر؟")
        )
    }
    var currentQuantityIndex by remember { mutableStateOf(0) }
    val quantityPuzzle = quantityPuzzles[currentQuantityIndex]

    // 3. Shadow matching
    val shadowPuzzles = remember {
        listOf(
            ShadowPuzzle("دب", "🐻", "اختر ظل 'الدب':", listOf("🦁", "🐻", "🐰", "🐘"), "🐻"),
            ShadowPuzzle("سيارة", "🚗", "اختر ظل 'السيارة':", listOf("✈️", "🚢", "🚗", "🚲"), "🚗"),
            ShadowPuzzle("تفاحة", "🍎", "اختر ظل 'التفاحة':", listOf("🍌", "🍇", "🥕", "🍎"), "🍎"),
            ShadowPuzzle("سمكة", "🐟", "اختر ظل 'السمكة':", listOf("🐙", "🐬", "🐟", "🦀"), "🐟")
        )
    }
    var currentShadowIndex by remember { mutableStateOf(0) }
    val shadowPuzzle = shadowPuzzles[currentShadowIndex]

    // Celebration
    var showSuccessBanner by remember { mutableStateOf(false) }

    // Keep track of correct option in Quantity game to show checkmark state
    var selectedQuantitySide by remember { mutableStateOf<String?>(null) } // "A" or "B"
    var isQuantitySelectionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // State for shadow matching selection feedback
    var selectedShadowOption by remember { mutableStateOf<String?>(null) }
    var isShadowSelectionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // Reset helpers when sub-game transitions
    LaunchedEffect(activeSubGame) {
        showSuccessBanner = false
        spelledLetters.clear()
        selectedQuantitySide = null
        isQuantitySelectionCorrect = null
        selectedShadowOption = null
        isShadowSelectionCorrect = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الألعاب والأنشطة الذكية 🧩", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF001D34)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF1A1C1E))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.5.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.5f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Horizontal game tab switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeSubGame = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubGame == 0) SoftBluePrimary else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "تهجي الكلمات 🔠",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubGame == 0) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = { activeSubGame = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubGame == 1) PlayfulOrange else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "أكبر وأصغر 🔢",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubGame == 1) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = { activeSubGame = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubGame == 2) MintGreen else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "الشكل والظل 👥",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubGame == 2) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = { activeSubGame = 3 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubGame == 3) SunnyYellow else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "قبل وبعد ⏱️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSubGame == 3) Color.White else Color.Black
                    )
                }
            }

            // Success animation Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MintGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SunnyYellow, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "عمل ممتاز! إجابة صحيحة ذكية جداً! 👏🌟",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Game Contents
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWide = maxWidth >= 600.dp

                    when (activeSubGame) {
                        // ================= 0: Spelling Words game =================
                        0 -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("spelling_card_game"),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                if (isWide) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        // Left: Giant Focus Box
                                        Card(
                                            modifier = Modifier
                                                .weight(0.42f)
                                                .fillMaxHeight(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    "الكلمة المستهدفة 🎯",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftBluePrimary
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(140.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(spellingPuzzle.emoji, fontSize = 95.sp)
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(
                                                    spellingPuzzle.word,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0F4C81),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Right: Interactive Game Mechanics
                                        Column(
                                            modifier = Modifier
                                                .weight(0.58f)
                                                .fillMaxHeight()
                                                .padding(horizontal = 8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            SpellingMechanicsArea(
                                                spellingPuzzle = spellingPuzzle,
                                                spelledLetters = spelledLetters,
                                                letterPool = spellingPuzzle.letterPool,
                                                showSuccessBanner = showSuccessBanner,
                                                onLetterTap = { char ->
                                                    if (!spelledLetters.contains(char)) {
                                                        spelledLetters.add(char)
                                                        onSpeak(char)
                                                        if (spelledLetters.size == spellingPuzzle.correctOrder.size) {
                                                            if (spelledLetters == spellingPuzzle.correctOrder) {
                                                                showSuccessBanner = true
                                                                onSpeak("ممتاز! تهجئة صحيحة لكلمة ${spellingPuzzle.word}")
                                                                viewModel.logSessionActivity("تهجي الكلمات", 5, "تهجأ كلمة ${spellingPuzzle.word} بنجاح!")
                                                                
                                                                // AUTO ADvance
                                                                coroutineScope.launch {
                                                                    delay(2200)
                                                                    if (currentSpellingIndex < spellingPuzzles.size - 1) {
                                                                        currentSpellingIndex++
                                                                    } else {
                                                                        currentSpellingIndex = 0
                                                                    }
                                                                    spelledLetters.clear()
                                                                    showSuccessBanner = false
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "الترتيب غير صحيح، حاول ثانية!", Toast.LENGTH_SHORT).show()
                                                                spelledLetters.clear()
                                                            }
                                                        }
                                                    }
                                                },
                                                onReset = {
                                                    spelledLetters.clear()
                                                    showSuccessBanner = false
                                                },
                                                onNext = {
                                                    if (currentSpellingIndex < spellingPuzzles.size - 1) {
                                                        currentSpellingIndex++
                                                    } else {
                                                        currentSpellingIndex = 0
                                                    }
                                                    spelledLetters.clear()
                                                    showSuccessBanner = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // Portrait Compact Layout (scrollable)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE1F5FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(spellingPuzzle.emoji, fontSize = 95.sp)
                                        }

                                        Text(
                                            spellingPuzzle.word,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SoftBluePrimary
                                        )

                                        SpellingMechanicsArea(
                                            spellingPuzzle = spellingPuzzle,
                                            spelledLetters = spelledLetters,
                                            letterPool = spellingPuzzle.letterPool,
                                            showSuccessBanner = showSuccessBanner,
                                            onLetterTap = { char ->
                                                if (!spelledLetters.contains(char)) {
                                                    spelledLetters.add(char)
                                                    onSpeak(char)
                                                    if (spelledLetters.size == spellingPuzzle.correctOrder.size) {
                                                        if (spelledLetters == spellingPuzzle.correctOrder) {
                                                            showSuccessBanner = true
                                                            onSpeak("ممتاز! تهجئة صحيحة لكلمة ${spellingPuzzle.word}")
                                                            viewModel.logSessionActivity("تهجي الكلمات", 5, "تهجأ كلمة ${spellingPuzzle.word} بنجاح!")
                                                            
                                                            // AUTO ADvance
                                                            coroutineScope.launch {
                                                                delay(2200)
                                                                if (currentSpellingIndex < spellingPuzzles.size - 1) {
                                                                    currentSpellingIndex++
                                                                } else {
                                                                    currentSpellingIndex = 0
                                                                }
                                                                spelledLetters.clear()
                                                                showSuccessBanner = false
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "الترتيب غير صحيح، حاول ثانية!", Toast.LENGTH_SHORT).show()
                                                            spelledLetters.clear()
                                                        }
                                                    }
                                                }
                                            },
                                            onReset = {
                                                spelledLetters.clear()
                                                showSuccessBanner = false
                                            },
                                            onNext = {
                                                if (currentSpellingIndex < spellingPuzzles.size - 1) {
                                                    currentSpellingIndex++
                                                } else {
                                                    currentSpellingIndex = 0
                                                }
                                                spelledLetters.clear()
                                                showSuccessBanner = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ================= 1: Largest / Smaller comparison =================
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                if (isWide) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        // Left Column: Giant visual scoreboard of items (Prominent Emojis + Giant readable numbers!)
                                        Card(
                                            modifier = Modifier
                                                .weight(0.48f)
                                                .fillMaxHeight(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(14.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "المقارنة البصرية الكبيرة 📊",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PlayfulOrange
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                        .padding(vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // Group A (Right)
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .background(Color.White)
                                                            .border(1.5.dp, Color(0xFFFFF59D), RoundedCornerShape(18.dp))
                                                            .padding(8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            modifier = Modifier.fillMaxHeight()
                                                        ) {
                                                            // Giant badge representing the number
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(54.dp)
                                                                    .clip(CircleShape)
                                                                    .background(SoftBluePrimary)
                                                                    .border(3.dp, Color.White, CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text("${quantityPuzzle.sideACount}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                            }

                                                            Text(
                                                                quantityPuzzle.sideAEmojis,
                                                                fontSize = 52.sp,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 4.dp).weight(1f, fill = false)
                                                            )

                                                            Text(
                                                                "اليمنى (العدد: ${quantityPuzzle.sideACount})",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.DarkGray
                                                            )
                                                        }
                                                    }

                                                    // Group B (Left)
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .background(Color.White)
                                                            .border(1.5.dp, Color(0xFFFFF59D), RoundedCornerShape(18.dp))
                                                            .padding(8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            modifier = Modifier.fillMaxHeight()
                                                        ) {
                                                            // Giant badge representing the number B
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(54.dp)
                                                                    .clip(CircleShape)
                                                                    .background(PlayfulOrange)
                                                                    .border(3.dp, Color.White, CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text("${quantityPuzzle.sideBCount}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                            }

                                                            Text(
                                                                quantityPuzzle.sideBEmojis,
                                                                fontSize = 52.sp,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 4.dp).weight(1f, fill = false)
                                                            )

                                                            Text(
                                                                "اليسرى (العدد: ${quantityPuzzle.sideBCount})",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.DarkGray
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Right Column: Interaction buttons
                                        Column(
                                            modifier = Modifier
                                                .weight(0.52f)
                                                .fillMaxHeight(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            QuantityChoicesArea(
                                                quantityPuzzle = quantityPuzzle,
                                                showSuccessBanner = showSuccessBanner,
                                                onChoiceSelect = { isA ->
                                                    val side = if (isA) "A" else "B"
                                                    selectedQuantitySide = side
                                                    val isCorrect = if (quantityPuzzle.question.contains("أكبر")) {
                                                        if (isA) quantityPuzzle.sideACount > quantityPuzzle.sideBCount else quantityPuzzle.sideBCount > quantityPuzzle.sideACount
                                                    } else {
                                                        if (isA) quantityPuzzle.sideACount < quantityPuzzle.sideBCount else quantityPuzzle.sideBCount < quantityPuzzle.sideACount
                                                    }
                                                    isQuantitySelectionCorrect = isCorrect

                                                    if (isCorrect) {
                                                        showSuccessBanner = true
                                                        onSpeak("أحسنت! إجابة صحيحة")
                                                        viewModel.logSessionActivity("أكبر وأصغر", 5, "حل تمرين مقارنة الكميات الحسابية بنجاح")
                                                        
                                                        // AUTO ADVANCE
                                                        coroutineScope.launch {
                                                            delay(2200)
                                                            if (currentQuantityIndex < quantityPuzzles.size - 1) {
                                                                currentQuantityIndex++
                                                            } else {
                                                                currentQuantityIndex = 0
                                                            }
                                                            selectedQuantitySide = null
                                                            isQuantitySelectionCorrect = null
                                                            showSuccessBanner = false
                                                        }
                                                    } else {
                                                        onSpeak("حاول مرة أخرى يا بطل")
                                                        Toast.makeText(context, "الجواب غير صحيح، حاول مرة أخرى! ❤️", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onNext = {
                                                    if (currentQuantityIndex < quantityPuzzles.size - 1) {
                                                        currentQuantityIndex++
                                                    } else {
                                                        currentQuantityIndex = 0
                                                    }
                                                    selectedQuantitySide = null
                                                    isQuantitySelectionCorrect = null
                                                    showSuccessBanner = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // Portrait Compact Mode
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            quantityPuzzle.question,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PlayfulOrange,
                                            textAlign = TextAlign.Center
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Compact side A
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                colors = CardDefaults.cardColors(containerColor = SoftCreamBg),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(quantityPuzzle.sideAEmojis, fontSize = 45.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("اليمنى (العدد: ${quantityPuzzle.sideACount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // Compact side B
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                colors = CardDefaults.cardColors(containerColor = SoftCreamBg),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(quantityPuzzle.sideBEmojis, fontSize = 45.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("اليسرى (العدد: ${quantityPuzzle.sideBCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        QuantityChoicesArea(
                                            quantityPuzzle = quantityPuzzle,
                                            showSuccessBanner = showSuccessBanner,
                                            onChoiceSelect = { isA ->
                                                val side = if (isA) "A" else "B"
                                                selectedQuantitySide = side
                                                val isCorrect = if (quantityPuzzle.question.contains("أكبر")) {
                                                    if (isA) quantityPuzzle.sideACount > quantityPuzzle.sideBCount else quantityPuzzle.sideBCount > quantityPuzzle.sideACount
                                                } else {
                                                    if (isA) quantityPuzzle.sideACount < quantityPuzzle.sideBCount else quantityPuzzle.sideBCount < quantityPuzzle.sideACount
                                                }
                                                isQuantitySelectionCorrect = isCorrect

                                                if (isCorrect) {
                                                    showSuccessBanner = true
                                                    onSpeak("أحسنت! إجابة صحيحة")
                                                    viewModel.logSessionActivity("أكبر وأصغر", 5, "حل تمرين مقارنة الكميات الحسابية بنجاح")
                                                    
                                                    // AUTO ADVANCE
                                                    coroutineScope.launch {
                                                        delay(2200)
                                                        if (currentQuantityIndex < quantityPuzzles.size - 1) {
                                                            currentQuantityIndex++
                                                        } else {
                                                            currentQuantityIndex = 0
                                                        }
                                                        selectedQuantitySide = null
                                                        isQuantitySelectionCorrect = null
                                                        showSuccessBanner = false
                                                    }
                                                } else {
                                                    onSpeak("حاول مرة أخرى يا بطل")
                                                    Toast.makeText(context, "الجواب غير صحيح، حاول مرة أخرى! ❤️", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onNext = {
                                                if (currentQuantityIndex < quantityPuzzles.size - 1) {
                                                    currentQuantityIndex++
                                                } else {
                                                    currentQuantityIndex = 0
                                                }
                                                selectedQuantitySide = null
                                                isQuantitySelectionCorrect = null
                                                showSuccessBanner = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ================= 2: Shadow matching =================
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                if (isWide) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        // Left Column: Clue panel showing original prominent shape
                                        Card(
                                            modifier = Modifier
                                                .weight(0.40f)
                                                .fillMaxHeight(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    "ابقَ منتبهاً للشكل 🎯",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MintGreen
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(140.dp)
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .background(Color.White)
                                                        .border(2.dp, MintGreen.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(shadowPuzzle.originalEmoji, fontSize = 95.sp)
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    shadowPuzzle.original,
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = MintGreen,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Right Column: Interaction Game Mechanics (2x2 grid for prominent options!)
                                        Column(
                                            modifier = Modifier
                                                .weight(0.60f)
                                                .fillMaxHeight(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            ShadowMatchArea(
                                                shadowPuzzle = shadowPuzzle,
                                                showSuccessBanner = showSuccessBanner,
                                                selectedShadowOption = selectedShadowOption,
                                                isShadowSelectionCorrect = isShadowSelectionCorrect,
                                                onOptionSelect = { opt ->
                                                    selectedShadowOption = opt
                                                    val isCorrect = opt == shadowPuzzle.correctOption
                                                    isShadowSelectionCorrect = isCorrect

                                                    if (isCorrect) {
                                                        showSuccessBanner = true
                                                        onSpeak("أحسنت هذا هو الظل والرمز المتطابق!")
                                                        viewModel.logSessionActivity("الشكل والظل", 5, "ربط بنجاح شكل ${shadowPuzzle.original} بظله")
                                                        
                                                        // AUTO ADVANCE
                                                        coroutineScope.launch {
                                                            delay(2200)
                                                            if (currentShadowIndex < shadowPuzzles.size - 1) {
                                                                currentShadowIndex++
                                                            } else {
                                                                currentShadowIndex = 0
                                                            }
                                                            selectedShadowOption = null
                                                            isShadowSelectionCorrect = null
                                                            showSuccessBanner = false
                                                        }
                                                    } else {
                                                        onSpeak("الرمز غير مطابق حاول ثانية")
                                                        Toast.makeText(context, "الرمز غير مطابق للظل، حاول ثانية! ❤️", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onNext = {
                                                    if (currentShadowIndex < shadowPuzzles.size - 1) {
                                                        currentShadowIndex++
                                                    } else {
                                                        currentShadowIndex = 0
                                                    }
                                                    selectedShadowOption = null
                                                    isShadowSelectionCorrect = null
                                                    showSuccessBanner = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // Portrait Compact Mode
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            shadowPuzzle.shadowLabel,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MintGreen
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFFE8F5E9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(shadowPuzzle.originalEmoji, fontSize = 95.sp)
                                        }

                                        ShadowMatchArea(
                                            shadowPuzzle = shadowPuzzle,
                                            showSuccessBanner = showSuccessBanner,
                                            selectedShadowOption = selectedShadowOption,
                                            isShadowSelectionCorrect = isShadowSelectionCorrect,
                                            onOptionSelect = { opt ->
                                                selectedShadowOption = opt
                                                val isCorrect = opt == shadowPuzzle.correctOption
                                                isShadowSelectionCorrect = isCorrect

                                                if (isCorrect) {
                                                    showSuccessBanner = true
                                                    onSpeak("أحسنت هذا هو الظل والرمز المتطابق!")
                                                    viewModel.logSessionActivity("الشكل والظل", 5, "ربط بنجاح شكل ${shadowPuzzle.original} بظله")
                                                    
                                                    // AUTO ADVANCE
                                                    coroutineScope.launch {
                                                        delay(2200)
                                                        if (currentShadowIndex < shadowPuzzles.size - 1) {
                                                            currentShadowIndex++
                                                        } else {
                                                            currentShadowIndex = 0
                                                        }
                                                        selectedShadowOption = null
                                                        isShadowSelectionCorrect = null
                                                        showSuccessBanner = false
                                                    }
                                                } else {
                                                    onSpeak("الرمز غير مطابق حاول ثانية")
                                                    Toast.makeText(context, "الرمز غير مطابق للظل، حاول ثانية! ❤️", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onNext = {
                                                if (currentShadowIndex < shadowPuzzles.size - 1) {
                                                    currentShadowIndex++
                                                } else {
                                                    currentShadowIndex = 0
                                                }
                                                selectedShadowOption = null
                                                isShadowSelectionCorrect = null
                                                showSuccessBanner = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ================= 3: Before and After game =================
                        3 -> {
                            BeforeAfterMatchArea(
                                viewModel = viewModel,
                                onSpeak = onSpeak,
                                showSuccessBanner = showSuccessBanner,
                                onSuccessTrigger = {
                                    coroutineScope.launch {
                                        showSuccessBanner = true
                                        delay(2200)
                                        showSuccessBanner = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub components to structure each mechanics area neatly:
@Composable
fun SpellingMechanicsArea(
    spellingPuzzle: SpellingPuzzle,
    spelledLetters: List<String>,
    letterPool: List<String>,
    showSuccessBanner: Boolean,
    onLetterTap: (String) -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val parentMaxWidth = maxWidth
        val parentMaxHeight = maxHeight
        val baseSize = (parentMaxWidth.value + parentMaxHeight.value) / 100
        val titleFontSize = (baseSize * 1.8f).coerceIn(14f, 22f).sp
        val letterFontSize = (baseSize * 3.5f).coerceIn(22f, 36f).sp
        val ghostFontSize = (baseSize * 3.0f).coerceIn(18f, 30f).sp
        val buttonFontSize = (baseSize * 1.5f).coerceIn(13f, 20f).sp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "رتّب الحروف بالترتيب الصحيح لتهجئة الكلمة:",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // Slots area
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                spellingPuzzle.correctOrder.forEachIndexed { idx, char ->
                    val enteredChar = spelledLetters.getOrNull(idx) ?: ""
                    val expectedChar = spellingPuzzle.correctOrder[idx]
                    Box(
                        modifier = Modifier
                            .size(if (parentMaxHeight < 360.dp) 44.dp else 56.dp)
                            .weight(1.0f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (enteredChar.isNotEmpty()) SoftBluePrimary.copy(alpha = 0.15f)
                                else Color.LightGray.copy(alpha = 0.15f)
                            )
                            .border(1.5.dp, if (enteredChar.isNotEmpty()) SoftBluePrimary else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enteredChar.isNotEmpty()) {
                            Text(
                                enteredChar,
                                fontSize = letterFontSize,
                                fontWeight = FontWeight.Bold,
                                color = SoftBluePrimary
                            )
                        } else {
                            // Ghost hint
                            Text(
                                expectedChar,
                                fontSize = ghostFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray.copy(alpha = 0.18f)
                            )
                        }
                    }
                }
            }

            // Tap characters pool
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                letterPool.forEach { char ->
                    val isUsed = spelledLetters.contains(char)
                    Box(
                        modifier = Modifier
                            .size(if (parentMaxHeight < 360.dp) 48.dp else 60.dp)
                            .clip(CircleShape)
                            .background(if (isUsed) Color.LightGray.copy(alpha = 0.5f) else PlayfulOrange)
                            .clickable(enabled = !isUsed) {
                                onLetterTap(char)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            char,
                            fontSize = letterFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(contentColor = PlayfulOrange)
                ) {
                    Text("إعادة المحاولة 🔄", fontSize = buttonFontSize, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("الكلمة التالية ➜", color = Color.White, fontWeight = FontWeight.Bold, fontSize = buttonFontSize)
                }
            }
        }
    }
}

@Composable
fun QuantityChoicesArea(
    quantityPuzzle: QuantityPuzzle,
    showSuccessBanner: Boolean,
    onChoiceSelect: (Boolean) -> Unit, // passes true for Side A (Right), false for Side B (Left)
    onNext: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val parentMaxWidth = maxWidth
        val parentMaxHeight = maxHeight
        val baseSize = (parentMaxWidth.value + parentMaxHeight.value) / 100
        val questionFontSize = (baseSize * 2.2f).coerceIn(18f, 28f).sp
        val instructionFontSize = (baseSize * 1.5f).coerceIn(13f, 20f).sp
        val sideTitleFontSize = (baseSize * 2.0f).coerceIn(16f, 26f).sp
        val countLabelFontSize = (baseSize * 1.5f).coerceIn(12f, 18f).sp
        val badgeFontSize = (baseSize * 1.5f).coerceIn(12f, 18f).sp
        val emojiFontSize = (baseSize * 6.0f).coerceIn(38f, 54f).sp
        val buttonFontSize = (baseSize * 1.6f).coerceIn(14f, 22f).sp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                quantityPuzzle.question,
                fontSize = questionFontSize,
                fontWeight = FontWeight.Black,
                color = PlayfulOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                "اضغط على المستطيل الصحيح للمقارنة: 👇",
                fontSize = instructionFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // Big touch-friendly rectangle cards (prominent choice buttons) with side squares
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Option 1: Right Side (اليمنى)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onChoiceSelect(true) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCreamBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = borderScheme(PlayfulOrange.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Right part of Row: Text description
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "الجهة اليمنى 👉",
                                fontSize = sideTitleFontSize,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "العدد الإجمالي: ${quantityPuzzle.sideACount}",
                                fontSize = countLabelFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        // Left part of Row: The gorgeous large picture square
                        Box(
                            modifier = Modifier
                                .size(if (parentMaxHeight < 360.dp) 64.dp else 80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(2.5.dp, PlayfulOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Display emojis inside beautifully
                            Text(
                                text = quantityPuzzle.sideAEmojis,
                                fontSize = if (quantityPuzzle.sideACount > 3) emojiFontSize * 0.8f else emojiFontSize,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                            
                            // Giant overlay badge showing the number securely so it is extremely clear!
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(if (parentMaxHeight < 360.dp) 24.dp else 30.dp)
                                    .clip(CircleShape)
                                    .background(PlayfulOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${quantityPuzzle.sideACount}",
                                    color = Color.White,
                                    fontSize = badgeFontSize,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Option 2: Left Side (اليسرى)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onChoiceSelect(false) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCreamBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = borderScheme(SoftBluePrimary.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Right part of Row: Text description
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "الجهة اليسرى 👈",
                                fontSize = sideTitleFontSize,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "العدد الإجمالي: ${quantityPuzzle.sideBCount}",
                                fontSize = countLabelFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        // Left part of Row: The gorgeous large picture square
                        Box(
                            modifier = Modifier
                                .size(if (parentMaxHeight < 360.dp) 64.dp else 80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(2.5.dp, SoftBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Display emojis inside beautifully
                            Text(
                                text = quantityPuzzle.sideBEmojis,
                                fontSize = if (quantityPuzzle.sideBCount > 3) emojiFontSize * 0.8f else emojiFontSize,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                            
                            // Giant overlay badge showing the number securely so it is extremely clear!
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(if (parentMaxHeight < 360.dp) 24.dp else 30.dp)
                                    .clip(CircleShape)
                                    .background(SoftBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${quantityPuzzle.sideBCount}",
                                    color = Color.White,
                                    fontSize = badgeFontSize,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text("التالي ➜", color = Color.White, fontWeight = FontWeight.Bold, fontSize = buttonFontSize)
            }
        }
    }
}

@Composable
fun ShadowMatchArea(
    shadowPuzzle: ShadowPuzzle,
    showSuccessBanner: Boolean,
    selectedShadowOption: String?,
    isShadowSelectionCorrect: Boolean?,
    onOptionSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val parentMaxWidth = maxWidth
        val baseSize = (parentMaxWidth.value + maxHeight.value) / 100
        val titleFontSize = (baseSize * 2.2f).coerceIn(18f, 28f).sp
        val instructionFontSize = (baseSize * 1.5f).coerceIn(13f, 20f).sp
        val cardFontSize = (baseSize * 8.5f).coerceIn(52f, 85f).sp
        val buttonFontSize = (baseSize * 1.6f).coerceIn(14f, 22f).sp

        val lazyListState = rememberLazyListState()
        val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                shadowPuzzle.shadowLabel,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = MintGreen,
                textAlign = TextAlign.Center
            )

            Text(
                "ابحث عن الشكل المطابق واسحب لليمين واليسار للتصفح! 🔍✨",
                fontSize = instructionFontSize,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Smooth Horizontal Scrolling Row with Snap Snapping
            LazyRow(
                state = lazyListState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(shadowPuzzle.options) { opt ->
                    val isSelectedThis = selectedShadowOption == opt
                    val cardBorder = when {
                        isSelectedThis && isShadowSelectionCorrect == true -> Color(0xFF4CAF50)
                        isSelectedThis && isShadowSelectionCorrect == false -> Color(0xFFF44336)
                        else -> Color.Transparent
                    }

                    Card(
                        modifier = Modifier
                            .width(parentMaxWidth * 0.42f) // Proportional responsive width (equivalent of vw)
                            .fillMaxHeight(0.95f)
                            .clickable { onOptionSelect(opt) }
                            .border(if (isSelectedThis) 3.dp else 0.dp, cardBorder, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoftCreamBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(opt, fontSize = cardFontSize)
                        }
                    }
                }
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "الشكل التالي ➜",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = buttonFontSize
                )
            }
        }
    }
}

// Border creator helper
@Composable
fun borderScheme(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.5.dp, color)
}

// ==========================================
// 3: Before and After Game (قبل وبعد) Components
// ==========================================

data class ChronologicalStep(
    val id: Int, // 1, 2, 3
    val emoji: String,
    val label: String,
    val descriptionText: String
)

data class ChronologicalPuzzle(
    val title: String,
    val categoryName: String,
    val steps: List<ChronologicalStep>
)

@Composable
fun BeforeAfterMatchArea(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    showSuccessBanner: Boolean,
    onSuccessTrigger: () -> Unit
) {
    val context = LocalContext.current
    
    val puzzles = remember {
        listOf(
            ChronologicalPuzzle(
                title = "غسيل اليدين بالصابون 🧼",
                categoryName = "النظافة الشخصية",
                steps = listOf(
                    ChronologicalStep(1, "🧴", "وضع الصابون", "نضع الصابون اللطيف على اليدين"),
                    ChronologicalStep(2, "🚰", "فرك بالماء", "نفرك اليدين بالماء جيداً لإزالة الجراثيم"),
                    ChronologicalStep(3, "🧼", "تجفيف اليدين", "نجفف بالمنشفة لتصبح أيدينا نظيفة وجافة")
                )
            ),
            ChronologicalPuzzle(
                title = "مراحل نمو النبتة 🌱",
                categoryName = "العلوم والطبيعة",
                steps = listOf(
                    ChronologicalStep(1, "🕳️", "بذرة في التربة", "نضع البذرة الصغيرة في التربة الدافئة"),
                    ChronologicalStep(2, "🌱", "سقي البرعم", "تسقى بالماء وتكبر لتصبح برعماً صغيراً"),
                    ChronologicalStep(3, "🌸", "تفتح الزهرة", "تتفتح الزهرة الملونة وتسعد بضوء الشمس")
                )
            ),
            ChronologicalPuzzle(
                title = "الاستيقاظ والنشاط ⏰",
                categoryName = "روتين اليوم",
                steps = listOf(
                    ChronologicalStep(1, "⏰", "رنين المنبه", "يدق المنبه في الصباح الباكر للاستيقاظ"),
                    ChronologicalStep(2, "🚿", "غسل الوجه والأسنان", "نغسل الوجه والأسنان بالفرشاة والماء"),
                    ChronologicalStep(3, "🍳", "فطور لذيذ وصحي", "نتناول فطورنا المغذي لنبدأ يومنا بنشاط")
                )
            ),
            ChronologicalPuzzle(
                title = "شطيرة الجبن اللذيذة 🥪",
                categoryName = "الطهي والغذاء",
                steps = listOf(
                    ChronologicalStep(1, "🍞", "تجهيز الخبز", "نحضر قطعة خبز طازجة ونظيفة"),
                    ChronologicalStep(2, "🧀", "إضافة الجبن والطماطم", "نضع الجبن اللذيذ وقطع الطماطم الطازجة"),
                    ChronologicalStep(3, "🥪", "شطيرة جاهزة", "نحصل على شطيرة شهية ومغذية جاهزة للأكل")
                )
            ),
            ChronologicalPuzzle(
                title = "لوحة فنية رائعة 🖼️",
                categoryName = "الفنون والمواهب",
                steps = listOf(
                    ChronologicalStep(1, "📝", "ورقة بيضاء", "نحضر كراسة الرسم البيضاء وأقلام التلوين"),
                    ChronologicalStep(2, "✏️", "رسم الخطوط", "نرسم الأشكال والخطوط بلطف بقلم الرصاص"),
                    ChronologicalStep(3, "🎨", "تلوين اللوحة", "نلونها بألوان زاهية لتصبح لوحة فنية رائعة")
                )
            )
        )
    }

    var currentPuzzleIndex by remember { mutableStateOf(0) }
    val currentPuzzle = puzzles[currentPuzzleIndex]

    // Randomized options
    var randomizedOptions by remember { mutableStateOf<List<ChronologicalStep>>(emptyList()) }
    
    // Slots sequence (3 slots)
    val selectedSlots = remember { mutableStateListOf<ChronologicalStep?>(null, null, null) }
    
    // Correct status
    var isCorrectCheck by remember { mutableStateOf<Boolean?>(null) }
    var showSuccessModal by remember { mutableStateOf(false) }

    // SoundEffectsHelper
    val soundHelper = remember { com.example.ui.SoundEffectsHelper }

    // Shuffling on puzzle change
    LaunchedEffect(currentPuzzleIndex) {
        randomizedOptions = currentPuzzle.steps.shuffled()
        selectedSlots[0] = null
        selectedSlots[1] = null
        selectedSlots[2] = null
        isCorrectCheck = null
        showSuccessModal = false
    }

    // Speech narration on first load of puzzle
    LaunchedEffect(currentPuzzleIndex) {
        onSpeak("لعبة قبل وبعد. رتّب خطوات: ${currentPuzzle.title}")
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.5.dp, SunnyYellow.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        val parentMaxWidth = maxWidth
        val isLandscape = parentMaxWidth > maxHeight
        val baseSize = (parentMaxWidth.value + maxHeight.value) / 100
        val titleFontSize = (baseSize * 2.2f).coerceIn(18f, 28f).sp
        val instructionFontSize = (baseSize * 1.5f).coerceIn(13f, 20f).sp
        val sectionTitleFontSize = (baseSize * 1.6f).coerceIn(14f, 22f).sp
        val cardLabelFontSize = (baseSize * 1.4f).coerceIn(11f, 18f).sp
        val cardEmojiFontSize = (baseSize * 6.5f).coerceIn(48f, 75f).sp
        val badgeFontSize = (baseSize * 1.3f).coerceIn(10f, 15f).sp
        val buttonFontSize = (baseSize * 1.5f).coerceIn(13f, 20f).sp

        val lazyOptionsState = rememberLazyListState()
        val optionsSnapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyOptionsState)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Category Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPuzzle.title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF001D34)
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SunnyYellow.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentPuzzle.categoryName,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = badgeFontSize
                    )
                }
            }

            Text(
                text = "اضغط على البطاقات بالأسفل لترتيب الخطوات من اليمين (قبل) إلى اليسار (بعد) 🧩✨",
                fontSize = instructionFontSize,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== SLOTS AREA (TOP) ====================
            Text(
                text = "صناديق الترتيب 📥 (اضغط على صندوق لتفريغه)",
                fontSize = sectionTitleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF005AC1),
                modifier = Modifier.align(Alignment.Start)
            )

            // 3 slots
            val slotLabels = listOf("الأولى (البداية) ⏳", "الثانية (الوسط) 🔄", "الثالثة (النهاية) ✅")
            val slotBorderColors = listOf(PlayfulOrange, SunnyYellow, MintGreen)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    val step = selectedSlots[i]
                    val isSelected = step != null
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLandscape) 100.dp else 140.dp)
                            .clickable {
                                if (isSelected) {
                                    soundHelper.playBubbleSound()
                                    selectedSlots[i] = null
                                    isCorrectCheck = null
                                }
                            }
                            .border(
                                width = if (isSelected) 2.5.dp else 1.5.dp,
                                color = if (isSelected) slotBorderColors[i] else Color.LightGray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF4F9FF) else Color(0xFFFAFAFA)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (step != null) {
                                Text(
                                    text = step.emoji,
                                    fontSize = cardEmojiFontSize,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = step.label,
                                    fontSize = cardLabelFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = "❓",
                                    fontSize = cardEmojiFontSize * 0.75f,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = slotLabels[i],
                                    fontSize = cardLabelFontSize * 0.85f,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== OPTIONS AREA (BOTTOM) ====================
            Text(
                text = "البطاقات المتاحة للترتيب 👇 (اسحب لليمين واليسار)",
                fontSize = sectionTitleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                modifier = Modifier.align(Alignment.Start)
            )

            // Smooth Horizontal Scrolling Row for Available Cards with Snap Snapping
            LazyRow(
                state = lazyOptionsState,
                flingBehavior = optionsSnapFlingBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) 115.dp else 155.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(randomizedOptions) { step ->
                    val isUsed = selectedSlots.contains(step)
                    
                    Card(
                        modifier = Modifier
                            .width(parentMaxWidth * 0.44f) // Proportional responsive width so children can scroll/snap easily
                            .fillMaxHeight()
                            .clickable(enabled = !isUsed) {
                                soundHelper.playClick()
                                // Find first null slot and place
                                val firstNullIndex = selectedSlots.indexOf(null)
                                if (firstNullIndex != -1) {
                                    selectedSlots[firstNullIndex] = step
                                    onSpeak(step.label)
                                }
                            }
                            .border(
                                width = 1.5.dp,
                                color = if (isUsed) Color.LightGray.copy(alpha = 0.2f) else SoftBluePrimary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUsed) Color(0xFFF0F0F0).copy(alpha = 0.6f) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = step.emoji,
                                fontSize = cardEmojiFontSize,
                                modifier = Modifier.padding(bottom = 2.dp).alpha(if (isUsed) 0.3f else 1f)
                            )
                            Text(
                                text = step.label,
                                fontSize = cardLabelFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (isUsed) Color.Gray.copy(alpha = 0.5f) else Color.Black,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== ACTIONS / CHECK AREA ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                OutlinedButton(
                    onClick = {
                        soundHelper.playBubbleSound()
                        selectedSlots[0] = null
                        selectedSlots[1] = null
                        selectedSlots[2] = null
                        isCorrectCheck = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إعادة 🔄", fontSize = buttonFontSize, fontWeight = FontWeight.Bold)
                }

                // Check button
                Button(
                    onClick = {
                        if (selectedSlots.any { it == null }) {
                            soundHelper.playFailure()
                            Toast.makeText(context, "الرجاء ترتيب جميع البطاقات الثلاثة أولاً! 🧩", Toast.LENGTH_SHORT).show()
                            onSpeak("يرجى وضع جميع البطاقات أولاً")
                        } else {
                            val correct = selectedSlots[0]?.id == 1 && selectedSlots[1]?.id == 2 && selectedSlots[2]?.id == 3
                            isCorrectCheck = correct
                            if (correct) {
                                soundHelper.playSuccess()
                                viewModel.logSessionActivity("لعبة قبل وبعد", 8, "رتب بنجاح خطوات: ${currentPuzzle.title}")
                                onSuccessTrigger()
                                showSuccessModal = true
                            } else {
                                soundHelper.playFailure()
                                Toast.makeText(context, "الترتيب غير دقيق، حاول مجدداً يا بطل! 💡", Toast.LENGTH_SHORT).show()
                                onSpeak("الترتيب غير صحيح، حاول مجدداً يا بطل")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("تحقق من الترتيب ✨", fontSize = buttonFontSize, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // ==================== SUCCESS / ACHIEVEMENT DIALOG (RESPONSIVE) ====================
        if (showSuccessModal) {
            AlertDialog(
                onDismissRequest = { /* dismiss disabled for success celebration */ },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier
                    .fillMaxWidth(if (isLandscape) 0.85f else 0.9f)
                    .fillMaxHeight(if (isLandscape) 0.85f else 0.7f)
                    .padding(16.dp),
                confirmButton = {},
                dismissButton = {},
                title = null,
                text = {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(3.dp, SunnyYellow, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎉 عمل رائع بطل! 🎉",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE65100),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text = "🌟✨🏆✨🌟",
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, MintGreen.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "القصة بالترتيب الصحيح 📖:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF33691E),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    currentPuzzle.steps.forEachIndexed { index, step ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(MintGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "${step.emoji} ${step.label}: ${step.descriptionText}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.DarkGray,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    soundHelper.playClick()
                                    onSpeak("نطق القصة بالكامل: " + currentPuzzle.steps.joinToString(". ") { it.descriptionText })
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "استمع للقصة كاملة 🔊",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    soundHelper.playClick()
                                    showSuccessModal = false
                                    if (currentPuzzleIndex < puzzles.size - 1) {
                                        currentPuzzleIndex++
                                    } else {
                                        currentPuzzleIndex = 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = "القصة التالية ➔",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
