package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class GameCategory(val key: String, val displayName: String, val emoji: String) {
    FACES("FACES", "وجوه ومشاعر", "😊"),
    ANIMALS("ANIMALS", "حيوانات وأصوات", "🦁"),
    ACTIONS("ACTIONS", "أفعال يومية", "🏃‍♂️"),
    SHADOWS("SHADOWS", "أشكال وألوان", "⭐"),
    LETTERS("LETTERS", "حروف الهجاء", "أ")
}

data class LidCardState(
    val id: Int,
    val pairId: Int,
    val emoji: String,
    val speechText: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class MatchItemPool(
    val id: Int,
    val icon: String,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingGameScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isMuted by viewModel.isMuted.collectAsState()

    // 1. Data Pools for the 5 categories
    val facesPool = remember {
        listOf(
            MatchItemPool(1, "😊", "وجه مبتسم وسعيد! أنا فرحان ومسرور"),
            MatchItemPool(2, "🤩", "وجه متحمس ومبتهج! يا لها من مفاجأة جميلة"),
            MatchItemPool(3, "🥰", "وجه محب وودود! قلبي مليء بالحب والمودة"),
            MatchItemPool(4, "😂", "وجه ضاحك ومرح! ههههه، هذه الضحكة لطيفة"),
            MatchItemPool(5, "😉", "وجه يغمز بمرح! أنت ذكي وشاطر جداً"),
            MatchItemPool(6, "😎", "وجه رائع بنظارة شمسية! واثق وجميل"),
            MatchItemPool(7, "😜", "وجه يخرج لسانه بمرح ولعب! مرح ولعب ونشاط"),
            MatchItemPool(8, "😮", "وجه مندهش ومتعجب! واو، هذا مذهل حقاً")
        )
    }

    val animalsPool = remember {
        listOf(
            MatchItemPool(1, "🦁", "أسد شجاع قوي! زئير، أنا ملك الغابة"),
            MatchItemPool(2, "🐱", "قطة لطيفة وجميلة! مواء مواء، تحب اللعب بالكرة"),
            MatchItemPool(3, "🐶", "كلب وفي وحارس! هو هو، يركض بنشاط وسرعة"),
            MatchItemPool(4, "🐦", "عصفور صغير مغرد! صوصو صوصو، يطير في السماء"),
            MatchItemPool(5, "🐸", "ضفدع أخضر قفاز! كواك كواك، يقفز في البحيرة"),
            MatchItemPool(6, "🐮", "بقرة معطاءة ومفيدة! مووو مووو، تعطينا حليباً لذيذاً"),
            MatchItemPool(7, "🐵", "قرد شقي ومرح! أوو أوو، يحب تسلق الأشجار والموز"),
            MatchItemPool(8, "🐘", "فيل ضخم وطيب! طوووط، لديه خرطوم طويل رائع")
        )
    }

    val actionsPool = remember {
        listOf(
            MatchItemPool(1, "😋", "طفل يأكل طعاماً شهياً! همم، لذيذ ومغذي"),
            MatchItemPool(2, "🥛", "طفل يشرب كوباً من الحليب العذب والمفيد لصحة العظام"),
            MatchItemPool(3, "😴", "طفل نائم في سريره بهدوء وعافية! أحلام سعيدة"),
            MatchItemPool(4, "📚", "طفل يقرأ كتاباً مفيداً ليتعلم أشياء جديدة وذكية"),
            MatchItemPool(5, "🏃‍♂️", "طفل يجري ويلعب بنشاط وحيوية في الحديقة الواسعة"),
            MatchItemPool(6, "✍️", "طفل يكتب ويرسم لوحة فنية جميلة بألوان رائعة"),
            MatchItemPool(7, "🧼", "طفل يغسل يديه بالماء والصابون ليحافظ على نظافته"),
            MatchItemPool(8, "🗣️", "طفل يتحدث ويعبر عن مشاعره الجميلة بوضوح ومخارج حروف صحيحة")
        )
    }

    val shadowsPool = remember {
        listOf(
            MatchItemPool(1, "❤️", "شكل قلب أحمر ينبض بالحب والود الصادق"),
            MatchItemPool(2, "⭐", "نجمة ذهبية لامعة تضيء في أعالي السماء المظلمة"),
            MatchItemPool(3, "🔵", "دائرة زرقاء مستديرة وناعمة ككرة زجاجية جميلة"),
            MatchItemPool(4, "🟩", "مربع أخضر زاهي بأربعة أضلاع متطابقة ومنظمة"),
            MatchItemPool(5, "🔺", "مثلث أحمر متناسق بثلاثة زوايا وثلاثة أضلاع"),
            MatchItemPool(6, "🔶", "شكل معين برتقالي متألق يشبه قطعة الماس"),
            MatchItemPool(7, "🌙", "هلال أصفر جميل يزين السماء في بداية الشهر الهجري"),
            MatchItemPool(8, "🌸", "وردة وردية متفتحة تنشر رائحة عطرة في البستان")
        )
    }

    val lettersPool = remember {
        listOf(
            MatchItemPool(1, "أ", "ألف! أرنب يقفز ويلعب في العشب الأخضر"),
            MatchItemPool(2, "ب", "باء! بطة تسبح في البحيرة وتصيح بط بط"),
            MatchItemPool(3, "ت", "تاء! تفاحة حمراء لذيذة ومغذية ومفيدة جداً"),
            MatchItemPool(4, "ث", "ثاء! ثعلب ذكي يمشي بهدوء وسرعة وخفة"),
            MatchItemPool(5, "ج", "جيم! جمل صبور يتحمل العطش ويمشي في الصحراء"),
            MatchItemPool(6, "ح", "حاء! حصان سريع قوي يجري في السهول الواسعة"),
            MatchItemPool(7, "خ", "خاء! خروف لطيف مغطى بالصوف الأبيض الدافئ"),
            MatchItemPool(8, "د", "دال! ديك ينادي بصوت عالي كوكوكوكو في الصباح الباكر")
        )
    }

    // 2. Active States
    var selectedCategory by remember { mutableStateOf(GameCategory.FACES) }
    var cardsList by remember { mutableStateOf(listOf<LidCardState>()) }
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var movesCount by remember { mutableStateOf(0) }
    var pairsMatched by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var showVictoryOverlay by remember { mutableStateOf(false) }

    // Helper logic to shuffle cards and reset lid state
    val resetGame = {
        val pool = when (selectedCategory) {
            GameCategory.FACES -> facesPool
            GameCategory.ANIMALS -> animalsPool
            GameCategory.ACTIONS -> actionsPool
            GameCategory.SHADOWS -> shadowsPool
            GameCategory.LETTERS -> lettersPool
        }
        val doubled = (pool + pool).shuffled()
        cardsList = doubled.mapIndexed { i, item ->
            LidCardState(
                id = i,
                pairId = item.id,
                emoji = item.icon,
                speechText = item.text,
                isFlipped = false,
                isMatched = false
            )
        }
        firstSelectedIndex = null
        movesCount = 0
        pairsMatched = 0
        isProcessing = false
        showVictoryOverlay = false
        onSpeak("اختر فئة وابدأ برفع الأغطية الذهبية لتجد الأزواج المتطابقة!")
    }

    // Shuffle automatically upon selecting a category or initially
    LaunchedEffect(selectedCategory) {
        resetGame()
    }

    // Core tapping handler with speech feedback and mismatch delays
    val onCardClick: (Int) -> Unit = { index ->
        if (!isProcessing) {
            val card = cardsList[index]
            if (!card.isFlipped && !card.isMatched) {
                // Play lid slide sound
                SoundEffectsHelper.playBubbleSound()

                // Flip card locally
                cardsList = cardsList.mapIndexed { i, c ->
                    if (i == index) c.copy(isFlipped = true) else c
                }

                // Speak the Arabic details out loud
                onSpeak(card.speechText)

                if (firstSelectedIndex == null) {
                    firstSelectedIndex = index
                } else {
                    val firstIdx = firstSelectedIndex!!
                    movesCount++

                    if (cardsList[firstIdx].pairId == card.pairId) {
                        // Match Found! Keep flipped permanently
                        cardsList = cardsList.mapIndexed { i, c ->
                            if (i == firstIdx || i == index) c.copy(isMatched = true) else c
                        }
                        pairsMatched++
                        firstSelectedIndex = null
                        SoundEffectsHelper.playSuccess()

                        if (pairsMatched == 8) {
                            showVictoryOverlay = true
                            viewModel.logSessionActivity(
                                activityName = "لعبة مطابقة الأزواج بالأغطية",
                                score = 40,
                                notes = "طابق الطفل بنجاح جميع الأغطية الـ 16 للفئة: ${selectedCategory.displayName} بعدد محاولات: $movesCount"
                            )
                            onSpeak("رائع جداً يا بطل! لقد نجحت في مطابقة جميع الأغطية بالأشكال الصحيحة!")
                        }
                    } else {
                        // Mismatch: Lock other clicks, wait 1.5s, then cover back down
                        isProcessing = true
                        coroutineScope.launch {
                            delay(1500)
                            cardsList = cardsList.mapIndexed { i, c ->
                                if (i == firstIdx || i == index) c.copy(isFlipped = false) else c
                            }
                            firstSelectedIndex = null
                            isProcessing = false
                            SoundEffectsHelper.playFailure()
                        }
                    }
                }
            }
        }
    }

    // Detect screen orientation for a responsive design
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDF9), Color(0xFFF0F4C3).copy(alpha = 0.35f), Color(0xFFE1F5FE))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (!isLandscape) {
            // ================== Portrait Layout (Mobile) ==================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // 1. Beautiful Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White, CircleShape)
                                .shadow(2.dp, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مطابقة الأغطية 🧩",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleMute() },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White, CircleShape)
                                .shadow(2.dp, CircleShape)
                        ) {
                            Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp)
                        }

                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Scrolling Category Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFFF9100) else Color.White)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color(0xFFFF6D00) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    SoundEffectsHelper.playClick()
                                    selectedCategory = category
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(category.emoji, fontSize = 16.sp)
                                Text(
                                    text = category.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Stats Banner Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الأزواج المتطابقة: $pairsMatched / 8 🏆",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "عدد المحاولات: $movesCount 🐾",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                // 4. 4x4 Grid - Perfect Size Scaling
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 4) {
                                    val index = row * 4 + col
                                    if (index < cardsList.size) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            LidCardItem(
                                                card = cardsList[index],
                                                index = index,
                                                isProcessing = isProcessing,
                                                onCardClick = onCardClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ================== Landscape Layout (Tablet / Split screen) ==================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Responsive 4x4 Grid fitting the height
                Box(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0 until 4) {
                                    val index = row * 4 + col
                                    if (index < cardsList.size) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            LidCardItem(
                                                card = cardsList[index],
                                                index = index,
                                                isProcessing = isProcessing,
                                                onCardClick = onCardClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column: Controls, Stats, Category choices
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape)
                                    .shadow(2.dp, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الأغطية المتطابقة 🧩",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleMute() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .shadow(2.dp, CircleShape)
                        ) {
                            Text(if (isMuted) "🔇" else "🔊", fontSize = 14.sp)
                        }
                    }

                    // Scoreboard Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("المطابقة 🏆", fontSize = 11.sp, color = Color(0xFF047857))
                                Text("$pairsMatched / 8", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("المحاولات 🐾", fontSize = 11.sp, color = Color(0xFF1D4ED8))
                                Text("$movesCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }
                        }
                    }

                    // Scrollable Category list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            "اختر فئة اللعب:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GameCategory.values().forEach { category ->
                                val isSelected = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFFF9100) else Color.White)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) Color(0xFFFF6D00) else Color(0xFFCBD5E1),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            SoundEffectsHelper.playClick()
                                            selectedCategory = category
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(category.emoji, fontSize = 14.sp)
                                        Text(
                                            text = category.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reset Button at the bottom
                    Button(
                        onClick = { resetGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رموز جديدة وإعادة 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ================== Celebrate Victory Modal Overlay ==================
        if (showVictoryOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(enabled = false) {}, // Block user clicks behind modal
                contentAlignment = Alignment.Center
            ) {
                // Falling high performance native Confetti
                ConfettiOverlay()

                // Pulsing Trophy Animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val trophyScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.16f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.55f else 0.85f)
                        .fillMaxHeight(0.85f)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFC107)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 48.sp,
                                modifier = Modifier.scale(trophyScale)
                            )
                            Text(
                                text = "أحسنت يا بطل! 🎉",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF9100),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "لقد طابقت جميع الأغطية بنجاح!",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "الفئة: ${selectedCategory.displayName}",
                                fontSize = 11.sp,
                                color = Color(0xFF78909C),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "الخطوات: $movesCount 🐾",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E88E5)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "النقاط: +40 ⭐",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "العب مجدداً 🔄",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LidCardItem(
    card: LidCardState,
    index: Int,
    isProcessing: Boolean,
    onCardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .testTag("card_$index")
            .clickable(enabled = !isProcessing && !card.isFlipped && !card.isMatched) {
                onCardClick(index)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMatched) Color(0xFFE8F5E9) else Color(0xFFF9FBE7)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (card.isMatched) Color(0xFF81C784) else Color(0xFFDCE775)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Underneath content: centered big emoji and state description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                Text(
                    text = card.emoji,
                    fontSize = if (card.emoji.length > 2) 32.sp else 45.sp,
                    modifier = Modifier.scale(if (card.isMatched) 1.15f else 1.0f)
                )

                if (card.isMatched) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "متطابق ✨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Tactile Lid overlay that slides off
            val isRevealed = card.isFlipped || card.isMatched

            val slideOffset by animateDpAsState(
                targetValue = if (isRevealed) (-85).dp else 0.dp,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "slide"
            )
            val scale by animateFloatAsState(
                targetValue = if (isRevealed) 0.4f else 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "scale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isRevealed) 0f else 1f,
                animationSpec = tween(durationMillis = 400),
                label = "alpha"
            )

            if (alpha > 0.01f) {
                YellowPlasticLid(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .offset(x = slideOffset, y = -slideOffset)
                        .scale(scale)
                        .alpha(alpha)
                )
            }
        }
    }
}

