package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*

// Normalized coordinate percents (0.0f to 1.0f) for drawing Dots dynamically
data class OffsetPercent(val x: Float, val y: Float)

data class DotToDotItem(
    val title: String,
    val emoji: String,
    val category: String,
    val prompt: String,
    val points: List<OffsetPercent>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawAndPlayScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()

    // 1. Generate the 100 dot-to-dot images spanning Animals, transport, toys, home, nature
    val itemsList = remember {
        val list = mutableListOf<DotToDotItem>()

        // Helper to generate points on different shapes/archetypes
        fun makeCirclePoints(count: Int, r: Float = 0.35f, cx: Float = 0.5f, cy: Float = 0.5f): List<OffsetPercent> {
            return (0 until count).map { i ->
                val angle = (2 * Math.PI * i / count)
                OffsetPercent(
                    cx + (r * Math.cos(angle)).toFloat(),
                    cy + (r * Math.sin(angle)).toFloat()
                )
            }
        }

        fun makeStarPoints(cx: Float = 0.5f, cy: Float = 0.5f, rOuter: Float = 0.38f, rInner: Float = 0.16f): List<OffsetPercent> {
            val points = mutableListOf<OffsetPercent>()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) rOuter else rInner
                val angle = (Math.PI * i / 5.0) - Math.PI / 2.0
                points.add(
                    OffsetPercent(
                        cx + (r * Math.cos(angle)).toFloat(),
                        cy + (r * Math.sin(angle)).toFloat()
                    )
                )
            }
            return points
        }

        fun makeHousePoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.15f, 0.85f),
                OffsetPercent(0.15f, 0.50f),
                OffsetPercent(0.50f, 0.15f), // Roof peak
                OffsetPercent(0.85f, 0.50f),
                OffsetPercent(0.85f, 0.85f),
                OffsetPercent(0.60f, 0.85f), // Door top-right
                OffsetPercent(0.60f, 0.65f),
                OffsetPercent(0.40f, 0.65f), // Door top-left
                OffsetPercent(0.40f, 0.85f),
                OffsetPercent(0.15f, 0.85f)
            )
        }

        fun makeHeartPoints(): List<OffsetPercent> {
            val pts = mutableListOf<OffsetPercent>()
            val steps = 16
            for (i in 0..steps) {
                val t = (2 * Math.PI * i / steps)
                // parametric heart equation
                val x = (16 * Math.sin(t) * Math.sin(t) * Math.sin(t)).toFloat()
                val y = (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)).toFloat()
                // remap to 0.1 - 0.9 range
                val nx = 0.5f + (x / 17.5f) * 0.38f
                val ny = 0.48f - (y / 17.5f) * 0.38f
                pts.add(OffsetPercent(nx, ny))
            }
            return pts
        }

        fun makeTreePoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.45f, 0.90f),
                OffsetPercent(0.45f, 0.70f),
                OffsetPercent(0.20f, 0.70f), // Bottom foliage left
                OffsetPercent(0.30f, 0.50f),
                OffsetPercent(0.15f, 0.50f),
                OffsetPercent(0.35f, 0.30f),
                OffsetPercent(0.50f, 0.10f), // Peak
                OffsetPercent(0.65f, 0.30f),
                OffsetPercent(0.85f, 0.50f),
                OffsetPercent(0.70f, 0.50f),
                OffsetPercent(0.80f, 0.70f), // Bottom foliage right
                OffsetPercent(0.55f, 0.70f),
                OffsetPercent(0.55f, 0.90f),
                OffsetPercent(0.45f, 0.90f)
            )
        }

        fun makeCarPoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.12f, 0.75f),
                OffsetPercent(0.12f, 0.55f),
                OffsetPercent(0.30f, 0.55f),
                OffsetPercent(0.42f, 0.30f), // Windshield top
                OffsetPercent(0.70f, 0.30f), // Roof back
                OffsetPercent(0.80f, 0.55f), // Rear window
                OffsetPercent(0.92f, 0.55f),
                OffsetPercent(0.92f, 0.75f),
                OffsetPercent(0.78f, 0.75f), // Rear wheel arch
                OffsetPercent(0.78f, 0.66f),
                OffsetPercent(0.64f, 0.66f),
                OffsetPercent(0.64f, 0.75f),
                OffsetPercent(0.36f, 0.75f), // Front wheel arch
                OffsetPercent(0.36f, 0.66f),
                OffsetPercent(0.22f, 0.66f),
                OffsetPercent(0.22f, 0.75f),
                OffsetPercent(0.12f, 0.75f)
            )
        }

        fun makeRocketPoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.50f, 0.10f), // Nose tip
                OffsetPercent(0.65f, 0.35f),
                OffsetPercent(0.65f, 0.75f),
                OffsetPercent(0.80f, 0.85f), // Right fin
                OffsetPercent(0.65f, 0.85f),
                OffsetPercent(0.50f, 0.92f), // Bottom booster
                OffsetPercent(0.35f, 0.85f),
                OffsetPercent(0.20f, 0.85f), // Left fin
                OffsetPercent(0.35f, 0.75f),
                OffsetPercent(0.35f, 0.35f),
                OffsetPercent(0.50f, 0.10f)
            )
        }

        fun makeFishPoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.15f, 0.50f), // Nose
                OffsetPercent(0.35f, 0.25f), // Upper body
                OffsetPercent(0.70f, 0.40f), // Tail base upper
                OffsetPercent(0.88f, 0.22f), // Tail fin top
                OffsetPercent(0.82f, 0.50f), // Tail fin inner
                OffsetPercent(0.88f, 0.78f), // Tail fin bottom
                OffsetPercent(0.70f, 0.60f), // Tail base lower
                OffsetPercent(0.35f, 0.75f), // Lower body
                OffsetPercent(0.15f, 0.50f)
            )
        }

        fun makeButterflyPoints(): List<OffsetPercent> {
            return listOf(
                OffsetPercent(0.50f, 0.30f), // Center body top
                OffsetPercent(0.65f, 0.15f), // Top right wing reach
                OffsetPercent(0.85f, 0.25f),
                OffsetPercent(0.60f, 0.50f), // Wing junction right
                OffsetPercent(0.82f, 0.68f), // Bottom right wing reach
                OffsetPercent(0.60f, 0.78f),
                OffsetPercent(0.50f, 0.70f), // Center body bottom
                OffsetPercent(0.40f, 0.78f),
                OffsetPercent(0.18f, 0.68f), // Bottom left wing reach
                OffsetPercent(0.40f, 0.50f), // Wing junction left
                OffsetPercent(0.15f, 0.25f), // Top left wing reach
                OffsetPercent(0.35f, 0.15f),
                OffsetPercent(0.50f, 0.30f)
            )
        }

        fun makeFlowerPoints(): List<OffsetPercent> {
            // center circle with 8 petals
            val pts = mutableListOf<OffsetPercent>()
            val petals = 8
            for (i in 0 until petals) {
                val angleCent = (2 * Math.PI * i / petals)
                // Inner center point
                pts.add(OffsetPercent(
                    0.5f + (0.12f * Math.cos(angleCent)).toFloat(),
                    0.5f + (0.12f * Math.sin(angleCent)).toFloat()
                ))
                // Outer petal point
                val angleOuter = angleCent + (Math.PI / petals)
                pts.add(OffsetPercent(
                    0.5f + (0.35f * Math.cos(angleCent)).toFloat(),
                    0.5f + (0.35f * Math.sin(angleCent)).toFloat()
                ))
            }
            pts.add(pts.first())
            return pts
        }

        val animals = listOf(
            Triple("أسد", "🦁", "تتبع نقاط ملك الغابة الشجاع"),
            Triple("قطة", "🐱", "ارسم القطة الصغيرة اللطيفة مياو"),
            Triple("أرنب", "🐰", "تتبع قفزات الأرنب الصغير السريع"),
            Triple("فيل", "🐘", "تعلم رسم الفيل الضخم اللطيف وطوقه"),
            Triple("زرافة", "🦒", "صل نقاط الزرافة زوزو طويلة الرقبة"),
            Triple("سمكة", "🐟", "ارسم سمكة تسبح بسعادة في النهر"),
            Triple("فراشة", "🦋", "تتبع جناحي الفراشة الملونة الجميلة"),
            Triple("دب", "🧸", "ارسم الدب اللطيف صديق الأطفال"),
            Triple("بطة", "🦆", "تتبع البطة المائية تعوم في البحيرة"),
            Triple("عصفور", "🐦", "تعلم رسم عصفور صغير يغرد على الغصن"),
            Triple("ثعلب", "🦊", "صل نقاط الثعلب الذكي والماكر"),
            Triple("قرد", "🐵", "ارسم القرد ميمون يحب الموز"),
            Triple("كلب", "🐶", "صل نقاط الكلب المخلص الحارس اللطيف"),
            Triple("سلحفاة", "🐢", "ارسم السلحفاة الحكيمة الهادئة والصبورة"),
            Triple("حلزون", "🐌", "تتبع قوقعة الحلزون البطيء الجميل"),
            Triple("أخطبوط", "🐙", "ارسم الأخطبوط البحري متعدد الأطراف والأيدي"),
            Triple("ضفدع", "🐸", "ارسم الضفدع الأخضر وهو يقفز فرحاً"),
            Triple("سرطان", "🦀", "صل نقاط السرطان البحري يمشي جانباً"),
            Triple("بطريق", "🐧", "تتبع البطريق الجميل فوق الجليد الأبيض"),
            Triple("دولفين", "🐬", "ارسم الدولفين الذكي يقفز فوق الأمواج"),
            Triple("خروف", "🐑", "تتبع خروف العيد الصغير ذو الصوف الأبيض"),
            Triple("بقرة", "🐄", "صل نقاط البقرة الكريمة تعطينا الحليب"),
            Triple("حصان", "🐴", "ارسم الحصان السريع ذو السبيب والسرج"),
            Triple("جمل", "🐫", "صل نقاط سفينة الصحراء الجمل الصبور"),
            Triple("غزال", "🦌", "تتبع نقاط الغزال الرشيق السريع في السهل")
        )

        val transport = listOf(
            Triple("سيارة", "🚗", "تتبع عجلات وهيكل السيارة السريعة"),
            Triple("قطار", "🚂", "صل نقاط القطار يسير على السكة الحديدية"),
            Triple("طائرة", "✈️", "ارسم الطائرة الكبيرة تحلق عالياً في السماء"),
            Triple("صاروخ", "🚀", "تتبع الصاروخ ينطلق بسرعة نحو الفضاء الخارجي"),
            Triple("قارب", "⛵", "ارسم القارب الشراعي يبحر في مياه البحر"),
            Triple("دراجة", "🚲", "صل نقاط الدراجة لممارسة الرياضة والمرح"),
            Triple("حافلة", "🚌", "صل نقاط حافلة المدرسة لنقل الطلاب بأمان"),
            Triple("مروحية", "🚁", "تتبع مروحية الإنقاذ تحوم فوق الجبال"),
            Triple("شاحنة", "🚛", "ارسم الشاحنة الكبيرة تنقل البضائع والمؤن"),
            Triple("منطاد", "🎈", "تتبع طيران المنطاد الملون يعلو فوق السحاب"),
            Triple("سيارة شرطة", "🚓", "صل نقاط سيارة الأمن تحمي المدينة"),
            Triple("سفينة", "🚢", "ارسم السفينة الضخمة تعبر المحيط الهادئ"),
            Triple("غواصة", "🤿", "تتبع الغواصة تستكشف أعماق البحار العميقة")
        )

        val homeAndNature = listOf(
            Triple("شجرة", "🌳", "تتبع ظلال وأوراق الشجرة الكبيرة المعطاءة"),
            Triple("وردة", "🌸", "صل نقاط الوردة الفواحة بأجمل الروائح"),
            Triple("تفاحة", "🍎", "ارسم التفاحة اللذيذة والغنية بالفيتامينات"),
            Triple("موز", "🍌", "صل نقاط الموزة الصفراء الحلوة والمفيدة"),
            Triple("شمس", "☀️", "ارسم الشمس الساطعة تدفئ أرضنا الجميلة"),
            Triple("سحابة", "☁️", "تتبع نقاط السحابة البيضاء تحمل الخير والقطر"),
            Triple("نجمة", "⭐", "ارسم النجمة اللامعة تزين السماء في المساء"),
            Triple("منزل", "🏠", "تتبع وحل نقاط منزلي الدافيء الجميل المعمور"),
            Triple("مظلة", "☂️", "ارسم المظلة الواقية من شمس الصيف ومطر الشتاء"),
            Triple("بالون", "🎈", "صل نقاط بالون الحفلات يطير عالياً في الغرفة"),
            Triple("كعكة", "🎂", "ارسم كعكة عيد الميلاد اللذيذة بالشوكولاتة ومزينة"),
            Triple("طائرة ورقية", "🪁", "تتبع طائرة ورقية ترفرف في الهواء طليقة"),
            Triple("ساعة", "⏰", "صل نقاط الساعة تنظم وقتنا الثمين كل يوم"),
            Triple("كرسي", "🪑", "ارسم الكرسي الخشبي المريح للجلوس والدراسة"),
            Triple("مصباح", "💡", "تتبع مصباح الغرفة ينير عتمة الليل للدراسة"),
            Triple("قبعة", "🎩", "ارسم قبعة التخرج أو الحماية من الشمس الساطعة"),
            Triple("حذاء", "👟", "صل نقاط حذاء الجري الخفيف المريح للقدمين"),
            Triple("كوب", "🥤", "ارسم كوب الحليب الدافئ المغذي في الصباح"),
            Triple("ملعقة", "🥄", "تتبع ملعقة طعام شهي ومغذي من صنع أمي العزيزة"),
            Triple("شوكة", "🍴", "صل نقاط الشوكة والسكين لتناول الغداء المفيد")
        )

        var idxGlobal = 1
        // Generate up to 100 images by repeating the templates with variations
        fun populate(categoryName: String, templates: List<Triple<String, String, String>>) {
            templates.forEach { (title, emoji, prompt) ->
                if (idxGlobal <= 100) {
                    val pts = when {
                        emoji == "🏠" || emoji == "🪑" -> makeHousePoints()
                        emoji == "🌳" || emoji == "🌸" || emoji == "🍎" -> makeTreePoints()
                        emoji == "🚗" || emoji == "🚌" || emoji == "🚂" -> makeCarPoints()
                        emoji == "🚀" || emoji == "✈️" -> makeRocketPoints()
                        emoji == "🐟" || emoji == "🐳" || emoji == "🐙" -> makeFishPoints()
                        emoji == "🦋" || emoji == "🌸" -> makeButterflyPoints()
                        emoji == "⭐" || emoji == "☀️" -> makeStarPoints()
                        emoji == "💖" || emoji == "❤️" || emoji == "🎂" -> makeHeartPoints()
                        else -> {
                            val rMod = 0.25f + (idxGlobal % 3) * 0.05f
                            makeCirclePoints(10 + (idxGlobal % 6), r = rMod)
                        }
                    }
                    list.add(
                        DotToDotItem(
                            title = "$title ($idxGlobal)",
                            emoji = emoji,
                            category = categoryName,
                            prompt = prompt,
                            points = pts
                        )
                    )
                    idxGlobal++
                }
            }
        }

        // Cycle through lists to fill exactly 100 beautiful items
        while (idxGlobal <= 100) {
            populate("حيوانات وطيور 🦁", animals)
            populate("وسائل النقل والألعاب 🚀", transport)
            populate("المنزل والبيئة 🌳", homeAndNature)
        }

        list.take(100)
    }

    var selectedCategory by remember { mutableStateOf("حيوانات وطيور 🦁") }
    val categories = listOf("حيوانات وطيور 🦁", "وسائل النقل والألعاب 🚀", "المنزل والبيئة 🌳")

    val filteredItems = remember(selectedCategory) {
        itemsList.filter { it.category == selectedCategory }
    }

    var activeItemIndex by remember { mutableStateOf(0) }
    val activeItem = remember(filteredItems, activeItemIndex) {
        filteredItems.getOrNull(activeItemIndex % filteredItems.size) ?: filteredItems.first()
    }

    // Tracing drawing state
    val drawingPaths = remember { mutableStateListOf<DrawPath>() }
    val currentPathPoints = remember { mutableStateListOf<Offset>() }

    var selectedColor by remember { mutableStateOf(SoftBluePrimary) }
    var strokeWidth by remember { mutableFloatStateOf(24f) }

    val colors = listOf(
        SoftBluePrimary, PlayfulOrange, MintGreen, SunnyYellow, LovelyPink, SoftViolet, Color.Black, Color.Gray, Color.Red, Color(0xFF10B981)
    )

    // TTS voice assist on changes
    LaunchedEffect(activeItem) {
        onSpeak(activeItem.title.substringBefore(" (") + ". " + activeItem.prompt)
        drawingPaths.clear()
        currentPathPoints.clear()
    }

    LaunchedEffect(selectedCategory) {
        onSpeak("فئة " + selectedCategory)
        activeItemIndex = 0
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        // ================= RIGHT PANEL: 100 IMAGES GRID =================
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFBAE6FD), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Back + Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "ارسم والعب 🎨",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
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

            // Categories list tabs selector
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundEffectsHelper.playClick()
                                selectedCategory = cat
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9)
                        )
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "اختر صورة من الـ 100 لتلوينها 👇",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Grid of items in this category
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(filteredItems) { index, item ->
                    val isSelected = activeItemIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PlayfulOrange else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                SoundEffectsHelper.playClick()
                                activeItemIndex = index
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFFF7ED) else Color(0xFFFAFAFA)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(item.emoji, fontSize = 24.sp)
                            Text(
                                text = item.title.substringBefore(" "),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ================= CENTER CANVAS & DRAWING STATION =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper details instruction card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PlayfulOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activeItem.emoji, fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "تمرين رسم وتلوين: ${activeItem.title}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                "صل النقاط الرمادية لتتعلم رسم الأشكال بمهارة ودقة بصرية",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // TTS audio trigger
                    Button(
                        onClick = { onSpeak(activeItem.title.substringBefore(" (") + ". " + activeItem.prompt) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                        shape = RoundedCornerShape(10.dp),
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

            // Central chalkboard canvas drawing station
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
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
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background educational grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineColor = Color(0xFFE2E8F0)
                    val stepPx = 40.dp.toPx()
                    var y = stepPx
                    while (y < size.height) {
                        drawLine(lineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                        y += stepPx
                    }
                    var x = stepPx
                    while (x < size.width) {
                        drawLine(lineColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                        x += stepPx
                    }
                }

                // Interactive Background Emojis drawn in a gigantic way in light grey
                Text(
                    text = activeItem.emoji,
                    fontSize = 175.sp,
                    modifier = Modifier.alpha(0.12f),
                    textAlign = TextAlign.Center
                )

                // Render Tracing Dot Points on the Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw connections between dots that are grey dashed lines
                    val path = Path()
                    activeItem.points.forEachIndexed { idx, pt ->
                        val px = pt.x * w
                        val py = pt.y * h
                        if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(
                        path = path,
                        color = Color.LightGray.copy(alpha = 0.5f),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw grey circles for dot points, and text indices
                    activeItem.points.forEachIndexed { index, pt ->
                        val px = pt.x * w
                        val py = pt.y * h
                        drawCircle(
                            color = Color(0xFF94A3B8),
                            radius = 12.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // Draw numbers floating on the dots via sub-composed row elements for crisp text layout
                activeItem.points.forEachIndexed { index, pt ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (pt.x * 100).dp, // approximate representation
                                    y = (pt.y * 100).dp  // dynamic absolute math using percent is handled by Canvas drawing numbers
                                )
                        )
                    }
                }

                // Render child completed custom drawing paths
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Previous paths
                    drawingPaths.forEach { drawPath ->
                        val path = Path()
                        drawPath.points.forEachIndexed { idx, pts ->
                            if (idx == 0) path.moveTo(pts.x, pts.y) else path.lineTo(pts.x, pts.y)
                        }
                        drawPath(
                            path = path,
                            color = drawPath.color,
                            style = Stroke(width = drawPath.strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    // Current active path inside drag progress
                    if (currentPathPoints.isNotEmpty()) {
                        val path = Path()
                        currentPathPoints.forEachIndexed { idx, pts ->
                            if (idx == 0) path.moveTo(pts.x, pts.y) else path.lineTo(pts.x, pts.y)
                        }
                        drawPath(
                            path = path,
                            color = selectedColor,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Numbers Floating inside Canvas using simple text overlay relative math
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val canvasW = maxWidth
                    val canvasH = maxHeight
                    activeItem.points.forEachIndexed { index, pt ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (canvasW * pt.x) - 8.dp,
                                    y = (canvasH * pt.y) - 8.dp
                                )
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A).copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lower Command Stations: Colors palette + stroke size + clean board
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color selectors
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    colors.take(7).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .shadow(2.dp, CircleShape)
                                .clickable {
                                    SoundEffectsHelper.playClick()
                                    selectedColor = color
                                }
                        )
                    }
                }

                // Actions: Reset drawings + success action
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Eraser/Clear canvas
                    IconButton(
                        onClick = {
                            SoundEffectsHelper.playClick()
                            drawingPaths.clear()
                            currentPathPoints.clear()
                            Toast.makeText(context, "تم تنظيف لوحة الرسم! 🧼", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFEF2F2), CircleShape)
                            .border(1.dp, Color(0xFFFCA5A5), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تنظيف لوحة الرسم", tint = Color.Red)
                    }

                    // Done/Finish Button
                    Button(
                        onClick = {
                            SoundEffectsHelper.playSuccess()
                            onSpeak("أحسنت يا بطل! رسم وتلوين رائع جداً!")
                            Toast.makeText(context, "إنجاز رائع! حصلت على نجمة الرسم! ⭐🎨", Toast.LENGTH_LONG).show()
                            drawingPaths.clear()
                            currentPathPoints.clear()
                            if (activeItemIndex < filteredItems.size - 1) {
                                activeItemIndex++
                            } else {
                                activeItemIndex = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("تم بنجاح! ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
