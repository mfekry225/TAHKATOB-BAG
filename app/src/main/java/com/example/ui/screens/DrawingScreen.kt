package com.example.ui.screens

import com.example.ui.SoundEffectsHelper

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

enum class TraceCategory(val title: String, val emoji: String) {
    ARABIC_LETTERS("الحروف العربية", "🗣️"),
    ENGLISH_LETTERS("English Alphabet", "🔤"),
    GEOMETRIC_SHAPES("الأشكال الهندسية", "🔺"),
    ARABIC_NUMBERS("الأرقام العربية", "١٢٣"),
    ENGLISH_NUMBERS("English Numbers", "10")
}

data class TraceItem(
    val category: TraceCategory,
    val char: String,
    val title: String,
    val soundText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()
    var selectedCategory by remember { mutableStateOf(TraceCategory.ARABIC_LETTERS) }

    // Generates 28 Arabic Letters with phonetic help
    val arabicTraceItems = remember {
        listOf(
            TraceItem(TraceCategory.ARABIC_LETTERS, "أ", "أرنب 🐰", "ألف أرنب"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ب", "بطة 🦆", "باء بطة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ت", "تفاحة 🍎", "تاء تفاحة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ث", "ثعلب 🦊", "ثاء ثعلب"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ج", "جمل 🐫", "جيم جمل"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ح", "حوت 🐋", "حاء حوت"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "خ", "خروف 🐑", "خاء خروف"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "د", "ديناصور 🦖", "دال ديناصور"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ذ", "ذئب 🐺", "ذال ذئب"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ر", "رمان 🍎", "راء رمان"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ز", "زرافة 🦒", "زاي زرافة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "س", "سمكة 🐟", "سين سمكة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ش", "شمس ☀️", "شين شمس"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ص", "صقر 🦅", "صاد صقر"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ض", "ضفدع 🐸", "ضاد ضفدع"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ط", "طائرة ✈️", "طاء طائرة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ظ", "ظرف ✉️", "ظاء ظرف"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ع", "عصفور 🐦", "عين عصفور"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "غ", "غزال 🦌", "غين غزال"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ف", "فيل 🐘", "فاء فيل"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ق", "قلم ✏️", "قاف قلم"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ك", "كلب 🐶", "كاف كلب"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ل", "ليمون 🍋", "لام ليمون"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "م", "موز 🍌", "ميم موز"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ن", "نجمة ⭐", "نون نجمة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "هـ", "هلال 🌙", "هاء هلال"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "و", "وردة 🌹", "واو وردة"),
            TraceItem(TraceCategory.ARABIC_LETTERS, "ي", "يد ✋", "ياء يد")
        )
    }

    // Generates 26 English Alphabets
    val englishTraceItems = remember {
        ('A'..'Z').map { char ->
            val titleWord = when(char) {
                'A' -> "Apple 🍎"
                'B' -> "Bear 🐻"
                'C' -> "Cat 🐱"
                'D' -> "Dog 🐶"
                'E' -> "Egg 🥚"
                'F' -> "Fish 🐟"
                'G' -> "Grapes 🍇"
                'H' -> "House 🏠"
                'I' -> "Igloo 🏕️"
                'J' -> "Jar 🫙"
                'K' -> "Kite 🪁"
                'L' -> "Lion 🦁"
                'M' -> "Monkey 🐵"
                'N' -> "Nest   🪺"
                'O' -> "Owl 🦉"
                'P' -> "Parrot 🦜"
                'Q' -> "Queen 👑"
                'R' -> "Rabbit 🐰"
                'S' -> "Sun ☀️"
                'T' -> "Tiger 🐯"
                'U' -> "Umbrella ⛱️"
                'V' -> "Violin 🎻"
                'W' -> "Water 💧"
                'X' -> "Xylophone 🪘"
                'Y' -> "Yacht ⛵"
                'Z' -> "Zebra 🦓"
                else -> "Word"
            }
            TraceItem(TraceCategory.ENGLISH_LETTERS, char.toString(), titleWord, "$char for $titleWord")
        }
    }

    // Geometric Shapes (أشكال هندسية)
    val shapeTraceItems = remember {
        listOf(
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "⭕", "دائرة", "دائرة ممتعة"),
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "🔺", "مثلث", "مثلث ثلاثي الزوايا"),
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "⬜", "مربع", "مربع متساوي الأضلاع"),
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "⭐", "نجمة 🌟", "نجمة مضيئة"),
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "💛", "قلب 💖", "شكل قلب جميل"),
            TraceItem(TraceCategory.GEOMETRIC_SHAPES, "🛑", "ثماني", "شكل ثماني الأوجه")
        )
    }

    // Arabic Numbers (١-١٠)
    val arabicNumberTraceItems = remember {
        listOf(
            TraceItem(TraceCategory.ARABIC_NUMBERS, "١", "واحد (١) ☝️", "الرقم واحد"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٢", "اثنان (٢) ✌️", "الرقم اثنان"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٣", "ثلاثة (٣) 🤟", "الرقم ثلاثة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٤", "أربعة (٤) 🍀", "الرقم أربعة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٥", "خمسة (٥) 🖐️", "الرقم خمسة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٦", "ستة (٦) 🎨", "الرقم ستة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٧", "سبعة (٧) 🙌", "الرقم سبعة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٨", "ثمانية (٨) 🐙", "الرقم ثمانية"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "٩", "تسعة (٩) 🎈", "الرقم تسعة"),
            TraceItem(TraceCategory.ARABIC_NUMBERS, "١٠", "عشرة (١٠) 🍀", "الرقم عشرة")
        )
    }

    // English Numbers (1-10)
    val englishNumberTraceItems = remember {
        (1..10).map { num ->
            val representation = when(num) {
                1 -> "One (1) ☝️"
                2 -> "Two (2) ✌️"
                3 -> "Three (3) 🤟"
                4 -> "Four (4) 🍀"
                5 -> "Five (5) 🖐️"
                6 -> "Six (6) 🎨"
                7 -> "Seven (7) 🙌"
                8 -> "Eight (8) 🐙"
                9 -> "Nine (9) 🎈"
                10 -> "Ten (10) 🍀"
                else -> num.toString()
            }
            TraceItem(TraceCategory.ENGLISH_NUMBERS, num.toString(), representation, "Number $num")
        }
    }

    // Combined items mapping
    val currentCategoryItems = when(selectedCategory) {
        TraceCategory.ARABIC_LETTERS -> arabicTraceItems
        TraceCategory.ENGLISH_LETTERS -> englishTraceItems
        TraceCategory.GEOMETRIC_SHAPES -> shapeTraceItems
        TraceCategory.ARABIC_NUMBERS -> arabicNumberTraceItems
        TraceCategory.ENGLISH_NUMBERS -> englishNumberTraceItems
    }

    // Active tracing element
    var activeItemIndex by remember { mutableStateOf(0) }
    // Safe index bounds
    val activeItem = currentCategoryItems.getOrNull(activeItemIndex % currentCategoryItems.size) ?: currentCategoryItems.first()

    // Tracing drawing state
    val drawingPaths = remember { mutableStateListOf<DrawPath>() }
    val currentPathPoints = remember { mutableStateListOf<Offset>() }

    var selectedColor by remember { mutableStateOf(SoftBluePrimary) }
    var strokeWidth by remember { mutableFloatStateOf(24f) }

    val colors = listOf(
        SoftBluePrimary, PlayfulOrange, MintGreen, SunnyYellow, LovelyPink, SoftViolet, Color.Black, Color.Gray
    )

    // Sound TTS announcer when template changes
    LaunchedEffect(activeItem) {
        onSpeak(activeItem.soundText)
        drawingPaths.clear()
        currentPathPoints.clear()
    }

    // Sound TTS announcer when category changes
    LaunchedEffect(selectedCategory) {
        onSpeak(selectedCategory.title)
        activeItemIndex = 0
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F7FF), Color(0xFFF0FFF6))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        // ================= LEFT PANEL: TEMPLATES SELECTOR =================
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFE3F2FD), RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Back Arrow + Mute toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "مرح الكتابة 🎨",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001D34)
                    )
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

            // High Contrast Category Select Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TraceCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val buttonBgColor = if (isSelected) {
                        when (category) {
                            TraceCategory.ARABIC_LETTERS -> Color(0xFFFA5A3D) // Vibrant Coral Orange
                            TraceCategory.ENGLISH_LETTERS -> Color(0xFF4F46E5) // Royal Violet Blue
                            TraceCategory.GEOMETRIC_SHAPES -> Color(0xFF10B981) // Emerald Green
                            TraceCategory.ARABIC_NUMBERS -> Color(0xFFF59E0B) // Amber Yellow
                            TraceCategory.ENGLISH_NUMBERS -> Color(0xFFEC4899) // Sweet Pink
                        }
                    } else {
                        Color(0xFFF1F5F9) // Warm Light Gray
                    }
                    val labelColor = if (isSelected) Color.White else Color(0xFF334155)

                    Button(
                        onClick = {
                            selectedCategory = category
                            SoundEffectsHelper.playClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBgColor,
                            contentColor = labelColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                              )
                        }
                    }
                }
            }

            Divider(color = Color(0xFFFFECEF), thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Dotted items grid for selection (Tens of icons matching the image request!)
            Text(
                "اختر الرمز للتلوين 📝:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF001D34),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Start
            )

            // Manual chunked scroll grid
            val chunkedItems = remember(currentCategoryItems) {
                currentCategoryItems.chunked(3)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunkedItems.forEachIndexed { rowIndex, rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowList.forEachIndexed { colIndex, item ->
                            val actualIndex = rowIndex * 3 + colIndex
                            val isSelected = activeItemIndex == actualIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SoftBluePrimary.copy(alpha = 0.25f) else Color(0xFFEDF4FE))
                                    .border(
                                        2.dp,
                                        if (isSelected) SoftBluePrimary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { 
                                        activeItemIndex = actualIndex
                                        SoundEffectsHelper.playClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.char,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF001D34)
                                    )
                                    Text(
                                        text = item.title.substringBefore(" ").take(4),
                                        fontSize = 8.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (rowList.size < 3) {
                            repeat(3 - rowList.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // ================= CENTER CANVAS CONTENT AREA =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Card showing selected guide & target
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PlayfulOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activeItem.char, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "تمرين تتبع: " + activeItem.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001D34)
                            )
                            Text(
                                "تتبع ببطء على خطوط النقاط لتعلم الكتابة والتلوين",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // TTS Replay Button
                    Button(
                        onClick = { onSpeak(activeItem.soundText) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استمع 🗣️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // The main dynamic classroom blackboard canvas!
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, PlayfulOrange.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPathPoints.clear()
                                currentPathPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPathPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPathPoints.isNotEmpty()) {
                                    drawingPaths.add(
                                        DrawPath(
                                            points = currentPathPoints.toList(),
                                            color = selectedColor,
                                            strokeWidth = strokeWidth
                                        )
                                    )
                                    currentPathPoints.clear()
                                }
                            }
                        )
                    }
                    .testTag("drawing_canvas"),
                contentAlignment = Alignment.Center
            ) {
                // Drawing classroom copy-book lines background (Perfect attention-to-detail!)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeColor = Color(0xFFE3F2FD)
                    val spacing = 36.dp.toPx()
                    var y = spacing
                    while (y < size.height) {
                        drawLine(
                            color = strokeColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f
                        )
                        y += spacing
                    }
                }

                // Guide item printed inside the canvas (Dotted lines/points for real educational copywriting)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (selectedCategory) {
                        TraceCategory.GEOMETRIC_SHAPES -> {
                            // Draw dynamic giant shape dashed outline in center
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = Path()
                                    val sizePx = size.width
                                    when (activeItem.char) {
                                        "⭕" -> {
                                            drawCircle(
                                                color = Color.LightGray,
                                                radius = sizePx / 2.2f,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                        "🔺" -> {
                                            path.moveTo(sizePx / 2f, 20f)
                                            path.lineTo(sizePx - 20f, sizePx - 20f)
                                            path.lineTo(20f, sizePx - 20f)
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = Color.LightGray,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                        "⬜" -> {
                                            path.moveTo(30f, 30f)
                                            path.lineTo(sizePx - 30f, 30f)
                                            path.lineTo(sizePx - 30f, sizePx - 30f)
                                            path.lineTo(30f, sizePx - 30f)
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = Color.LightGray,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                        "⭐" -> {
                                            // 5 point star calculation
                                            val centerX = sizePx / 2f
                                            val centerY = sizePx / 2f
                                            val outerRadius = sizePx / 2f - 10f
                                            val innerRadius = outerRadius / 2.5f
                                            var angle = -Math.PI / 2
                                            val angleStep = Math.PI / 5

                                            path.moveTo(
                                                (centerX + outerRadius * Math.cos(angle)).toFloat(),
                                                (centerY + outerRadius * Math.sin(angle)).toFloat()
                                            )
                                            for (i in 1..9) {
                                                angle += angleStep
                                                val radius = if (i % 2 == 0) outerRadius else innerRadius
                                                path.lineTo(
                                                    (centerX + radius * Math.cos(angle)).toFloat(),
                                                    (centerY + radius * Math.sin(angle)).toFloat()
                                                )
                                            }
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = Color.LightGray,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                        "💛" -> {
                                            // Heart calculation path
                                            val width = sizePx
                                            val height = sizePx
                                            path.moveTo(width / 2f, height / 4f)
                                            path.cubicTo(width / 4f, 0f, 0f, height / 3f, width / 2f, height - 20f)
                                            path.cubicTo(width, height / 3f, width * 3f / 4f, 0f, width / 2f, height / 4f)
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = Color.LightGray,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                        else -> {
                                            // Octagon default
                                            path.moveTo(sizePx * 0.3f, 20f)
                                            path.lineTo(sizePx * 0.7f, 20f)
                                            path.lineTo(sizePx - 20f, sizePx * 0.3f)
                                            path.lineTo(sizePx - 20f, sizePx * 0.7f)
                                            path.lineTo(sizePx * 0.7f, sizePx - 20f)
                                            path.lineTo(sizePx * 0.3f, sizePx - 20f)
                                            path.lineTo(20f, sizePx * 0.7f)
                                            path.lineTo(20f, sizePx * 0.3f)
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = Color.LightGray,
                                                style = Stroke(
                                                    width = 12f,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // For alphanumeric tracing: print a massive dashed light gray character in Center
                            Text(
                                text = activeItem.char,
                                fontSize = 160.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray.copy(alpha = 0.45f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.wrapContentSize(),
                                letterSpacing = 0.sp
                            )
                        }
                    }
                }

                // Guided "Trace circles/target start" points overlays to help toddlers know where to draw
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "🟢 ابدأ الرسم من هنا",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }

                // Interactive child finger painting canvas layer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw already cached painted lines
                    drawingPaths.forEach { path ->
                        if (path.points.size > 1) {
                            val strokePath = Path().apply {
                                val firstPoint = path.points.first()
                                moveTo(firstPoint.x, firstPoint.y)
                                path.points.drop(1).forEach { pt ->
                                    lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                color = path.color,
                                style = Stroke(
                                    width = path.strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Draw active drag coordinate line
                    if (currentPathPoints.size > 1) {
                        val strokePath = Path().apply {
                            val firstPoint = currentPathPoints.first()
                            moveTo(firstPoint.x, firstPoint.y)
                            currentPathPoints.drop(1).forEach { pt ->
                                lineTo(pt.x, pt.y)
                            }
                        }
                        drawPath(
                            path = strokePath,
                            color = selectedColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Navigation "التالي والسابق" overlaid on bottom of the canvas area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        SoundEffectsHelper.playPrevious()
                        if (activeItemIndex > 0) {
                            activeItemIndex--
                        } else {
                            activeItemIndex = currentCategoryItems.size - 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("السابق ⬅️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "${activeItemIndex + 1} / ${currentCategoryItems.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001D34)
                )

                Button(
                    onClick = {
                        SoundEffectsHelper.playNext()
                        if (activeItemIndex < currentCategoryItems.size - 1) {
                            activeItemIndex++
                        } else {
                            activeItemIndex = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("التالي ➡️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ================= RIGHT PANEL: PALETTE COLORS & BRUSH =================
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFFEE6EE), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Palette Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "فرشاة الألوان 🎨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF001D34)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Brush thickness badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Pair(10f, "صغير"),
                        Pair(24f, "وسط"),
                        Pair(44f, "كبير")
                    ).forEach { (size, label) ->
                        val isSelected = strokeWidth == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SoftBluePrimary else Color(0xFFF0F4F8))
                                .clickable { strokeWidth = size }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            // Beautiful array of shiny glossy crayons (colors palette)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(colors) { col ->
                    val isSelected = selectedColor == col
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(col)
                            .clickable { selectedColor = col }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color(0xFF001D34) else Color.LightGray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✨", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Control Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Clear Canvas button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .clickable {
                            drawingPaths.clear()
                            currentPathPoints.clear()
                            onSpeak("تم تنظيف اللوحة")
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEA5B5B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مسح 🧼", fontSize = 11.sp, color = Color(0xFFEA5B5B), fontWeight = FontWeight.Bold)
                    }
                }

                // Register Progress in Child Log Book
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .clickable {
                            SoundEffectsHelper.playSuccess()
                            viewModel.logSessionActivity(
                                activityName = "تعلم الكتابة والتتبع",
                                score = 5,
                                notes = "أكمل تتبع الحرف والتمثيل: " + activeItem.title
                            )
                            Toast.makeText(context, "تم حفظ الإنجاز في سجل الجلسات 🏆!", Toast.LENGTH_SHORT).show()
                            onSpeak("ممتاز! تم تسجيل رصد مهارة التتبع والكتابة للطفل")
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("رصد الإنجاز ✓", fontSize = 11.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
