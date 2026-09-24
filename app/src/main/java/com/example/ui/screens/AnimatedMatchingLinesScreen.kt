package com.example.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
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

// Matching item model
data class MatchingItem(
    val id: Int,
    val display: String, // Emoji or object
    val matchWord: String // Arabic description
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedMatchingLinesScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeChild by viewModel.activeChild.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    // Match sets lists
    val matchingPacks = remember {
        listOf(
            listOf(
                MatchingItem(1, "🍎", "تفاحة"),
                MatchingItem(2, "🦁", "أسد"),
                MatchingItem(3, "🚗", "سيارة"),
                MatchingItem(4, "🐟", "سمكة")
            ),
            listOf(
                MatchingItem(5, "🥛", "حليب"),
                MatchingItem(6, "⚽", "كرة"),
                MatchingItem(7, "🍌", "موز"),
                MatchingItem(8, "✈️", "طائرة")
            ),
            listOf(
                MatchingItem(9, "🐱", "قطة"),
                MatchingItem(10, "🥕", "جزر"),
                MatchingItem(11, "🏠", "منزل"),
                MatchingItem(12, "🌞", "شمس")
            )
        )
    }

    var packIndex by remember { mutableStateOf(0) }
    val currentPack = matchingPacks[packIndex]

    // Shuffled left and right items lists
    var leftItems by remember { mutableStateOf<List<MatchingItem>>(emptyList()) }
    var rightItems by remember { mutableStateOf<List<MatchingItem>>(emptyList()) }

    // Coordinates of items mapped by item ID to compute lines center positions
    val leftPositions = remember { mutableStateMapOf<Int, Offset>() }
    val rightPositions = remember { mutableStateMapOf<Int, Offset>() }

    // Connected pairs IDs
    var connectedPairs by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    // Active Drag tracking states
    var draggingLeftId by remember { mutableStateOf<Int?>(null) }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    var movesCount by remember { mutableStateOf(0) }
    var showVictoryOverlay by remember { mutableStateOf(false) }

    // Infinite pulsing visual transitions for sensory guidance
    val infiniteTransition = rememberInfiniteTransition(label = "sensory_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Cosmic sky background
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE0F7FA), // Soft turquoise sky
            Color(0xFFFFF9C4), // Smooth yellow sun
            Color(0xFFFFD54F).copy(alpha = 0.4f)
        )
    )

    // Generator level shuffle items
    val startPack = {
        leftItems = currentPack.shuffled()
        rightItems = currentPack.shuffled()
        connectedPairs = emptySet()
        draggingLeftId = null
        currentDragPosition = null
        showVictoryOverlay = false
        onSpeak("صل كل مجسم بالكلمة المناسبة له بسحب الخط بدقة!")
    }

    // Load initial pack
    LaunchedEffect(packIndex) {
        startPack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "التوصيل المتحرك 🔗",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF006064)
                        )
                        Text(
                            "التطابق المفهومي والتآزر الحركي البصري",
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
                            .weight(1.1f)
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

                        // Help/Instructions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "اسحب خطاً من العناصر اليمين (الصور) لتوصيلها بالكلمات الصحيحة في اليسار!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006064),
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Buttons
                        Button(
                            onClick = { startPack() },
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
                                packIndex = (packIndex + 1) % matchingPacks.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text("المجموعة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Right Column (Matching Space)
                    Box(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.5.dp, Color(0xFFB2EBF2), RoundedCornerShape(24.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            connectedPairs.forEach { (lId, rId) ->
                                val start = leftPositions[lId]
                                val end = rightPositions[rId]
                                if (start != null && end != null) {
                                    drawLine(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                        start = start,
                                        end = end,
                                        strokeWidth = 20f
                                    )
                                    drawLine(
                                        color = Color(0xFF4CAF50),
                                        start = start,
                                        end = end,
                                        strokeWidth = 8f
                                    )
                                }
                            }

                            val dId = draggingLeftId
                            val dragPos = currentDragPosition
                            if (dId != null && dragPos != null) {
                                val start = leftPositions[dId]
                                if (start != null) {
                                    drawLine(
                                        color = Color(0xFF00BCD4).copy(alpha = 0.3f),
                                        start = start,
                                        end = dragPos,
                                        strokeWidth = 20f
                                    )
                                    drawLine(
                                        color = Color(0xFF00BCD4),
                                        start = start,
                                        end = dragPos,
                                        strokeWidth = 6f
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1 (Left: Images)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                leftItems.forEach { item ->
                                    val isConnected = connectedPairs.any { it.first == item.id }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .scale(if (isConnected) 1f else pulseScale)
                                            .onGloballyPositioned { layoutCoordinates ->
                                                val position = layoutCoordinates.positionInWindow()
                                                val size = layoutCoordinates.size
                                                leftPositions[item.id] = Offset(
                                                    position.x + size.width / 2f,
                                                    position.y + size.height / 2f
                                                )
                                            }
                                            .pointerInput(item.id, isConnected) {
                                                if (isConnected) return@pointerInput
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        draggingLeftId = item.id
                                                        currentDragPosition = leftPositions[item.id]
                                                        SoundEffectsHelper.playClick()
                                                        onSpeak(item.display)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        if (draggingLeftId == item.id) {
                                                            val currentPos = currentDragPosition ?: Offset.Zero
                                                            currentDragPosition = Offset(
                                                                currentPos.x + dragAmount.x,
                                                                currentPos.y + dragAmount.y
                                                            )
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        val dragRelease = currentDragPosition
                                                        var matchFound = false

                                                        if (dragRelease != null && draggingLeftId == item.id) {
                                                            rightPositions.forEach { (rId, rOffset) ->
                                                                if (rId == item.id) {
                                                                    val distance = sqrt(
                                                                        (dragRelease.x - rOffset.x) * (dragRelease.x - rOffset.x) +
                                                                        (dragRelease.y - rOffset.y) * (dragRelease.y - rOffset.y)
                                                                    )
                                                                    if (distance <= 120f) {
                                                                        connectedPairs = connectedPairs + Pair(item.id, rId)
                                                                        matchFound = true
                                                                        SoundEffectsHelper.playSuccess()
                                                                        onSpeak("ممتاز! ${item.matchWord}")

                                                                        if (connectedPairs.size == leftItems.size) {
                                                                            showVictoryOverlay = true
                                                                            if (activeChild != null) {
                                                                                viewModel.logSessionActivity(
                                                                                    activityName = "التوصيل المتحرك: المجموعة ${packIndex + 1}",
                                                                                    score = 40,
                                                                                    notes = "طابق الطفل بنجاح كامل جميع الأشكال وظلالها اللغوية في التوصيل المتحرك مع محاولات متعددة."
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        if (!matchFound) {
                                                            SoundEffectsHelper.playFailure()
                                                            Toast.makeText(context, "توصيل غير صحيح! حاول مرة أخرى", Toast.LENGTH_SHORT).show()
                                                        }

                                                        draggingLeftId = null
                                                        currentDragPosition = null
                                                        movesCount++
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isConnected) Color(0xFFDCFCE7) else Color(0xFFE0F7FA))
                                            .border(
                                                1.5.dp,
                                                if (isConnected) Color(0xFF4CAF50) else Color(0xFF00ACC1),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.display, fontSize = 42.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            // Column 2 (Right: Words)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                rightItems.forEach { item ->
                                    val isConnected = connectedPairs.any { it.second == item.id }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { layoutCoordinates ->
                                                val position = layoutCoordinates.positionInWindow()
                                                val size = layoutCoordinates.size
                                                rightPositions[item.id] = Offset(
                                                    position.x + size.width / 2f,
                                                    position.y + size.height / 2f
                                                )
                                            }
                                            .clickable { onSpeak(item.matchWord) }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isConnected) Color(0xFFDCFCE7) else Color.White)
                                            .border(
                                                1.5.dp,
                                                if (isConnected) Color(0xFF4CAF50) else Color(0xFFECEFF1),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.matchWord,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isConnected) Color(0xFF2E7D32) else Color(0xFF37474F)
                                        )
                                    }
                                }
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
                    // Active Child Details
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

                    // Help/Instructions
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "اسحب خطاً من العناصر اليمين (الصور) لتوصيلها بالكلمات الصحيحة في اليسار!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006064),
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Play Match Space: Left/Right Column with connector Canvas overlay!
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.5.dp, Color(0xFFB2EBF2), RoundedCornerShape(24.dp))
                    ) {
                        // Canvas layer for lines drawing
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // 1. Draw already connected pairs
                            connectedPairs.forEach { (lId, rId) ->
                                val start = leftPositions[lId]
                                val end = rightPositions[rId]
                                if (start != null && end != null) {
                                    // Draw a thick glowing green success line
                                    drawLine(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                        start = start,
                                        end = end,
                                        strokeWidth = 24f
                                    )
                                    drawLine(
                                        color = Color(0xFF4CAF50),
                                        start = start,
                                        end = end,
                                        strokeWidth = 10f
                                    )
                                }
                            }

                            // 2. Draw current active drag line
                            val dId = draggingLeftId
                            val dragPos = currentDragPosition
                            if (dId != null && dragPos != null) {
                                val start = leftPositions[dId]
                                if (start != null) {
                                    // Glowing turquoise connecting line
                                    drawLine(
                                        color = Color(0xFF00BCD4).copy(alpha = 0.3f),
                                        start = start,
                                        end = dragPos,
                                        strokeWidth = 24f
                                    )
                                    drawLine(
                                        color = Color(0xFF00BCD4),
                                        start = start,
                                        end = dragPos,
                                        strokeWidth = 8f
                                    )
                                }
                            }
                        }

                        // Columns layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // COLUMN 1 (Left Items: Images/Emojis)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                leftItems.forEach { item ->
                                    val isConnected = connectedPairs.any { it.first == item.id }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .scale(if (isConnected) 1f else pulseScale)
                                            .onGloballyPositioned { layoutCoordinates ->
                                                // Save center coordinate in Local state coordinate space
                                                val position = layoutCoordinates.positionInWindow()
                                                val size = layoutCoordinates.size
                                                leftPositions[item.id] = Offset(
                                                    position.x + size.width / 2f,
                                                    position.y + size.height / 2f
                                                )
                                            }
                                            .pointerInput(item.id, isConnected) {
                                                if (isConnected) return@pointerInput
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        draggingLeftId = item.id
                                                        currentDragPosition = leftPositions[item.id]
                                                        SoundEffectsHelper.playClick()
                                                        onSpeak(item.display)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        if (draggingLeftId == item.id) {
                                                            val currentPos = currentDragPosition ?: Offset.Zero
                                                            currentDragPosition = Offset(
                                                                currentPos.x + dragAmount.x,
                                                                currentPos.y + dragAmount.y
                                                            )
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        val dragRelease = currentDragPosition
                                                        var matchFound = false

                                                        if (dragRelease != null && draggingLeftId == item.id) {
                                                            // Look if release is close to any correct matching word in the right positions
                                                            rightPositions.forEach { (rId, rOffset) ->
                                                                if (rId == item.id) { // Match correct pair logic
                                                                    val distance = sqrt(
                                                                        (dragRelease.x - rOffset.x) * (dragRelease.x - rOffset.x) +
                                                                        (dragRelease.y - rOffset.y) * (dragRelease.y - rOffset.y)
                                                                    )

                                                                    // If release coordinate is within 60dp radius of target center
                                                                    if (distance <= 150f) {
                                                                        connectedPairs = connectedPairs + Pair(item.id, rId)
                                                                        matchFound = true
                                                                        SoundEffectsHelper.playSuccess()

                                                                        // Play congrats word
                                                                        onSpeak("ممتاز! ${item.matchWord}")

                                                                        // Check if level completed (4 pairs matched)
                                                                        if (connectedPairs.size == leftItems.size) {
                                                                            showVictoryOverlay = true

                                                                            // Persist user log
                                                                            if (activeChild != null) {
                                                                                viewModel.logSessionActivity(
                                                                                    activityName = "التوصيل المتحرك: المجموعة ${packIndex + 1}",
                                                                                    score = 40,
                                                                                    notes = "طابق الطفل بنجاح كامل جميع الأشكال وظلالها اللغوية في التوصيل المتحرك مع محاولات متعددة."
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        if (!matchFound) {
                                                            SoundEffectsHelper.playFailure()
                                                            Toast.makeText(context, "توصيل غير صحيح! حاول مرة أخرى", Toast.LENGTH_SHORT).show()
                                                        }

                                                        draggingLeftId = null
                                                        currentDragPosition = null
                                                        movesCount++
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isConnected) Color(0xFFDCFCE7) else Color(0xFFE0F7FA))
                                            .border(
                                                2.dp,
                                                if (isConnected) Color(0xFF4CAF50) else Color(0xFF00ACC1),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.display, fontSize = 42.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(48.dp))

                            // COLUMN 2 (Right Items: Text Words)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                rightItems.forEach { item ->
                                    val isConnected = connectedPairs.any { it.second == item.id }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { layoutCoordinates ->
                                                val position = layoutCoordinates.positionInWindow()
                                                val size = layoutCoordinates.size
                                                rightPositions[item.id] = Offset(
                                                    position.x + size.width / 2f,
                                                    position.y + size.height / 2f
                                                )
                                            }
                                            .clickable { onSpeak(item.matchWord) }
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isConnected) Color(0xFFDCFCE7) else Color.White)
                                            .border(
                                                2.dp,
                                                if (isConnected) Color(0xFF4CAF50) else Color(0xFFECEFF1),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.matchWord,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isConnected) Color(0xFF2E7D32) else Color(0xFF37474F)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Restart and next button panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startPack() },
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
                                packIndex = (packIndex + 1) % matchingPacks.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("المجموعة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Confetti Victory Overlays
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
                                Text("🎉🥳🤝", fontSize = 48.sp)
                                Text(
                                    text = "توصيل مذهل! 🌟",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "لقد نجحت في توصيل جميع المجسمات بظلالها والكلمات اللغوية الصحيحة بذكاء وتألق!",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    // Move to next set pack
                                    packIndex = (packIndex + 1) % matchingPacks.size
                                    showVictoryOverlay = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "المجموعة التالية ➡️",
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
