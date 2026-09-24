package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.data.models.FlashcardDatabase
import com.example.data.models.FlashcardItem
import com.example.ui.AudioRecorderHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

sealed class FlashcardScreenState {
    object CategoriesPortal : FlashcardScreenState()
    data class CategoryItemsGrid(val categoryName: String) : FlashcardScreenState()
    data class ImmersiveCardView(val categoryName: String, val initialIndex: Int) : FlashcardScreenState()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FlashcardsScreen(
    viewModel: SpeechViewModel,
    audioHelper: AudioRecorderHelper,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var screenState by remember { mutableStateOf<FlashcardScreenState>(FlashcardScreenState.CategoriesPortal) }
    val isMuted by viewModel.isMuted.collectAsState()

    // Categories with beautiful icons and colors
    val categoryMetadata = remember {
        listOf(
            Triple("الحيوانات الأليفة", "🐶", Color(0xFFE3F2FD)),
            Triple("الحيوانات المفترسة والبرية", "🦁", Color(0xFFFBE9E7)),
            Triple("الفواكه", "🍎", Color(0xFFE8F5E9)),
            Triple("الخضروات", "🥕", Color(0xFFFFF3E0)),
            Triple("وسائل المواصلات", "🚗", Color(0xFFE0F7FA)),
            Triple("الملابس", "👕", Color(0xFFF3E5F5)),
            Triple("أثاث وأدوات المنزل", "🪑", Color(0xFFEFEBE9)),
            Triple("أجهزة كهربائية", "📺", Color(0xFFE8EAF6)),
            Triple("أدوات المطبخ", "🥄", Color(0xFFECEFF1)),
            Triple("أدوات الحمام", "🧼", Color(0xFFE0F2F1)),
            Triple("الأدوات المدرسية", "🎒", Color(0xFFFFF8E1)),
            Triple("أعضاء الجسم", "👃", Color(0xFFFCE4EC)),
            Triple("ألعاب الأطفال", "🧸", Color(0xFFFFF3E0)),
            Triple("الأشكال الهندسية والألوان", "🎨", Color(0xFFEDE7F6)),
            Triple("أدوات المهن والتصليح", "🔨", Color(0xFFEFEBE9)),
            Triple("أطعمة ومأكولات", "🍜", Color(0xFFF1F8E9)),
            Triple("عناصر الطبيعة والطقس", "☀️", Color(0xFFE0F7FA)),
            Triple("أدوات العناية الشخصية والنظافة", "🪮", Color(0xFFFCE4EC)),
            Triple("البحر والكائنات البحرية", "🐙", Color(0xFFE0F2F1)),
            Triple("الطيور والحشرات", "🦋", Color(0xFFE8F5E9)),
            Triple("المشاعر والانفعالات", "🎭", Color(0xFFFCE4EC))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFAFA), Color(0xFFEEF9FF))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Ultra Sleek Slim Header (No huge titles taking up crucial vertical estate on landscape/tablets)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            when (val state = screenState) {
                                is FlashcardScreenState.CategoriesPortal -> {
                                    audioHelper.stopRecording()
                                    audioHelper.stopPlaying()
                                    onBack()
                                }
                                is FlashcardScreenState.CategoryItemsGrid -> {
                                    screenState = FlashcardScreenState.CategoriesPortal
                                }
                                is FlashcardScreenState.ImmersiveCardView -> {
                                    screenState = FlashcardScreenState.CategoryItemsGrid(state.categoryName)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF44474E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (val state = screenState) {
                            is FlashcardScreenState.CategoriesPortal -> "المجموعات الضمنية 🃏"
                            is FlashcardScreenState.CategoryItemsGrid -> "محتويات: ${state.categoryName}"
                            is FlashcardScreenState.ImmersiveCardView -> "بطاقة تفاعلية 🔊"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001D34)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Mute toggle
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 15.sp)
                    }

                    // Small explanatory badge for children
                    Surface(
                        color = PlayfulOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡 انقر على الصورة لتسمع نطقها", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PlayfulOrange)
                        }
                    }
                }
            }

            Divider(color = Color(0xFFE3F2FD), thickness = 1.dp)

            // Animated Screen content depending on screen state
            Box(modifier = Modifier.weight(1f)) {
                when (val currentState = screenState) {
                    
                    // ================= 1. CATEGORIES PORTAL =================
                    is FlashcardScreenState.CategoriesPortal -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Text(
                                "اختر تصنيفاً للبدء بالتعلم واللعب الممتع 🌟",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 130.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(categoryMetadata) { _, (name, emoji, bg) ->
                                    val count = FlashcardDatabase.items.count { it.category == name }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(bg)
                                            .border(2.dp, bg.shadowColor(0.2f), RoundedCornerShape(20.dp))
                                            .clickable {
                                                screenState = FlashcardScreenState.CategoryItemsGrid(name)
                                                onSpeak(name)
                                            }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(76.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(emoji, fontSize = 42.sp)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF001D34)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                color = Color.White.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "$count بطاقة",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= 2. CATEGORY ITEMS DIRECTORY =================
                    is FlashcardScreenState.CategoryItemsGrid -> {
                        val items = remember(currentState.categoryName) {
                            FlashcardDatabase.items.filter { it.category == currentState.categoryName }
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "جميع العناصر المتاحة في المجموعة (${items.size} كلمات):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Button(
                                    onClick = {
                                        screenState = FlashcardScreenState.ImmersiveCardView(currentState.categoryName, 0)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("عرض تفاعلي مباشر 🚀", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 100.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(items) { index, item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                                            .clickable {
                                                screenState = FlashcardScreenState.ImmersiveCardView(currentState.categoryName, index)
                                            }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(76.dp)
                                                    .clip(CircleShape)
                                                    .background(SoftBluePrimary.copy(alpha = 0.05f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(item.emoji, fontSize = 42.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                item.arabicName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF001D34)
                                            )
                                            Text(
                                                item.englishName,
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= 3. BEAUTIFUL IMMERSIVE SLIDABLE CARD STATE =================
                    is FlashcardScreenState.ImmersiveCardView -> {
                        val filteredItems = remember(currentState.categoryName) {
                            FlashcardDatabase.items.filter { it.category == currentState.categoryName }
                        }
                        
                        val pagerState = rememberPagerState(
                            initialPage = currentState.initialIndex,
                            pageCount = { filteredItems.size }
                        )

                        val activeCard = filteredItems.getOrNull(pagerState.currentPage) ?: filteredItems.first()

                        var isRecording by remember { mutableStateOf(false) }
                        var isPlayingBack by remember { mutableStateOf(false) }

                        // Announce item name automatically upon swipe
                        LaunchedEffect(pagerState.currentPage) {
                            onSpeak(activeCard.arabicName)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            
                            // Left Section: Interactive Horizontal Swipe Pager & Info Pointers
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                
                                // Sleek Pager Card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        val item = filteredItems[page]
                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp)
                                                .testTag("flashcard_interactive_surface"),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(14.dp)
                                                    .clickable { onSpeak(item.arabicName) },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                // LEFT SIDE: The MUCH larger image/illustration emoji
                                                Box(
                                                    modifier = Modifier
                                                        .size(150.dp)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(SoftBluePrimary.copy(alpha = 0.08f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(item.emoji, fontSize = 100.sp) // HUGE emoji!
                                                }
                                                
                                                // RIGHT SIDE: The words on the side, stacked vertically!
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(horizontal = 4.dp),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "الكلمة بالعربية:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        item.arabicName,
                                                        fontSize = 32.sp, // beautiful large font
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = SoftBluePrimary,
                                                        textAlign = TextAlign.Start
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        text = "In English:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        item.englishName,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PlayfulOrange,
                                                        textAlign = TextAlign.Start
                                                     )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive swiping helper controls below card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage > 0) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                }
                                            }
                                        },
                                        enabled = pagerState.currentPage > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("السابق ⬅️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "${pagerState.currentPage + 1} من ${filteredItems.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001D34)
                                    )

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage < filteredItems.size - 1) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                } else {
                                                    Toast.makeText(context, "أكملت كل بطاقات المجموعة! ممتاز 🏆", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("التالي ➡️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Right Section: Speech Therapy Tips & Kids Auditory station
                            Column(
                                modifier = Modifier
                                    .width(220.dp)
                                    .fillMaxHeight()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0xFFE0F2F1), RoundedCornerShape(20.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Speech-Therapy Tip Box
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("طريقة النطق ومخارج الصوت:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF9FBFD))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            activeCard.articulationTip.ifEmpty { "تفخيم الصوت ونطق الحرف الأول بدقة مع الشفتين." },
                                            fontSize = 9.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                // Kids Micro recording station
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎙️ محطة تكرار الطفل للجملة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    
                                    // Record mic block
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isRecording) Color(0xFFFFEBEE) else Color(0xFFEDF4FE))
                                            .clickable {
                                                if (isRecording) {
                                                    audioHelper.stopRecording()
                                                    isRecording = false
                                                    onSpeak("أحسنت لقد تم حفظ نطقك")
                                                    Toast.makeText(context, "تم حفظ النطق بنجاح! 🏆", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val success = audioHelper.startRecording()
                                                    if (success) {
                                                        isRecording = true
                                                    } else {
                                                        Toast.makeText(context, "الرجاء كتم أو منح إذن الميكروفون!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isRecording) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("اضغط لإيقاف التسجيل", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                            } else {
                                                Text("🎙️ سجل صوت الطفل", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftBluePrimary)
                                            }
                                        }
                                    }

                                    // Playback block
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (audioHelper.hasRecording() && !isRecording) MintGreen.copy(alpha = 0.2f) else Color(0xFFF5F5F5))
                                            .clickable(enabled = audioHelper.hasRecording() && !isRecording) {
                                                isPlayingBack = true
                                                audioHelper.startPlaying {
                                                    isPlayingBack = false
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                if (isPlayingBack) "جاري التشغيل..." else "استمع لصوت طفلك 🎧",
                                                fontSize = 9.sp,
                                                color = if (audioHelper.hasRecording() && !isRecording) Color.Black else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                // Log achievement immediately
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .clickable {
                                            viewModel.logSessionActivity(
                                                activityName = "البطاقات التعليمية",
                                                score = 10,
                                                notes = "أنجز الطفل بطاقة: " + activeCard.arabicName
                                            )
                                            Toast.makeText(context, "تم رصد مهارة النطق للطفل بنجاح! ✓", Toast.LENGTH_SHORT).show()
                                            onSpeak("ممتاز! تم تسجيل الإنجاز بنجاح.")
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تم النطق بنجاح ✓", fontSize = 10.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension function to help make backgrounds look beautiful (darkened border shade)
fun Color.shadowColor(factor: Float): Color {
    return Color(
        red = (this.red * (1f - factor)).coerceIn(0f, 1f),
        green = (this.green * (1f - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}
