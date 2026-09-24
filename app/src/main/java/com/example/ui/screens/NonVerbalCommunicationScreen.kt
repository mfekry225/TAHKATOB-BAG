package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonVerbalCommunicationScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeChildState by viewModel.activeChild.collectAsState()

    // Screen Tabs
    var selectedTab by remember { mutableStateOf("pecs") } // "pecs", "attention", "point_game"

    // Fun background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9), // Light green-mint soft gradient
            Color(0xFFE0F2F1), // Gentle turquoise
            Color(0xFFF1F8E9)
        )
    )

    // Window configuration for responsive display
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🧩", fontSize = 26.sp)
                        Column {
                            Text(
                                text = "التواصل غير اللفظي والألعاب الذكية",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B5E20)
                            )
                            if (activeChildState != null) {
                                Text(
                                    text = "تدريب الطفل: ${activeChildState!!.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundEffectsHelper.playClick()
                            onBack()
                        },
                        modifier = Modifier.testTag("nonval_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    titleContentColor = Color(0xFF1B5E20)
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundGradient)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Switcher tab (Mint playful theme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabItems = listOf(
                    Triple("pecs", "لوحة تواصل PECS 📋", Color(0xFFE8F5E9)),
                    Triple("attention", "الانتباه البصري واللمس 🫧", Color(0xFFE3F2FD)),
                    Triple("point_game", "طلبات الطفل الذكية 👦", Color(0xFFFFF3E0))
                )

                tabItems.forEach { (tabId, label, color) ->
                    val isSelected = selectedTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFF2E7D32) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                SoundEffectsHelper.playClick()
                                selectedTab = tabId
                            }
                            .padding(vertical = GridPaddingForTab(isLandscape)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = if (isLandscape) 13.sp else 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF1B5E20) else Color(0xFF555555),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Central Active Activity Area with high interactive response
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    "pecs" -> PecsCommunicationPanel(viewModel, onSpeak)
                    "attention" -> AttentionVisualPanel(viewModel, onSpeak)
                    "point_game" -> PointGamePanel(viewModel, onSpeak)
                }
            }
        }
    }
}

@Composable
fun GridPaddingForTab(isLandscape: Boolean): androidx.compose.ui.unit.Dp {
    return if (isLandscape) 10.dp else 12.dp
}

// PECS Model definition
data class PecsItem(
    val id: Int,
    val text: String,
    val spokenText: String,
    val emoji: String,
    val category: String, // "need", "emotion"
    val color: Color
)

