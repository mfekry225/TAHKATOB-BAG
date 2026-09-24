package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

// Data classes for visual puzzles
data class OddOneOutPuzzle(
    val title: String,
    val items: List<String>,
    val correctIndex: Int,
    val audioProneText: String,
    val successText: String
)

data class SequencePuzzle(
    val title: String,
    val sequence: List<String>,
    val options: List<String>,
    val correctOption: String,
    val audioProneText: String,
    val successText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttentionFocusScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeSubGame by remember { mutableStateOf(0) } // 0: Find Odd One, 1: Visual Balloon Tracking, 2: Pattern Sequencing
    val isMuted by viewModel.isMuted.collectAsState()

    // 1. Odd One Out Puzzles
    val oddOnePuzzles = remember {
        listOf(
            OddOneOutPuzzle(
                title = "أوجد الحيوان المختلف عن الآخرين:",
                items = listOf("🦁", "🦁", "🐯", "🦁"),
                correctIndex = 2,
                audioProneText = "ابحث عن الحيوان المختلف واضغط عليه",
                successText = "أحسنت! النمر مختلف عن الأسود!"
            ),
            OddOneOutPuzzle(
                title = "أوجد الفاكهة المختلفة الكائنة في السلة:",
                items = listOf("🍎", "🍎", "🍎", "🍌"),
                correctIndex = 3,
                audioProneText = "ما هي الفاكهة المختلفة هنا؟ اضغط عليها",
                successText = "رائع! الموزة مختلفة عن التفاح!"
            ),
            OddOneOutPuzzle(
                title = "أوجد وسيلة النقل المختلفة:",
                items = listOf("✈️", "🚗", "🚗", "🚗"),
                correctIndex = 0,
                audioProneText = "أي واحدة تطير في السماء ومختلفة؟",
                successText = "يا لك من ذكي! الطائرة تحلق ومختلفة عن السيارات!"
            ),
            OddOneOutPuzzle(
                title = "أوجد الشكل الهندسي المختلف باللون أو الهيئة:",
                items = listOf("🟥", "🟥", "🔺", "🟥"),
                correctIndex = 2,
                audioProneText = "أين هو الشكل الهندسي المختلف؟",
                successText = "بطل! المثلث بثلاث زوايا وهو مختلف عن المربعات!"
            ),
            OddOneOutPuzzle(
                title = "أوجد الحشرة اللطيفة المختلفة:",
                items = listOf("🐝", "🦋", "🐝", "🐝"),
                correctIndex = 1,
                audioProneText = "ابحث عن الفراشة الجميلة المختلفة",
                successText = "جميل جداً! الفراشة الملونة هي المختلفة!"
            )
        )
    }
    var currentOddIndex by remember { mutableStateOf(0) }
    var selectedOddItemIndex by remember { mutableStateOf<Int?>(null) }
    var isOddSelectionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // 2. Pattern Sequel Puzzles
    val sequencePuzzles = remember {
        listOf(
            SequencePuzzle(
                title = "ماذا يأتي بعد الموز والتفاح؟ أكمل الترتيب البصري:",
                sequence = listOf("🍌", "🍎", "🍌", "🍎"),
                options = listOf("🍌", "🍎", "🍇"),
                correctOption = "🍌",
                audioProneText = "موز، تفاح، موز، تفاح. ماذا نضع الآن بالترتيب؟",
                successText = "أحسنت! التالي في النمط هو الموزة!"
            ),
            SequencePuzzle(
                title = "ماذا يأتي في نهاية تتابع النقل والمواصلات؟",
                sequence = listOf("🚗", "✈️", "🚗", "✈️"),
                options = listOf("🚗", "✈️", "🚢"),
                correctOption = "🚗",
                audioProneText = "سيارة، طائرة، سيارة، طائرة. ماذا نضع بعدها؟",
                successText = "ممتاز! السيارة هي القطعة الصحيحة لتكمل النمط!"
            ),
            SequencePuzzle(
                title = "أكمل نمط الأشكال الهندسية بعناية وتركيز:",
                sequence = listOf("🟡", "🟢", "🟡", "🟢"),
                options = listOf("🟡", "🟢", "🔵"),
                correctOption = "🟡",
                audioProneText = "دائرة صفراء، دائرة خضراء، صفراء، خضراء. فكر جيدا ماذا يليها؟",
                successText = "عبقري! الدائرة الصفراء هي إجابتك الصحيحة!"
            ),
            SequencePuzzle(
                title = "دعنا نكمل نمط الأصدقاء اللطاف:",
                sequence = listOf("🐼", "🐼", "🐰", "🐼", "🐼"),
                options = listOf("🐼", "🐰", "🐨"),
                correctOption = "🐰",
                audioProneText = "باندا، باندا، أرنب، باندا، باندا. ماذا نضع في المربع الفارغ؟",
                successText = "مدهش! الأرنب اللطيف هو الذي يكمل الصف النظيف!"
            )
        )
    }
    var currentSequenceIndex by remember { mutableStateOf(0) }
    var selectedSequenceOption by remember { mutableStateOf<String?>(null) }
    var isSequenceSelectionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // 3. Visual Tracking / Balloon pop
    var balloonPopCount by remember { mutableStateOf(0) }
    var balloonEmoji by remember { mutableStateOf("🎈") }
    val balloonEmojis = listOf("🎈", "🐞", "🐝", "🧸", "🦋", "🍄", "⚽")
    
    // Smooth custom physics movement for the tracking balloon / insect using Compose Animatable offsets
    val balloonOffsetX = remember { Animatable(100f) }
    val balloonOffsetY = remember { Animatable(100f) }
    
    // We update target bounds dynamically inside BoxWithConstraints
    var areaWidth by remember { mutableStateOf(300) }
    var areaHeight by remember { mutableStateOf(300) }

    // Coroutine tracking looping movement
    LaunchedEffect(activeSubGame, areaWidth, areaHeight) {
        if (activeSubGame == 1 && areaWidth > 50 && areaHeight > 50) {
            onSpeak("تتبع البالونة الطاحنة التي تظهر على الشاشة واضغط عليها بسرعة لتزيد من تركيزك واكتساب النقاط!")
            
            while (activeSubGame == 1) {
                val targetX = Random.nextInt(20, (areaWidth - 100).coerceAtLeast(40)).toFloat()
                val targetY = Random.nextInt(20, (areaHeight - 120).coerceAtLeast(40)).toFloat()
                val duration = Random.nextInt(900, 2100) // Speed changes to force sustained attention focus
                
                launch {
                    balloonOffsetX.animateTo(
                        targetValue = targetX,
                        animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                    )
                }
                balloonOffsetY.animateTo(
                    targetValue = targetY,
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
                delay(10)
            }
        }
    }

    // Speech instructions on switching games
    LaunchedEffect(activeSubGame) {
        selectedOddItemIndex = null
        isOddSelectionCorrect = null
        selectedSequenceOption = null
        isSequenceSelectionCorrect = null
        
        when (activeSubGame) {
            0 -> onSpeak(oddOnePuzzles[currentOddIndex].audioProneText)
            2 -> onSpeak(sequencePuzzles[currentSequenceIndex].audioProneText)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قسم الانتباه والتركيز 🧠🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF001D34)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("attention_back_button")) {
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF9E6), Color(0xFFE2F9D3)) // Warm focus-enhancing color palette
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant top selection tabs for games matching high functional fidelity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("أوجد المختلف 🔍", 0),
                    Pair("التتبع البصري 🐞", 1),
                    Pair("إكمال الأنماط 🔁", 2)
                ).forEach { (label, index) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (activeSubGame == index) SoftBluePrimary else Color.Transparent)
                            .clickable { activeSubGame = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (activeSubGame == index) Color.White else SoftBluePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Central Game viewport Container
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(3.dp, SoftBluePrimary.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activeSubGame) {
                    0 -> {
                        // Game 0: Find the Odd One Out
                        val puzzle = oddOnePuzzles[currentOddIndex]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = puzzle.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BoldDarkText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Odd elements row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                puzzle.items.forEachIndexed { idx, item ->
                                    val isSelected = selectedOddItemIndex == idx
                                    val isCorrect = puzzle.correctIndex == idx
                                    
                                    val cardBorderColor = when {
                                        isSelected && isCorrect -> MintGreen
                                        isSelected && !isCorrect -> PlayfulOrange
                                        else -> SoftBluePrimary.copy(alpha = 0.2f)
                                    }

                                    val cardBgColor = when {
                                        isSelected && isCorrect -> Color(0xFFE8F5E9)
                                        isSelected && !isCorrect -> Color(0xFFFEEBEE)
                                        else -> Color.White
                                    }

                                    Card(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .border(if (isSelected) 4.dp else 2.dp, cardBorderColor, RoundedCornerShape(18.dp))
                                            .clickable(enabled = isOddSelectionCorrect != true) {
                                                selectedOddItemIndex = idx
                                                if (isCorrect) {
                                                    isOddSelectionCorrect = true
                                                    onSpeak(puzzle.successText)
                                                    
                                                    // Logging session success if viewmodel supports children actions
                                                    if (viewModel.activeChild.value != null) {
                                                        viewModel.logSessionActivity("الانتباه والتركيز - أوجد المختلف", 100, "أوجد المختلف رقم ${currentOddIndex + 1} بنجاح")
                                                    }

                                                    // الانتقال التلقائي للسؤال التالي بعد ثانيتين
                                                    coroutineScope.launch {
                                                        delay(2000)
                                                        currentOddIndex = (currentOddIndex + 1) % oddOnePuzzles.size
                                                        selectedOddItemIndex = null
                                                        isOddSelectionCorrect = null
                                                    }
                                                } else {
                                                    isOddSelectionCorrect = false
                                                    onSpeak("حاول مجدداً، ابحث بشكل أفضل!")
                                                }
                                            }
                                            .testTag("odd_item_$idx"),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = item, fontSize = 54.sp)
                                        }
                                    }
                                }
                            }

                            // Success or encouragement feedback footer
                            AnimatedVisibility(
                                visible = isOddSelectionCorrect != null,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isOddSelectionCorrect == true) Color(0xFFE8F5E9) else Color(0xFFFDE8E8))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isOddSelectionCorrect == true) puzzle.successText else "تحذير: هذا الكائن مألوف ومتشابه، ركز وابحث عن الشكل المختلف!",
                                        color = if (isOddSelectionCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Horizontal Controls to skip/shuffle next level
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        currentOddIndex = (currentOddIndex + 1) % oddOnePuzzles.size
                                        selectedOddItemIndex = null
                                        isOddSelectionCorrect = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("odd_skip_button")
                                ) {
                                    Text("التالي ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onSpeak(puzzle.audioProneText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("🔊 استمع للإرشاد", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    1 -> {
                        // Game 1: Moving Balloon Tracking Pop
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE0F7FA).copy(alpha = 0.4f))
                        ) {
                            areaWidth = maxWidth.value.roundToInt()
                            areaHeight = maxHeight.value.roundToInt()
                            
                            Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(SunnyYellow)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "مستوى الانتباه ومحاولات اللمس: $balloonPopCount 🎯",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = BoldDarkText
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            balloonPopCount = 0
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("تصفير 🔄", fontSize = 10.sp, color = BoldDarkText)
                                    }
                                }

                                // Interactive tracking play guide text overlay
                                Text(
                                    "قم بتركيز كامل لعينيك على البالون وأمسكه بإصبعك!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF006064),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Dynamic animated floating focus target
                            Box(
                                modifier = Modifier
                                    .absoluteOffset {
                                        IntOffset(
                                            balloonOffsetX.value.dp.roundToPx(),
                                            balloonOffsetY.value.dp.roundToPx()
                                        )
                                    }
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color.White, PlayfulOrange)
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable {
                                        balloonPopCount++
                                        onSpeak(listOf("رائع!", "بطل!", "انتباه مدهش!", "صيد رائع!", "أمسكتها!").random())
                                        balloonEmoji = balloonEmojis.random()
                                        
                                        // Logging activity session
                                        if (viewModel.activeChild.value != null) {
                                            viewModel.logSessionActivity("الانتباه والتركيز - تتبع بصري", 100, "تتبع ولمس الكائن بنجاح. الإجمالي: $balloonPopCount")
                                        }
                                    }
                                    .testTag("balloon_target"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = balloonEmoji,
                                    fontSize = 64.sp
                                )
                            }
                        }
                    }

                    2 -> {
                        // Game 2: Complete the patterns 
                        val puzzle = sequencePuzzles[currentSequenceIndex]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = puzzle.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BoldDarkText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Render visual pattern row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                puzzle.sequence.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .border(1.5.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item, fontSize = 48.sp)
                                    }
                                }

                                // Arrow pointer
                                Text("➡️", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 4.dp))

                                // Missing piece placeholder box
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEBF3FF))
                                        .border(2.5.dp, SoftBluePrimary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedSequenceOption ?: "❓",
                                        fontSize = 48.sp,
                                        color = SoftBluePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Choose options row
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "اختر الرمز الصحيح لتعبئة الفراغ:",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    puzzle.options.forEach { opt ->
                                        Card(
                                            modifier = Modifier
                                                .size(85.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(2.dp, SoftBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                                .clickable(enabled = isSequenceSelectionCorrect != true) {
                                                    selectedSequenceOption = opt
                                                    if (opt == puzzle.correctOption) {
                                                        isSequenceSelectionCorrect = true
                                                        onSpeak(puzzle.successText)
                                                        
                                                        if (viewModel.activeChild.value != null) {
                                                            viewModel.logSessionActivity("الانتباه والتركيز - غلق الأنماط", 100, "إكمال النمط رقم ${currentSequenceIndex + 1} بنجاح")
                                                        }

                                                        // الانتقال التلقائي للنمط التالي بعد ثانيتين
                                                        coroutineScope.launch {
                                                            delay(2000)
                                                            currentSequenceIndex = (currentSequenceIndex + 1) % sequencePuzzles.size
                                                            selectedSequenceOption = null
                                                            isSequenceSelectionCorrect = null
                                                        }
                                                    } else {
                                                        isSequenceSelectionCorrect = false
                                                        onSpeak("هذا غير متناسق مع الترتيب، جرب خياراً آخراً!")
                                                    }
                                                }
                                                .testTag("pattern_opt_$opt"),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = opt, fontSize = 52.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Feedback footer
                            AnimatedVisibility(
                                visible = isSequenceSelectionCorrect != null,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSequenceSelectionCorrect == true) Color(0xFFE8F5E9) else Color(0xFFFDE8E8))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSequenceSelectionCorrect == true) puzzle.successText else "غير صحيح! الترتيب بحاجة للانسجام والمطابقة، أعد المحاولة!",
                                        color = if (isSequenceSelectionCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Level Controllers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        currentSequenceIndex = (currentSequenceIndex + 1) % sequencePuzzles.size
                                        selectedSequenceOption = null
                                        isSequenceSelectionCorrect = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("sequence_skip_button")
                                ) {
                                    Text("التالي ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onSpeak(puzzle.audioProneText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("🔊 استمع للنمط", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
