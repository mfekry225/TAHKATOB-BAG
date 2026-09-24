package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MathTab(val title: String, val emoji: String) {
    CARDS_1_100("بطاقات الأرقام حتى ١٠٠", "💯"),
    COUNT_QUIZ("صل الرقم بالكمية", "🎈"),
    OPERATIONS("الجمع والطرح البسيط", "➕"),
    MULTIPLICATION("جدول الضرب الذكي", "✖️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathLearningScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isMuted by viewModel.isMuted.collectAsState()
    var activeTab by remember { mutableStateOf(MathTab.CARDS_1_100) }

    // Sound announcer when tab changes
    LaunchedEffect(activeTab) {
        onSpeak(activeTab.title)
    }

    // Mathematical phonetic Arabic reader up to 100
    fun getArabicNumberPhonetic(number: Int): String {
        if (number == 100) return "مئة"
        val units = listOf("", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة")
        val teens = listOf("عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر")
        val tens = listOf("", "عشرة", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")

        return when {
            number in 1..9 -> units[number]
            number in 10..19 -> teens[number - 10]
            number % 10 == 0 -> tens[number / 10]
            else -> {
                val unitPart = units[number % 10]
                val tenPart = tens[number / 10]
                "$unitPart و$tenPart"
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1FDFB), Color(0xFFE8F9FF))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // ================= LEFT: MATH SUB-TAB SELECTOR =================
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFE0F2F1), RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header close back + Mute toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEEEEEE), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مرح الرياضيات ➕", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF001D34))
                }

                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                ) {
                    Text(if (isMuted) "🔇" else "🔊", fontSize = 14.sp)
                }
            }

            Text(
                "اختر اللعبة أو الأداة:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            // Selector list of Math Tabs
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MathTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MintGreen else Color(0xFFF1FDFD))
                            .border(1.5.dp, if (isSelected) MintGreen else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                tab.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else Color(0xFF004D40)
                            )
                        }
                    }
                }
            }
        }

        // ================= CENTER MULTI-VIEW REPERTOIRE AREA =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Dynamic view selector depending on active math tab
            when (activeTab) {
                // ================= 1. CARD REPRESENTATION 1 TO 100 =================
                MathTab.CARDS_1_100 -> {
                    var isGridView by remember { mutableStateOf(true) } // Default to beautiful Grid view!
                    var selectedCardIndex by remember { mutableStateOf(1) }
                    
                    val pagerState = rememberPagerState(initialPage = 1) { 101 } // 1 to 100

                    // Sync pager selection
                    LaunchedEffect(selectedCardIndex) {
                        if (pagerState.currentPage != selectedCardIndex) {
                            pagerState.animateScrollToPage(selectedCardIndex)
                        }
                        onSpeak("$selectedCardIndex: " + getArabicNumberPhonetic(selectedCardIndex))
                    }

                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage in 1..100 && selectedCardIndex != pagerState.currentPage) {
                            selectedCardIndex = pagerState.currentPage
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header controller for Grid/Card View Toggle
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("بطاقات الأعداد التفاعلية (١-١٠٠):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(if (isGridView) "اضغط على أي رقم من الشبكة لقراءته وسماعه" else "اسحب لليمين واليسار للتنقل بين الأرقام (قابلة للسحب!) 👈👉", fontSize = 10.sp, color = Color.Gray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { isGridView = !isGridView },
                                        colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (isGridView) "عرض بطاقة 🃏" else "عرض شبكة 🔢", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onSpeak("$selectedCardIndex " + getArabicNumberPhonetic(selectedCardIndex)) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("استمع 🔊", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (isGridView) {
                            // Grid view of all numbers 1 to 100
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(8.dp)
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 48.dp),
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(100) { index ->
                                        val num = index + 1
                                        val isSelected = selectedCardIndex == num
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) MintGreen else Color(0xFFF9F9F9))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MintGreen else Color.LightGray.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { 
                                                    selectedCardIndex = num
                                                    isGridView = false // switch to card swipe view!
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "$num",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = if (isSelected) Color.White else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Draggable/Swipeable single card view using HorizontalPager!
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) { page ->
                                val currentPageNum = if (page == 0) 1 else page
                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$currentPageNum",
                                            fontSize = 86.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MintGreen
                                        )
                                        Text(
                                            text = getArabicNumberPhonetic(currentPageNum),
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        // Dynamic cute symbols counters up to 10 for counting, and block groups for larger numbers!
                                        val symbol = "⭐"
                                        val representations = when {
                                            currentPageNum <= 10 -> List(currentPageNum) { symbol }.joinToString(" ")
                                            currentPageNum <= 30 -> List(10) { symbol }.joinToString(" ") + "\n" + List(currentPageNum - 10) { symbol }.joinToString(" ")
                                            else -> {
                                                val groupsOfTen = currentPageNum / 10
                                                val remaining = currentPageNum % 10
                                                "مجموعات من ١٠: " + "📦".repeat(groupsOfTen) + (if (remaining > 0) " + " + "⭐".repeat(remaining) else "")
                                            }
                                        }
                                        Text(
                                            text = representations,
                                            fontSize = 24.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                            lineHeight = 28.sp
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            "اسحب لليمين أو اليسار للتنقل (الأرقام قابلة للسحب!) 👈👉",
                                            fontSize = 11.sp,
                                            color = Color.LightGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ================= 2. MATCH NUMBER WITH QUANTITY QUIZ =================
                MathTab.COUNT_QUIZ -> {
                    var targetCount by remember { mutableStateOf(3) }
                    var options by remember { mutableStateOf(listOf(2, 3, 5)) }
                    var hasWon by remember { mutableStateOf(false) }

                    val resetQuiz = {
                        targetCount = (1..10).random()
                        val uniqueOptions = mutableSetOf(targetCount)
                        while (uniqueOptions.size < 3) {
                            uniqueOptions.add((1..10).random())
                        }
                        options = uniqueOptions.toList().shuffled()
                        hasWon = false
                        onSpeak("كم عدد البالونات الملونة التي تظهر على الكرت؟")
                    }

                    LaunchedEffect(Unit) {
                        resetQuiz()
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "لعبة: صل الرقم بالكمية الصح! 🤔",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Central illustration card holding balloons
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (hasWon) {
                                    Text("🎉🤩 BINGO!", fontSize = 44.sp)
                                    Text("أحسنت يا بطل! إجابة صحيحة متطابقة", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MintGreen)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { resetQuiz() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange)
                                    ) {
                                        Text("جولة جديدة 🔄")
                                    }
                                } else {
                                    val balloons = List(targetCount) { "🎈" }.joinToString(" ")
                                    Text(balloons, fontSize = 42.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("عد البالونات جيداً واختر الرقم المناسب من الأسفل 👇", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Row of options
                        if (!hasWon) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                options.forEach { option ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .border(2.dp, SoftBluePrimary, RoundedCornerShape(14.dp))
                                            .clickable {
                                                if (option == targetCount) {
                                                    hasWon = true
                                                    onSpeak("رائع! الإجابة صحيحة")
                                                    viewModel.logSessionActivity(
                                                        activityName = "لعبة عد الكميات والبالونات",
                                                        score = 15,
                                                        notes = "نجح الطفل في عد $targetCount بالونات ووصلها بالعدد المكافئ"
                                                    )
                                                    // Auto-advance to next quiz question after simple delay
                                                    coroutineScope.launch {
                                                        delay(2500)
                                                        if (hasWon) {
                                                            resetQuiz()
                                                        }
                                                    }
                                                } else {
                                                    onSpeak("هذه هي القيمة " + getArabicNumberPhonetic(option) + "، حاول مرة أخرى لعد البالونات")
                                                    Toast.makeText(context, "حاول مجدداً لتخطي التحدي!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$option",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SoftBluePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ================= 3. MATH ARITHMETIC (PLAYFUL ADD & SUB) =================
                MathTab.OPERATIONS -> {
                    var mode by remember { mutableStateOf("جمع") } // "جمع", "طرح"
                    var numA by remember { mutableStateOf(4) }
                    var numB by remember { mutableStateOf(2) }
                    var childAnswer by remember { mutableStateOf("") }
                    var hasCelebrated by remember { mutableStateOf(false) }

                    val operatorSymbol = when (mode) {
                        "جمع" -> "➕"
                        else -> "➖"
                    }

                    val resetOperation = {
                        hasCelebrated = false
                        childAnswer = ""
                        when (mode) {
                            "جمع" -> {
                                numA = (1..5).random()
                                numB = (1..4).random()
                                onSpeak("ما ناتج جمع: $numA زائد $numB ؟")
                            }
                            else -> { // "طرح"
                                numA = (4..8).random()
                                numB = (1..3).random()
                                if (numA < numB) {
                                    val temp = numA
                                    numA = numB
                                    numB = temp
                                }
                                onSpeak("ما ناتج طرح: $numA ناقص $numB ؟")
                            }
                        }
                    }

                    LaunchedEffect(mode) {
                        resetOperation()
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Horizontal Toggle Mode for Operations
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("جمع", "طرح").forEach { opType ->
                                val isActive = mode == opType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isActive) SoftViolet else Color.White)
                                        .border(2.dp, if (isActive) SoftViolet else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable { mode = opType }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (opType == "جمع") "عملية الجمع ➕" else "عملية الطرح ➖",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        // Problem card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("$numA", fontSize = 44.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(operatorSymbol, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("$numB", fontSize = 44.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("=", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        if (childAnswer.isEmpty()) "؟" else childAnswer,
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (hasCelebrated) MintGreen else PlayfulOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Representation emojis to help kid count visually
                                val itemEmoji = if (mode == "جمع") "🍎" else "🎈"
                                
                                val visualRepresentation = if (mode == "جمع") {
                                    "عد التفاحات لتجد الجواب:\n" +
                                    List(numA) { itemEmoji }.joinToString(" ") + "  و  " + List(numB) { itemEmoji }.joinToString(" ")
                                } else {
                                    "عد البالونات المتبقية:\n" +
                                    List(numA) { itemEmoji }.joinToString(" ") + " (احذف منها $numB)"
                                }
                                
                                Text(
                                    text = visualRepresentation,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF555555),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        // Answer selectors
                        val correctAnswer = if (mode == "جمع") numA + numB else numA - numB

                        val choices = remember(numA, numB, mode) {
                            val dummySet = mutableSetOf(correctAnswer)
                            while (dummySet.size < 4) {
                                val ran = (0..10).random()
                                dummySet.add(ran)
                            }
                            dummySet.toList().shuffled()
                        }

                        if (!hasCelebrated) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                choices.forEach { choice ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .border(2.dp, SoftBluePrimary, RoundedCornerShape(16.dp))
                                            .clickable {
                                                childAnswer = "$choice"
                                                if (choice == correctAnswer) {
                                                    hasCelebrated = true
                                                    onSpeak("رائع جداً! الإجابة صحيح وبطل")
                                                    viewModel.logSessionActivity(
                                                        activityName = "تعليم العمليات الحسابية: $mode",
                                                        score = 15,
                                                        notes = "نجح الطفل في حل العملية الحسابية البسيطة: $numA $operatorSymbol $numB = $choice"
                                                    )
                                                    // Auto-advance to next arithmetic issue
                                                    coroutineScope.launch {
                                                        delay(2500)
                                                        if (hasCelebrated) {
                                                            resetOperation()
                                                        }
                                                    }
                                                } else {
                                                    onSpeak("لا يا بطل ليس $choice، حاول عد الفواكه أو البالونات")
                                                    Toast.makeText(context, "إجابة خاطئة، حاول مرة أخرى يا بطل! ❤️", Toast.LENGTH_SHORT).show()
                                                    childAnswer = ""
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$choice", fontSize = 22.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { resetOperation() },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("سؤال آخر رائع 🤩 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ================= 4. MULTIPLICATION TABLE SMART ACTIVITIES =================
                MathTab.MULTIPLICATION -> {
                    var multiplicationMode by remember { mutableStateOf("explorer") } // "explorer" or "challenge"
                    var explorerBase by remember { mutableStateOf(2) } // default multiplier base 2
                    
                    // Challenge state
                    var challengeNumA by remember { mutableStateOf(3) }
                    var challengeNumB by remember { mutableStateOf(4) }
                    var challengeAnswerChosen by remember { mutableStateOf("") }
                    var challengeWon by remember { mutableStateOf(false) }

                    val resetChallenge = {
                        challengeNumA = (1..10).random()
                        challengeNumB = (1..10).random()
                        challengeAnswerChosen = ""
                        challengeWon = false
                        onSpeak("كم حاصل ضرب: $challengeNumA في $challengeNumB ؟")
                    }

                    LaunchedEffect(multiplicationMode) {
                        if (multiplicationMode == "challenge") {
                            resetChallenge()
                        } else {
                            onSpeak("اختر الرقم لفتح جدول الضرب التفاعلي له")
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Sub-mode selectors
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (multiplicationMode == "explorer") SoftBluePrimary else Color.White)
                                    .border(2.dp, if (multiplicationMode == "explorer") SoftBluePrimary else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { multiplicationMode = "explorer" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("جدول الضرب التفاعلي 📚", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (multiplicationMode == "explorer") Color.White else Color.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (multiplicationMode == "challenge") SoftBluePrimary else Color.White)
                                    .border(2.dp, if (multiplicationMode == "challenge") SoftBluePrimary else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { multiplicationMode = "challenge" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("تحدي الضرب الذكي 🚀", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (multiplicationMode == "challenge") Color.White else Color.Black)
                            }
                        }

                        if (multiplicationMode == "explorer") {
                            // Row of multipliers (1 to 10) on top
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..10).forEach { baseNum ->
                                    val isSelected = explorerBase == baseNum
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(if (isSelected) PlayfulOrange else Color.White)
                                            .border(1.dp, Color.LightGray, CircleShape)
                                            .clickable { 
                                                explorerBase = baseNum
                                                onSpeak("جدول ضرب العدد $baseNum")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$baseNum", fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Black)
                                    }
                                }
                            }

                            // Full table view scrollable beautifully
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .padding(8.dp)
                            ) {
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    (1..10).forEach { multiplier ->
                                        val result = explorerBase * multiplier
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFAFAFA))
                                                .clickable {
                                                    onSpeak("$explorerBase ضرب $multiplier يساوي $result")
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⭐", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    "$explorerBase ✖️ $multiplier",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.DarkGray
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("=", fontSize = 16.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    "$result",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = MintGreen
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("🔊", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // CHALLENGES ACTIVITY
                            val correctAnswer = challengeNumA * challengeNumB
                            val challengeChoices = remember(challengeNumA, challengeNumB) {
                                val dummySet = mutableSetOf(correctAnswer)
                                while (dummySet.size < 4) {
                                    val ran = (1..100).random()
                                    dummySet.add(ran)
                                }
                                dummySet.toList().shuffled()
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$challengeNumA", fontSize = 32.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("✖️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("$challengeNumB", fontSize = 32.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("=", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            if (challengeAnswerChosen.isEmpty()) "؟" else challengeAnswerChosen,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (challengeWon) MintGreen else PlayfulOrange
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    val groupsStr = "$challengeNumA مجموعات من $challengeNumB تفاحات كالتالي:\n" +
                                            List(challengeNumA) { "🍎".repeat(challengeNumB) }.joinToString("  |  ")
                                    
                                    Text(
                                        text = groupsStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                    )
                                }
                            }

                            if (!challengeWon) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    challengeChoices.forEach { choice ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(54.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .border(2.dp, SoftBluePrimary, RoundedCornerShape(16.dp))
                                                .clickable {
                                                    challengeAnswerChosen = "$choice"
                                                    if (choice == correctAnswer) {
                                                        challengeWon = true
                                                        SoundEffectsHelper.playSuccess()
                                                        onSpeak("يا لك من عبقري ذكي! إجابة صحيحة")
                                                        viewModel.logSessionActivity(
                                                            activityName = "تحدي الضرب الذكي",
                                                            score = 25,
                                                            notes = "حل الطفل مسألة الضرب بنجاح: $challengeNumA x $challengeNumB = $choice"
                                                        )
                                                        coroutineScope.launch {
                                                            delay(2500)
                                                            if (challengeWon) {
                                                                resetChallenge()
                                                            }
                                                        }
                                                    } else {
                                                        onSpeak("لا يا بطل حاول مرة أخرى لحل حاصل ضرب $challengeNumA في $challengeNumB")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$choice", fontSize = 20.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary)
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { resetChallenge() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("تحدي ضرب رائع جديد 🎉🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