@Composable
fun PecsCommunicationPanel(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeChildState by viewModel.activeChild.collectAsState()

    // Visual items list
    val pecsList = listOf(
        // Needs
        PecsItem(1, "أريد ماء 🥛", "شرب ماء أنا عطشان", "💧", "need", Color(0xFFE3F2FD)),
        PecsItem(2, "أريد تفاحة 🍎", "أنا جائع أريد تفاح", "🍏", "need", Color(0xFFFFF3E0)),
        PecsItem(3, "أريد اللعب 🧸", "لعب ألعاب جميلة", "🧸", "need", Color(0xFFFFFDE7)),
        PecsItem(4, "المرحاض 🚽", "أريد الذهاب للمرحاض حمام", "🚻", "need", Color(0xFFECEFF1)),
        PecsItem(5, "أريد النوم 😴", "أشعر بالنعاس أريد النوم", "🛌", "need", Color(0xFFF3E5F5)),
        PecsItem(6, "الذهاب للحديقة 🚗", "أريد الذهاب للحديقة واللعب", "🌳", "need", Color(0xFFE8F5E9)),
        PecsItem(7, "أنا متألم 🤕", "أنا متألم يعورني هنا", "🥺", "need", Color(0xFFFFEBEE)),
        PecsItem(8, "أريد حضناً 🫂", "أريد حضناً من ماما وبابا", "❤️", "need", Color(0xFFFCE4EC)),
        // Emotions
        PecsItem(9, "أنا سعيد 😊", "أنا سعيد ومسرور اليوم", "😊", "emotion", Color(0xFFE8F5E9)),
        PecsItem(10, "أنا حزين 😢", "أنا حزين أريد المساعدة", "😢", "emotion", Color(0xFFFFF3E0)),
        PecsItem(11, "أنا متعب 🥱", "أنا متعب جداً وكسول", "🥱", "emotion", Color(0xFFECEFF1))
    )

    var lastSpokenCardText by remember { mutableStateOf<String?>(null) }
    var lastIcon by remember { mutableStateOf("") }
    var scaleTrigger by remember { mutableStateOf(false) }

    val scaleAnimate by animateFloatAsState(
        targetValue = if (scaleTrigger) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { scaleTrigger = false }
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Board grid (takes 65-70% space)
        Card(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(2.5.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "المس الرمز للتعبير عما تريد 👈",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(pecsList) { item ->
                        val isSelected = lastSpokenCardText == item.text
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.15f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (isSelected) 3.5.dp else 1.5.dp,
                                    color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFCFD8DC),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    SoundEffectsHelper.playSuccess()
                                    lastSpokenCardText = item.text
                                    lastIcon = item.emoji
                                    scaleTrigger = true
                                    onSpeak(item.spokenText)

                                    if (activeChildState != null) {
                                        viewModel.logSessionActivity(
                                            activityName = "لوحة التواصل PECS",
                                            score = 5,
                                            notes = "طلب بالرمز: '${item.text}' بنجاح."
                                        )
                                    }
                                }
                                .testTag("pecs_item_${item.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) item.color.copy(alpha = 0.9f) else item.color
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.emoji, fontSize = 58.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Side Full-view display panel (highly encouraging for nonverbal children)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFAFBEE5).copy(alpha = 0.2f)),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(2.dp, Color(0xFFB0C4DE))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (lastSpokenCardText != null) {
                    Box(
                        modifier = Modifier
                            .scale(scaleAnimate)
                            .size(175.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color(0xFF4F46E5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lastIcon, fontSize = 120.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "أنا أريد:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Text(
                        lastSpokenCardText!!,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4F46E5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(
                        onClick = { onSpeak(lastSpokenCardText!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("🔊 تشغيل الصوت", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👦", fontSize = 42.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "المس بالبطاقات لتكبيرها وسماع اللفظ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// BUBBLE ATTENTION MODEL
data class FloatingBubble(
    val id: Int,
    var x: Float,
    var y: Float,
    val size: Float,
    val emoji: String,
    val text: String
)

@Composable
fun AttentionVisualPanel(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeChildState by viewModel.activeChild.collectAsState()

    var scoreCount by remember { mutableStateOf(0) }
    val maxTarget = 10

    // List of interactive child items inside bubbles
    val funIcons = listOf("🧸", "🚗", "🌟", "🍏", "🐘", "🎈", "🐱", "🐶", "🦁", "🍰", "🍭", "🍇")
    val arabicNames = listOf("دبدوب", "سيارة", "نجمة", "تفاح", "فيل", "بالون", "بسة", "كلب", "أسد", "كيكة", "حلاوة", "عنب")

    // Dynamic state for active bubbles
    var bubbles by remember { mutableStateOf(listOf<FloatingBubble>()) }

    // Init first bubble set randomly
    LaunchedEffect(Unit) {
        val list = mutableListOf<FloatingBubble>()
        repeat(5) { i ->
            val idx = Random.nextInt(funIcons.size)
            list.add(
                FloatingBubble(
                    id = i,
                    x = Random.nextFloat(), // fractional coordinate 0.1f..0.9f
                    y = Random.nextFloat(),
                    size = (80..105).random().toFloat(),
                    emoji = funIcons[idx],
                    text = arabicNames[idx]
                )
            )
        }
        bubbles = list
    }

    // Dynamic anim loop of bubbles drifting slowly
    LaunchedEffect(bubbles) {
        delay(130)
        bubbles = bubbles.map { b ->
            // Drifts slightly
            var newX = b.x + (Random.nextFloat() * 0.04f - 0.02f)
            var newY = b.y - 0.015f // bubbles rise upwards
            if (newY < -0.1f) {
                newY = 1.1f // Respawn bottom
                newX = Random.nextFloat()
            }
            if (newX < 0f) newX = 1f
            if (newX > 1f) newX = 0f
            b.copy(x = newX, y = newY)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top score board tracking
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFFC0E0FF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎯", fontSize = 20.sp)
                    Text(
                        "هدف الانتباه البصري واللمس:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { scoreCount.toFloat() / maxTarget },
                        color = Color(0xFF1E88E5),
                        trackColor = Color(0xFFE3F2FD),
                        modifier = Modifier
                            .width(140.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "$scoreCount / $maxTarget",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }

        // Active Bubble Popper Canvas Area (taking major middle area)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1).copy(alpha = 0.5f)),
            border = BorderStroke(2.5.dp, Color(0xFFCFD8DC))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                val widthPx = constraints.maxWidth
                val heightPx = constraints.maxHeight

                if (scoreCount >= maxTarget) {
                    // Victory presentation
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⭐", fontSize = 72.sp)
                        Text(
                            text = "أحسنت يا بطل! انتباه بصري متميز 🌟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                SoundEffectsHelper.playClick()
                                scoreCount = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("اللعب مجدداً 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Bubble elements floating
                    bubbles.forEach { b ->
                        val topOffset = (b.y * heightPx).coerceIn(0f, heightPx - b.size * 2)
                        val leftOffset = (b.x * widthPx).coerceIn(0f, widthPx - b.size * 2)

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (leftOffset / LocalConfiguration.current.densityDpi * 160).dp,
                                    y = (topOffset / LocalConfiguration.current.densityDpi * 160).dp
                                )
                                .size(b.size.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0x8090CAF9),
                                            Color(0xB34FC3F7)
                                        )
                                    )
                                )
                                .border(1.5.dp, Color(0xFF29B6F6), CircleShape)
                                .clickable {
                                    SoundEffectsHelper.playBubbleSound()
                                    scoreCount++
                                    onSpeak(b.text)

                                    // Replace popped bubble
                                    val rIdx = Random.nextInt(funIcons.size)
                                    bubbles = bubbles.map {
                                        if (it.id == b.id) {
                                            it.copy(
                                                x = Random.nextFloat(),
                                                y = 1.0f, // starts from bottom again
                                                emoji = funIcons[rIdx],
                                                text = arabicNames[rIdx]
                                            )
                                        } else it
                                    }

                                    if (scoreCount >= maxTarget) {
                                        SoundEffectsHelper.playSuccess()
                                        if (activeChildState != null) {
                                            viewModel.logSessionActivity(
                                                activityName = "لعبة الانتباه البصري والفقاعات",
                                                score = 100,
                                                notes = "أكمل الطفل تحدي تتبع وفقاعات الانتباه البصري لـ 10 فقاعات بنجاح وتجاوب رائع."
                                            )
                                        }
                                    }
                                }
                                .testTag("bubble_item_${b.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(b.emoji, fontSize = (b.size * 0.55f).sp)
                        }
                    }
                }
            }
        }
    }
}

// POINT AND HELP GAME PARTNERS
data class PointStage(
    val character: String,
    val prompt: String,
    val textPrompt: String,
    val options: List<Triple<String, String, Boolean>>, // (emoji, description, isCorrect)
    val successVoice: String
)

@Composable
fun PointGamePanel(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeChildState by viewModel.activeChild.collectAsState()

    val stages = listOf(
        PointStage(
            character = "👦",
            prompt = "أنا جائع جداً! فماذا أطلب بالصور لآكل؟ 🍎",
            textPrompt = "أين طعام الطفل الجائع؟",
            options = listOf(
                Triple("⚽", "كرة قدم", false),
                Triple("🍎", "تفاحة شهية", true),
                Triple("👕", "قميص ملون", false)
            ),
            successVoice = "رائع! أحسنت يا بطل عندما أكون جائعاً أطلب الطعام مثل التفاح والفاكهة."
        ),
        PointStage(
            character = "👦",
            prompt = "أنا عطشان جداً وأريد أن أشرب! فماذا أطلب؟ 🥤",
            textPrompt = "أين شراب الطفل العطشان؟",
            options = listOf(
                Triple("🧩", "مكعبات ملونة", false),
                Triple("🥤", "عصير طبيعي", true),
                Triple("👞", "حذاء رياضي", false)
            ),
            successVoice = "ممتاز! عندما أشعر بالعطش أطلب الماء أو العصير الصحي لأرتوي."
        ),
        PointStage(
            character = "👦",
            prompt = "أشعر بالبرد الشديد في الشتاء! فماذا أرتدي؟ 🧣",
            textPrompt = "ماذا يرتدي الطفل ليشعر بالدفء؟",
            options = listOf(
                Triple("🧣", "وشاح صوف دافئ", true),
                Triple("🧊", "ثلج بارد", false),
                Triple("🎈", "بالون طائر", false)
            ),
            successVoice = "أحسنت يا ذكي! أطلب الملابس الدافئة والوشاح لحمايتنا من البرد القارس."
        ),
        PointStage(
            character = "👦",
            prompt = "أريد اللعب والمرح في الحديقة الخارجية! فماذا أطلب؟ 🪁",
            textPrompt = "ماذا يطلب الطفل ليلهو بالخارج؟",
            options = listOf(
                Triple("🧹", "مكنسة تنظيف", false),
                Triple("🪁", "طائرة ورقية", true),
                Triple("🥄", "ملعقة طعام", false)
            ),
            successVoice = "أنا سعيد جداً! أختار الطائرة لكي أطيرها وأقضي وقتاً مرحاً."
        ),
        PointStage(
            character = "👦",
            prompt = "أشعر بالتعب الشديد وصداع في رأسي! فماذا يحتاج جسمي؟ 🛌",
            textPrompt = "ماذا يطلب الطفل لينال الراحة؟",
            options = listOf(
                Triple("🛌", "سرير مريح للنوم", true),
                Triple("🍦", "مثلجات باردة", false),
                Triple("🔑", "مفتاح سيارة", false)
            ),
            successVoice = "أحسنت الاختيار! عندما أشعر بالصداع أو التعب أستلقي في سريري دافئاً لأستريح."
        )
    )

    var currentStageIdx by remember { mutableStateOf(0) }
    val currentStage = stages[currentStageIdx]

    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var isCorrectOption by remember { mutableStateOf<Boolean?>(null) }

    // Character Jump Displacement Animation
    val jumpAnimate = remember { Animatable(0f) }

    // TTS speaker when starting a stage
    LaunchedEffect(currentStageIdx) {
        onSpeak(currentStage.prompt)
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(2.5.dp, Color(0xFFFFE0B2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Instruction
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "طلبات التعبير والطلب للطفل 👦",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "تساعد الطفل على التعبير السليم عن حاجاته الأساسية بالصور",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Interactive character layout
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = jumpAnimate.value.dp)
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentStage.character, fontSize = 125.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentStage.prompt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clickable { onSpeak(currentStage.prompt) }
                    )
                }
            }

            // Option selection layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    currentStage.options.forEachIndexed { idx, option ->
                        val isPicked = selectedOptionIdx == idx
                        val cardBorderColor = when {
                            isPicked && isCorrectOption == true -> Color(0xFF4CAF50)
                            isPicked && isCorrectOption == false -> Color(0xFFF44336)
                            else -> Color(0xFFFFCC80)
                        }
                        val cardBgColor = when {
                            isPicked && isCorrectOption == true -> Color(0xFFE8F5E9)
                            isPicked && isCorrectOption == false -> Color(0xFFFFEBEE)
                            else -> Color.White
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isPicked) 3.5.dp else 1.5.dp,
                                    color = cardBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isCorrectOption == true) return@clickable // already completed, block duplicates

                                    selectedOptionIdx = idx
                                    if (option.third) {
                                        // CORRECT POINT!
                                        isCorrectOption = true
                                        SoundEffectsHelper.playSuccess()
                                        onSpeak(currentStage.successVoice)

                                        // Trig visual jump
                                        coroutineScope.launch {
                                            jumpAnimate.animateTo(
                                                targetValue = -35f,
                                                animationSpec = tween(150, easing = FastOutSlowInEasing)
                                            )
                                            jumpAnimate.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }

                                        if (activeChildState != null) {
                                            viewModel.logSessionActivity(
                                                activityName = "لعبة الإشارة والطلب المتبادل",
                                                score = 5,
                                                notes = "أصاب الطفل في الإشارة لرمز: ${option.second} لمرحلة ${currentStage.character}."
                                            )
                                        }

                                        // Auto-advance to next stage automatically after 2.5 seconds
                                        coroutineScope.launch {
                                            delay(2500)
                                            if (isCorrectOption == true) {
                                                if (currentStageIdx < stages.size - 1) {
                                                    SoundEffectsHelper.playNext()
                                                    currentStageIdx++
                                                    selectedOptionIdx = null
                                                    isCorrectOption = null
                                                } else {
                                                    SoundEffectsHelper.playSuccess()
                                                    Toast.makeText(context, "يا للمرب اللطيف! أتممت جميع مراحل الإشارة بنجاح ✨", Toast.LENGTH_LONG).show()
                                                    currentStageIdx = 0
                                                    selectedOptionIdx = null
                                                    isCorrectOption = null
                                                }
                                            }
                                        }
                                    } else {
                                        // WRONG TRY
                                        isCorrectOption = false
                                        SoundEffectsHelper.playPrevious()
                                        onSpeak("حاول مرة أخرى لتساعد الكائن اللطيف")
                                    }
                                }
                                .testTag("point_option_${idx}"),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(option.first, fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.second,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Actions Deck
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (currentStageIdx > 0) {
                                SoundEffectsHelper.playPrevious()
                                currentStageIdx--
                                selectedOptionIdx = null
                                isCorrectOption = null
                            }
                        },
                        enabled = currentStageIdx > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("السابق ➡️", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "المرحلة ${currentStageIdx + 1} / ${stages.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE65100)
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStageIdx < stages.size - 1) {
                                SoundEffectsHelper.playNext()
                                currentStageIdx++
                                selectedOptionIdx = null
                                isCorrectOption = null
                            } else {
                                // Victory screen triggered
                                SoundEffectsHelper.playSuccess()
                                Toast.makeText(context, "يا للمرب اللطيف! أتممت جميع مراحل الإشارة بنجاح ✨", Toast.LENGTH_LONG).show()
                                currentStageIdx = 0
                                selectedOptionIdx = null
                                isCorrectOption = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isCorrectOption == true
                    ) {
                        Text(
                            text = if (currentStageIdx < stages.size - 1) "⬅️ التالي" else "إعادة من البداية 🔄", 
                            color = Color.White, 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
