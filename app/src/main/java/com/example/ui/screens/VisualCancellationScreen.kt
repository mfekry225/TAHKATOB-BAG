package com.example.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Category configuration model
data class CancellationCategory(
    val id: String,
    val displayName: String,
    val items: List<String>,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualCancellationScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeChild by viewModel.activeChild.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    // Game categories
    val categories = remember {
        listOf(
            CancellationCategory(
                id = "arabic",
                displayName = "الحروف العربية",
                items = listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ذ", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق", "ك", "ل", "م", "ن", "هـ", "و", "ي"),
                description = "ابحث عن الحرف العربي المستهدف واشطبه!"
            ),
            CancellationCategory(
                id = "animals",
                displayName = "الحيوانات اللطيفة 🦁",
                items = listOf("🦁", "🐯", "🐰", "🦊", "🐻", "🐼", "🐨", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉"),
                description = "صيد جميع حيوانات الفئة المستهدفة!"
            ),
            CancellationCategory(
                id = "numbers",
                displayName = "الأرقام 🔢",
                items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩"),
                description = "اعثر على الرقم المتطابق واشطبه!"
            ),
            CancellationCategory(
                id = "colors",
                displayName = "الألوان 🔴",
                items = listOf("🔴", "🔵", "🟢", "🟡", "🟠", "🟣", "⚫", "⚪", "🟤", "💛", "💙", "💚", "❤️"),
                description = "اختر الألوان المتطابقة مع الرمز المستهدف!"
            ),
            CancellationCategory(
                id = "shapes",
                displayName = "الأشكال الهندسية",
                items = listOf("📐", "📏", "🟥", "🟧", "🟡", "🟩", "🔷", "🔺", "⭐", "🌙", "🧿", "🛑", "💠"),
                description = "حدد الشكل الهندسي المطابق!"
            ),
            CancellationCategory(
                id = "directions",
                displayName = "الأسهم والاتجاهات",
                items = listOf("⬆️", "⬇️", "⬅️", "➡️", "↗️", "↘️", "↖️", "↙️"),
                description = "تتبع السهم الذي يشير إلى نفس الاتجاه!"
            ),
            CancellationCategory(
                id = "emojis",
                displayName = "الوجوه التعبيرية",
                items = listOf("😀", "😂", "🥰", "😎", "🤔", "😮", "😴", "🥳", "🤠", "👽", "👻", "🤖", "🌟", "👾"),
                description = "ابحث عن تعبير الوجه المطابق تماماً!"
            ),
            CancellationCategory(
                id = "english",
                displayName = "الحروف الإنجليزية",
                items = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"),
                description = "ابحث عن الحرف الإنجليزي الصحيح!"
            )
        )
    }

    var selectedCatIndex by remember { mutableStateOf(0) }
    val currentCategory = categories[selectedCatIndex]

    // Game Session states
    var targetSymbol by remember { mutableStateOf("") }
    var gridItems by remember { mutableStateOf<List<String>>(emptyList()) }
    var clickedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var wrongClicksCount by remember { mutableStateOf(0) }
    var showVictoryOverlay by remember { mutableStateOf(false) }

    // Floating sky background
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD), // Soft baby blue
            Color(0xFFFFF9C4), // Warm sunlight yellow
            Color(0xFFE8F5E9)  // Smooth mint grass
        )
    )

    // Generator function for the worksheet grid
    val generateNewLevel = {
        val cat = categories[selectedCatIndex]
        if (cat.items.isNotEmpty()) {
            val target = cat.items.random()
            targetSymbol = target

            // Generate 24 items: 6 to 8 targets, and the rest from other symbols
            val targetCount = (5..8).random()
            val tempList = mutableListOf<String>()
            repeat(targetCount) { tempList.add(target) }

            val otherItems = cat.items.filter { it != target }
            val fillersNeeded = 24 - targetCount
            repeat(fillersNeeded) {
                if (otherItems.isNotEmpty()) {
                    tempList.add(otherItems.random())
                } else {
                    tempList.add(targetSymbol)
                }
            }
            tempList.shuffle()
            gridItems = tempList
            clickedIndices = emptySet()
            wrongClicksCount = 0
            showVictoryOverlay = false

            onSpeak("أهلاً بك في لعبة الشطب البصري. ابحث عن رمز: $targetSymbol واشطبه")
        }
    }

    // Trigger level generation when category changes
    LaunchedEffect(selectedCatIndex) {
        generateNewLevel()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "الشطب والإغلاق البصري 👁️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF002244)
                        )
                        Text(
                            "تطوير الانتباه والتمييز البصري",
                            fontSize = 12.sp,
                            color = Color.Gray
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.6f))
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
                    // Left Column (Controls & Info) - Styled like a Flexbox column with space-between
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Section of Left Column
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            activeChild?.let { child ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(child.avatarColor).copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(child.avatarColor)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "النشط: ${child.name} 🌟",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "الرمز المستهدف للبحث:",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                                            .clickable { onSpeak("الرمز المطلوب هو $targetSymbol") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = targetSymbol,
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currentCategory.description,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }

                        // Bottom Actions Section of Left Column - Kept firmly at the bottom without vertical overflow
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { generateNewLevel() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("توليد مستوى", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    selectedCatIndex = (selectedCatIndex + 1) % categories.size
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("الفئة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    // Right Column (Category selector + Dynamic Grid)
                    Column(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الفئات:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    itemsIndexed(categories) { index, cat ->
                                        val selected = index == selectedCatIndex
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) Color(0xFF0F172A) else Color.White)
                                                .border(1.dp, if (selected) Color(0xFF0F172A) else Color.LightGray, RoundedCornerShape(6.dp))
                                                .clickable { selectedCatIndex = index }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = cat.displayName,
                                                color = if (selected) Color.White else Color(0xFF475569),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Fully Responsive Weight-Based Grid (Fixed 4 rows x 6 columns)
                        // This perfectly replicates CSS Grid/Flex percentage sizing with 100% fit and absolutely no vertical overflow.
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (rowIndex in 0 until 4) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (colIndex in 0 until 6) {
                                        val index = rowIndex * 6 + colIndex
                                        if (index < gridItems.size) {
                                            val item = gridItems[index]
                                            val isCorrect = item == targetSymbol
                                            val isClicked = clickedIndices.contains(index)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isClicked && isCorrect) Color(0xFFDCFCE7)
                                                        else Color.White
                                                    )
                                                    .border(
                                                        width = if (isClicked && isCorrect) 2.dp else 1.dp,
                                                        color = if (isClicked && isCorrect) Color(0xFF22C55E) else Color(0xFFE2E8F0),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable(enabled = !isClicked) {
                                                        if (isCorrect) {
                                                            SoundEffectsHelper.playBubbleSound()
                                                            clickedIndices = clickedIndices + index
                                                            onSpeak(item)

                                                            val totalTargets = gridItems.count { it == targetSymbol }
                                                            val foundTargets = gridItems.filterIndexed { i, s ->
                                                                s == targetSymbol && clickedIndices.contains(i)
                                                            }.size

                                                            if (foundTargets == totalTargets) {
                                                                SoundEffectsHelper.playSuccess()
                                                                showVictoryOverlay = true

                                                                if (activeChild != null) {
                                                                    val scoreAwarded = 35 - (wrongClicksCount * 2).coerceAtLeast(0)
                                                                    viewModel.logSessionActivity(
                                                                        activityName = "الشطب البصري: ${currentCategory.displayName}",
                                                                        score = scoreAwarded.coerceAtLeast(10),
                                                                        notes = "أنهى الطفل تمرين الشطب والإغلاق البصري لرمز ($targetSymbol) بفئة: ${currentCategory.displayName} مع ارتكاب $wrongClicksCount أخطاء."
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            SoundEffectsHelper.playFailure()
                                                            wrongClicksCount++
                                                            Toast.makeText(context, "حاول ثانية يا بطل! ركز على $targetSymbol", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isClicked && isCorrect) {
                                                    Text(text = "🎉", fontSize = 32.sp)
                                                } else {
                                                    Text(
                                                        text = item,
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
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
                    // Active Child Indicator
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
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(child.avatarColor))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الطفل البطل النشط: ${child.name} 🌟",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    // Horizontal Category Selection Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الفئات:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            // Horizontal scrollable categories Row
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(categories) { index, cat ->
                                    val selected = index == selectedCatIndex
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) Color(0xFF0F172A) else Color.White)
                                            .border(1.5.dp, if (selected) Color(0xFF0F172A) else Color.LightGray, RoundedCornerShape(12.dp))
                                            .clickable { selectedCatIndex = index }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            color = if (selected) Color.White else Color(0xFF475569),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Target Box card displaying symbol to search for
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ابحث عن هذا الرمز المستهدف واشطبه:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(3.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                                    .clickable { onSpeak("الرمز المطلوب هو $targetSymbol") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = targetSymbol,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    Text(
                        text = currentCategory.description,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    // Grid of 24 worksheet items
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridItemsIndexed(gridItems) { index, item ->
                            val isCorrect = item == targetSymbol
                            val isClicked = clickedIndices.contains(index)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1.0f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isClicked && isCorrect) Color(0xFFDCFCE7) // Cleared targets
                                        else Color.White
                                    )
                                    .border(
                                        width = if (isClicked && isCorrect) 3.dp else 1.5.dp,
                                        color = if (isClicked && isCorrect) Color(0xFF22C55E) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !isClicked) {
                                        if (isCorrect) {
                                            SoundEffectsHelper.playBubbleSound()
                                            clickedIndices = clickedIndices + index

                                            // Speak the item
                                            onSpeak(item)

                                            // Check if all target occurrences are found
                                            val totalTargets = gridItems.count { it == targetSymbol }
                                            val foundTargets = gridItems.filterIndexed { i, s ->
                                                s == targetSymbol && clickedIndices.contains(i)
                                            }.size

                                            if (foundTargets == totalTargets) {
                                                // Complete level victory!
                                                SoundEffectsHelper.playSuccess()
                                                showVictoryOverlay = true

                                                // Log in SpeechViewModel if child exists
                                                if (activeChild != null) {
                                                    val scoreAwarded = 35 - (wrongClicksCount * 2).coerceAtLeast(0)
                                                    viewModel.logSessionActivity(
                                                        activityName = "الشطب البصري: ${currentCategory.displayName}",
                                                        score = scoreAwarded.coerceAtLeast(10),
                                                        notes = "أنهى الطفل تمرين الشطب والإغلاق البصري لرمز ($targetSymbol) بفئة: ${currentCategory.displayName} مع ارتكاب $wrongClicksCount أخطاء."
                                                    )
                                                }
                                            }
                                        } else {
                                            SoundEffectsHelper.playFailure()
                                            wrongClicksCount++
                                            Toast
                                                .makeText(context, "حاول ثانية يا بطل! ركز على $targetSymbol", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isClicked && isCorrect) {
                                    // Overlay dynamic accomplishments emoji
                                    Text(text = "🎉", fontSize = 45.sp)
                                } else {
                                    Text(
                                        text = item,
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }

                    // Restart and skip control bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { generateNewLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("توليد مستوى جديد", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedCatIndex = (selectedCatIndex + 1) % categories.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(start = 6.dp)
                        ) {
                            Text("الفئة التالية ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Beautiful interactive victory dialog - Fully responsive for Landscape Mode
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
                                Text("🥳", fontSize = 48.sp)
                                Text(
                                    text = "رائع جداً يا بطل! 🎉",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "لقد عثرت على جميع الرموز المستهدفة ($targetSymbol) بنجاح فائق وتألق بصري مميز!",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    generateNewLevel()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "المستوى التالي 🌟",
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