@Composable
fun YellowPlasticLid(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 5.dp, shape = CircleShape, clip = false)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFFFF8F00))
                ),
                shape = CircleShape
            )
            .border(3.dp, Color(0xFFE65100), CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Trim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color(0xFFFFE082).copy(alpha = 0.85f), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Mushroom Knobby peg / knob
            Box(
                modifier = Modifier
                    .fillMaxSize(0.42f)
                    .shadow(elevation = 3.dp, shape = CircleShape, clip = false)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFDE7), Color(0xFFF57C00))
                        ),
                        shape = CircleShape
                    )
                    .border(1.5.dp, Color(0xFFE65100), CircleShape)
            )
        }
    }
}

@Composable
fun ConfettiOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan, Color.LightGray)

        for (i in 0 until 40) {
            val startX = (width / 40) * i
            val speedFactor = 1f + (i % 3) * 0.45f
            val xOffset = sin(progress * 2 * Math.PI.toFloat() + i) * 32.dp.toPx()
            val yPos = (progress * height * speedFactor) % height

            val color = colors[i % colors.size]
            val particleSize = 8.dp.toPx()

            drawRect(
                color = color,
                topLeft = Offset(startX + xOffset, yPos),
                size = androidx.compose.ui.geometry.Size(particleSize, particleSize)
            )
        }
    }
}
