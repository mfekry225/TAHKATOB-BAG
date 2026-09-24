package com.example.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.sqrt

// Maze level model
data class MazeLevel(
    val id: String,
    val name: String,
    val startEmoji: String,
    val endEmoji: String,
    val targetMessage: String,
    // Track segments defined as relative ratios (0.0 to 1.0) of canvas width/height
    val trackPoints: List<Offset>,
    val tolerance: Float = 0.08f // how far the finger can stray relative to screen size
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMazeScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val activeChild by viewModel.activeChild.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    // Diff levels
    val mazeLevels = remember {
        listOf(
            MazeLevel(
                id = "easy",
                name = "سهل (خط مستقيم) 🚗🏠",
                startEmoji = "🚗",
                endEmoji = "🏠",
                targetMessage = "ساعد السيارة لتصل إلى المنزل!",
                trackPoints = listOf(
                    Offset(0.15f, 0.5f),
                    Offset(0.85f, 0.5f)
                ),
                tolerance = 0.12f
            ),
            MazeLevel(
                id = "medium",
                name = "متوسط (ممر متعرج) 🐝🌸",
                startEmoji = "🐝",
                endEmoji = "🌸",
                targetMessage = "وجه النحلة اللطيفة إلى الوردة الملونة!",
                trackPoints = listOf(
                    Offset(0.15f, 0.25f),
                    Offset(0.5f, 0.25f),
                    Offset(0.5f, 0.75f),
                    Offset(0.85f, 0.75f)
                ),
                tolerance = 0.09f
            ),
            MazeLevel(
                id = "hard",
                name = "صعب (متاهة الجبال) 🐒🍌",
                startEmoji = "🐒",
                endEmoji = "🍌",
                targetMessage = "ساعد القرد الشقي للوصول إلى الموز اللذيذ!",
                trackPoints = listOf(
                    Offset(0.15f, 0.15f),
                    Offset(0.15f, 0.5f),
                    Offset(0.5f, 0.5f),
                    Offset(0.5f, 0.15f),
                    Offset(0.85f, 0.15f),
                    Offset(0.85f, 0.85f)
                ),
                tolerance = 0.07f
            )
        )
    }

    var currentLevelIndex by remember { mutableStateOf(0) }
    val level = mazeLevels[currentLevelIndex]

    // Touch and path tracking
    var userPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var collisionActive by remember { mutableStateOf(false) }
    var isDraggingStarted by remember { mutableStateOf(false) }
    var movesCount by remember { mutableStateOf(0) }
    var showVictoryOverlay by remember { mutableStateOf(false) }

    // Sky back brush
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9), // Mint green
            Color(0xFFFFF9C4), // Sunlight
            Color(0xFFFFECB3)  // Orange sky transition
        )
    )

    // Helper: Distance from a point to a line segment
    fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return sqrt((p.x - a.x) * (p.x - a.x) + (p.y - a.y) * (p.y - a.y))
        var t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
        t = t.coerceIn(0f, 1f)
        val projection = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
        return sqrt((p.x - projection.x) * (p.x - projection.x) + (p.y - projection.y) * (p.y - projection.y))
    }

    // Helper: Check if a point is within tolerance of the allowed multi-segment track
    fun isTouchValidOnTrack(touch: Offset, track: List<Offset>, canvasWidth: Float, canvasHeight: Float): Boolean {
        if (track.size < 2) return false
        var minDistance = Float.MAX_VALUE

        // Convert track ratio offsets to actual pixel coordinates
        val pixelTrack = track.map { Offset(it.x * canvasWidth, it.y * canvasHeight) }

        for (i in 0 until pixelTrack.size - 1) {
            val dist = distanceToSegment(touch, pixelTrack[i], pixelTrack[i + 1])
            if (dist < minDistance) {
                minDistance = dist
            }
        }

        // Relative threshold based on the average screen size
        val maxAllowedDistance = level.tolerance * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))
        return minDistance <= maxAllowedDistance
    }

    // Reset Level state
    val resetLevel = {
        userPathPoints = emptyList()
        collisionActive = false
        isDraggingStarted = false
        movesCount++
        showVictoryOverlay = false
        onSpeak("ابتدئ من عند رمز البداية: ${level.startEmoji}، وحرك إصبعك بحذر!")
    }

    // Play TTS on load
    LaunchedEffect(currentLevelIndex) {
        resetLevel()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "المتاهات الذكية 🧩",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E3A1E)
                        )
                        Text(
                            "التحكم الحركي الدقيق والتوافق البصري",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape)
                    ) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.5f))
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column (Controls & Info)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        activeChild?.let { child ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(child.avatarColor).copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(child.avatarColor)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "النشط: ${child.name} 🌟",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        // Level Difficulty Switchers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mazeLevels.forEachIndexed { idx, item ->
                                val isSel = idx == currentLevelIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) Color(0xFF1E3A1E) else Color.White)
                                        .border(1.dp, if (isSel) Color(0xFF1E3A1E) else Color.LightGray, RoundedCornerShape(10.dp))
                                        .clickable { currentLevelIndex = idx }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.name.split(" ")[0],
                                        color = if (isSel) Color.White else Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Interactive info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1E3A1E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = level.targetMessage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        // Controls
                        Button(
                            onClick = { resetLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("البدء من جديد", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                currentLevelIndex = (currentLevelIndex + 1) % mazeLevels.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text("المتاهة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Right Column (Maze Play Area)
                    Box(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(3.dp, if (collisionActive) Color.Red else Color(0xFFDCDFD8), RoundedCornerShape(24.dp))
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = constraints.maxWidth.toFloat()
                            val canvasHeight = constraints.maxHeight.toFloat()

                            // Calculate absolute coordinates for Emojis placement
                            val pxTrack = level.trackPoints.map { Offset(it.x * canvasWidth, it.y * canvasHeight) }
                            val startPos = pxTrack.first()
                            val endPos = pxTrack.last()

                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(level.id) {
                                        detectDragGestures(
                                            onDragStart = { startOffset ->
                                                val distToStart = sqrt(
                                                    (startOffset.x - startPos.x) * (startOffset.x - startPos.x) +
                                                    (startOffset.y - startPos.y) * (startOffset.y - startPos.y)
                                                )
                                                val tolerancePx = 0.15f * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))

                                                if (distToStart <= tolerancePx) {
                                                    isDraggingStarted = true
                                                    collisionActive = false
                                                    userPathPoints = listOf(startOffset)
                                                    SoundEffectsHelper.playClick()
                                                } else {
                                                    isDraggingStarted = false
                                                    Toast.makeText(context, "ابدأ من عند رمز البداية! 🏁", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (!isDraggingStarted || showVictoryOverlay) return@detectDragGestures

                                                val currentPoint = change.position
                                                userPathPoints = userPathPoints + currentPoint

                                                val isValid = isTouchValidOnTrack(currentPoint, level.trackPoints, canvasWidth, canvasHeight)
                                                if (!isValid) {
                                                    collisionActive = true
                                                    isDraggingStarted = false
                                                    SoundEffectsHelper.playFailure()

                                                    if (vibrator != null && vibrator.hasVibrator()) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                                                        } else {
                                                            @Suppress("DEPRECATION")
                                                            vibrator.vibrate(150)
                                                        }
                                                    }

                                                    Toast.makeText(context, "اصطدام بالجدار! حاول ثانية يا بطل", Toast.LENGTH_SHORT).show()
                                                    userPathPoints = emptyList()
                                                } else {
                                                    val distToEnd = sqrt(
                                                        (currentPoint.x - endPos.x) * (currentPoint.x - endPos.x) +
                                                        (currentPoint.y - endPos.y) * (currentPoint.y - endPos.y)
                                                    )
                                                    val winTolerance = 0.08f * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))

                                                    if (distToEnd <= winTolerance) {
                                                        showVictoryOverlay = true
                                                        isDraggingStarted = false
                                                        SoundEffectsHelper.playSuccess()

                                                        if (activeChild != null) {
                                                            viewModel.logSessionActivity(
                                                                activityName = "المتاهة الذكية: ${level.name}",
                                                                score = when (level.id) {
                                                                    "easy" -> 20
                                                                    "medium" -> 35
                                                                    else -> 50
                                                                },
                                                                notes = "أكمل الطفل المتاهة بنجاح بنسبة ثبات ممتازة. فئة المتاهة: ${level.name} بعدد محاولات: $movesCount"
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                isDraggingStarted = false
                                            }
                                        )
                                    }
                            ) {
                                val roadWidth = level.tolerance * sqrt((size.width * size.width) + (size.height * size.height))
                                val roadPath = Path().apply {
                                    if (pxTrack.isNotEmpty()) {
                                        moveTo(pxTrack[0].x, pxTrack[0].y)
                                        for (i in 1 until pxTrack.size) {
                                            lineTo(pxTrack[i].x, pxTrack[i].y)
                                        }
                                    }
                                }

                                drawPath(
                                    path = roadPath,
                                    color = Color(0xFFECEFF1),
                                    style = Stroke(width = roadWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                drawPath(
                                    path = roadPath,
                                    color = Color(0xFFCFD8DC),
                                    style = Stroke(width = roadWidth * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                drawPath(
                                    path = roadPath,
                                    color = Color.White,
                                    style = Stroke(width = roadWidth * 0.95f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                if (userPathPoints.size > 1) {
                                    val userTracePath = Path().apply {
                                        moveTo(userPathPoints[0].x, userPathPoints[0].y)
                                        for (i in 1 until userPathPoints.size) {
                                            lineTo(userPathPoints[i].x, userPathPoints[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = userTracePath,
                                        color = if (collisionActive) Color.Red else Color(0xFF4CAF50),
                                        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val sizeDp = with(density) {
                                val roadW = level.tolerance * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))
                                (roadW * 0.7f).toDp().coerceAtLeast(40.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { startPos.x.toDp() - (sizeDp / 2) },
                                        y = with(density) { startPos.y.toDp() - (sizeDp / 2) }
                                    )
                                    .size(sizeDp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .border(2.dp, Color(0xFF2196F3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(level.startEmoji, fontSize = (sizeDp.value * 0.5f).sp)
                            }

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { endPos.x.toDp() - (sizeDp / 2) },
                                        y = with(density) { endPos.y.toDp() - (sizeDp / 2) }
                                    )
                                    .size(sizeDp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9))
                                    .border(2.dp, Color(0xFF4CAF50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(level.endEmoji, fontSize = (sizeDp.value * 0.5f).sp)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 650.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Active Child details
                    activeChild?.let { child ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(child.avatarColor).copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(child.avatarColor)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "الطفل النشط: ${child.name} 🌟",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    // Level Difficulty Switchers
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mazeLevels.forEachIndexed { idx, item ->
                            val isSel = idx == currentLevelIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color(0xFF1E3A1E) else Color.White)
                                    .border(1.5.dp, if (isSel) Color(0xFF1E3A1E) else Color.LightGray, RoundedCornerShape(12.dp))
                                    .clickable { currentLevelIndex = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.name.split(" ")[0], // Easy / Med / Hard name display
                                    color = if (isSel) Color.White else Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Interactive info card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1E3A1E))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = level.targetMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    // Play Area: The interactive Canvas Maze!
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(3.dp, if (collisionActive) Color.Red else Color(0xFFDCDFD8), RoundedCornerShape(24.dp))
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = constraints.maxWidth.toFloat()
                            val canvasHeight = constraints.maxHeight.toFloat()

                            // Calculate absolute coordinates for Emojis placement
                            val pxTrack = level.trackPoints.map { Offset(it.x * canvasWidth, it.y * canvasHeight) }
                            val startPos = pxTrack.first()
                            val endPos = pxTrack.last()

                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(level.id) {
                                        detectDragGestures(
                                            onDragStart = { startOffset ->
                                                // Ensure dragging starts close to the start emoji to maintain proper order
                                                val distToStart = sqrt(
                                                    (startOffset.x - startPos.x) * (startOffset.x - startPos.x) +
                                                    (startOffset.y - startPos.y) * (startOffset.y - startPos.y)
                                                )
                                                val tolerancePx = 0.15f * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))

                                                if (distToStart <= tolerancePx) {
                                                    isDraggingStarted = true
                                                    collisionActive = false
                                                    userPathPoints = listOf(startOffset)
                                                    SoundEffectsHelper.playClick()
                                                } else {
                                                    isDraggingStarted = false
                                                    Toast.makeText(context, "ابدأ من عند رمز البداية! 🏁", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (!isDraggingStarted || showVictoryOverlay) return@detectDragGestures

                                                val currentPoint = change.position
                                                userPathPoints = userPathPoints + currentPoint

                                                // Validate trace path with collision boundaries
                                                val isValid = isTouchValidOnTrack(currentPoint, level.trackPoints, canvasWidth, canvasHeight)
                                                if (!isValid) {
                                                    // Stray collision detected!
                                                    collisionActive = true
                                                    isDraggingStarted = false
                                                    SoundEffectsHelper.playFailure()

                                                    // Trigger hardware vibration if possible
                                                    if (vibrator != null && vibrator.hasVibrator()) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                                                        } else {
                                                            @Suppress("DEPRECATION")
                                                            vibrator.vibrate(150)
                                                        }
                                                    }

                                                    Toast.makeText(context, "اصطدام بالجدار! حاول ثانية يا بطل", Toast.LENGTH_SHORT).show()
                                                    userPathPoints = emptyList()
                                                } else {
                                                    // Smooth progress. Check if we reached the exit destination!
                                                    val distToEnd = sqrt(
                                                        (currentPoint.x - endPos.x) * (currentPoint.x - endPos.x) +
                                                        (currentPoint.y - endPos.y) * (currentPoint.y - endPos.y)
                                                    )
                                                    val winTolerance = 0.08f * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))

                                                    if (distToEnd <= winTolerance) {
                                                        // Reached ending successfully!
                                                        showVictoryOverlay = true
                                                        isDraggingStarted = false
                                                        SoundEffectsHelper.playSuccess()

                                                        // Log the child achievements in Room DB
                                                        if (activeChild != null) {
                                                            viewModel.logSessionActivity(
                                                                activityName = "المتاهة الذكية: ${level.name}",
                                                                score = when (level.id) {
                                                                    "easy" -> 20
                                                                    "medium" -> 35
                                                                    else -> 50
                                                                },
                                                                notes = "أكمل الطفل المتاهة بنجاح بنسبة ثبات ممتازة. فئة المتاهة: ${level.name} بعدد محاولات: $movesCount"
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                isDraggingStarted = false
                                            }
                                        )
                                    }
                            ) {
                                // 1. Draw Maze Track Background (The safe road)
                                val roadWidth = level.tolerance * sqrt((size.width * size.width) + (size.height * size.height))
                                val roadPath = Path().apply {
                                    if (pxTrack.isNotEmpty()) {
                                        moveTo(pxTrack[0].x, pxTrack[0].y)
                                        for (i in 1 until pxTrack.size) {
                                            lineTo(pxTrack[i].x, pxTrack[i].y)
                                        }
                                    }
                                }

                                // Draw outline/borders of track
                                drawPath(
                                    path = roadPath,
                                    color = Color(0xFFECEFF1),
                                    style = Stroke(width = roadWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                drawPath(
                                    path = roadPath,
                                    color = Color(0xFFCFD8DC),
                                    style = Stroke(width = roadWidth * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                drawPath(
                                    path = roadPath,
                                    color = Color.White,
                                    style = Stroke(width = roadWidth * 0.95f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                // 2. Draw user trace points
                                if (userPathPoints.size > 1) {
                                    val userTracePath = Path().apply {
                                        moveTo(userPathPoints[0].x, userPathPoints[0].y)
                                        for (i in 1 until userPathPoints.size) {
                                            lineTo(userPathPoints[i].x, userPathPoints[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = userTracePath,
                                        color = if (collisionActive) Color.Red else Color(0xFF4CAF50),
                                        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            // Overlay Emojis perfectly positioned at the start and end of path
                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val sizeDp = with(density) {
                                val roadW = level.tolerance * sqrt((canvasWidth * canvasWidth) + (canvasHeight * canvasHeight))
                                (roadW * 0.7f).toDp().coerceAtLeast(48.dp)
                            }

                            // Start Emoji
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { startPos.x.toDp() - (sizeDp / 2) },
                                        y = with(density) { startPos.y.toDp() - (sizeDp / 2) }
                                    )
                                    .size(sizeDp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .border(2.dp, Color(0xFF2196F3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(level.startEmoji, fontSize = (sizeDp.value * 0.5f).sp)
                            }

                            // End Emoji
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { endPos.x.toDp() - (sizeDp / 2) },
                                        y = with(density) { endPos.y.toDp() - (sizeDp / 2) }
                                    )
                                    .size(sizeDp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9))
                                    .border(2.dp, Color(0xFF4CAF50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(level.endEmoji, fontSize = (sizeDp.value * 0.5f).sp)
                            }
                        }
                    }

                    // Control panel bottom buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { resetLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("البدء من جديد", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                currentLevelIndex = (currentLevelIndex + 1) % mazeLevels.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("المتاهة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // High polish Victory popup overlay
            AnimatedVisibility(
                visible = showVictoryOverlay,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxHeight(0.85f)
                            .padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                Text("🎉🏆", fontSize = 48.sp)
                                Text(
                                    text = "بطل المتاهات! 🌟",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "أحسنت صنعاً! لقد تمكنت من توجيه ${level.startEmoji} بنجاح رائع إلى ${level.endEmoji} بدون الاصطدام بالحواجز!",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    // Go to next level
                                    currentLevelIndex = (currentLevelIndex + 1) % mazeLevels.size
                                    showVictoryOverlay = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "المتاهة التالية 🌟",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
