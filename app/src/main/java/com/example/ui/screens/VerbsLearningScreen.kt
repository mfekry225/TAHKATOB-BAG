package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*

data class VerbActionPair(
    val id: Int,
    val boyPhrase: String,
    val girlPhrase: String,
    val detailEmoji: String,
    val verbType: String // e.g., "أفعال طعام", "أفعال حركة"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbsLearningScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()

    // Exactly 25 actions for Boys and 25 for Girls
    val activeList = remember {
        listOf(
            VerbActionPair(1, "الولد يأكل الموز", "البنت تأكل الموز", "🍌", "طعام"),
            VerbActionPair(2, "الولد يمشط الشعر", "البنت تمشط الشعر", "🪮", "نظافة"),
            VerbActionPair(3, "الولد يسلم باليد", "البنت تسلم باليد", "✋", "تحية"),
            VerbActionPair(4, "الولد يركب الدراجة", "البنت تركب الدراجة", "🚲", "حركة"),
            VerbActionPair(5, "الولد يقرأ كتاباً", "البنت تقرأ كتاباً", "📖", "دراسة"),
            VerbActionPair(6, "الولد يشرب الماء", "البنت تشرب الماء", "🥛", "طعام"),
            VerbActionPair(7, "الولد ينام في السرير", "البنت تنام في السرير", "🛏️", "راحة"),
            VerbActionPair(8, "الولد يكتب بالقلم", "البنت تكتب بالقلم", "✏️", "دراسة"),
            VerbActionPair(9, "الولد ينظف الأسنان", "البنت تنظف الأسنان", "🪥", "نظافة"),
            VerbActionPair(10, "الولد يلعب بالكرة", "البنت تلعب بالكرة", "⚽", "حركة"),
            VerbActionPair(11, "الولد يرسم لوحة", "البنت ترسم لوحة", "🎨", "إبداع"),
            VerbActionPair(12, "الولد يقفز فرحاً", "البنت تقفز فرحاً", "🦘", "حركة"),
            VerbActionPair(13, "الولد يلبس الحذاء", "البنت تلبس الحذاء", "👟", "لباس"),
            VerbActionPair(14, "الولد يغسل يده", "البنت تغسل يدها", "🧼", "نظافة"),
            VerbActionPair(15, "الولد يقطع الورقة", "البنت تقطع الورقة", "✂️", "مهارة"),
            VerbActionPair(16, "الولد يفتح الباب", "البنت تفتح الباب", "🚪", "حركة"),
            VerbActionPair(17, "الولد يسقي الورد", "البنت تسقي الورد", "🌧️", "زراعة"),
            VerbActionPair(18, "الولد يركض سريعاً", "البنت تركض سريعاً", "🏃", "حركة"),
            VerbActionPair(19, "الولد يبكي حزناً", "البنت تبكي حزناً", "😢", "مشاعر"),
            VerbActionPair(20, "الولد يضحك مسروراً", "البنت تضحك مسرورة", "😄", "مشاعر"),
            VerbActionPair(21, "الولد يسبح بالبحر", "البنت تسبح بالبحر", "🏊", "حركة"),
            VerbActionPair(22, "الولد يقود السيارة", "البنت تقود السيارة", "🚗", "حركة"),
            VerbActionPair(23, "الولد يطير الطائرة", "البنت تطير الطائرة", "🚀", "حركة"),
            VerbActionPair(24, "الولد يأكل التفاحة", "البنت تأكل التفاحة", "🍎", "طعام"),
            VerbActionPair(25, "الولد يلبس القبعة", "البنت تلبس القبعة", "🎩", "لباس")
        )
    }

    var activeIndex by remember { mutableStateOf(0) }
    val activePair = activeList[activeIndex]

    LaunchedEffect(activeIndex) {
        onSpeak("تعلم الأفعال! " + activePair.boyPhrase)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFBF8FF), Color(0xFFEFF5FF))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        
        // ================= LEFT: 25 VERBS QUICK ACCESS RAIL =================
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFF3E5F5), RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Sleek layout header + Mute toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    Text("الأفعال والجمل 🗣️", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF001D34))
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
                "اختر جملة من الـ 25 المتاحة:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                textAlign = TextAlign.Start
            )

            // Grid of 25 verb triggers
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(activeList) { index, item ->
                    val isSelected = activeIndex == index
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SoftViolet else Color(0xFFF3E5F5).copy(alpha = 0.4f))
                            .border(1.5.dp, if (isSelected) SoftViolet else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { activeIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(1.dp)
                        ) {
                            Text(item.detailEmoji, fontSize = 40.sp)
                            val actionWord = item.boyPhrase.split(" ").getOrNull(1) ?: "يفعل"
                            Text(
                                actionWord,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF4A148C),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // ================= CENTER SCREEN COMPARATIVE PORTAL =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Helpful Guide Card Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "الهدف: ميز بين (الولد والبنت) وصيغة الفعل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34)
                        )
                        Text(
                            "انقر على الكرت لسماع نطق الجملة الكاملة",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Surface(
                        color = SoftViolet.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "تصنيف: " + activePair.verbType,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftViolet
                        )
                    }
                }
            }

            // Dual Masculine vs Feminine Visual Blocks
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // MASCULINE CARD (BOY 👦)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSpeak(activePair.boyPhrase) }
                        .border(1.5.dp, SoftBluePrimary, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = SoftBluePrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("مذكّر (هو) 👦", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SoftBluePrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }

                        // Big emoji pairing illustration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("👦", fontSize = 100.sp)
                            Text(activePair.detailEmoji, fontSize = 100.sp)
                        }

                        Text(
                            text = activePair.boyPhrase,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { onSpeak(activePair.boyPhrase) },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لفظ الجملة 🗣️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // FEMININE CARD (GIRL 👧)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSpeak(activePair.girlPhrase) }
                        .border(1.5.dp, LovelyPink, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = LovelyPink.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("مؤنّث (هي) 👧", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LovelyPink, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }

                        // Big emoji pairing illustration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("👧", fontSize = 100.sp)
                            Text(activePair.detailEmoji, fontSize = 100.sp)
                        }

                        Text(
                            text = activePair.girlPhrase,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { onSpeak(activePair.girlPhrase) },
                            colors = ButtonDefaults.buttonColors(containerColor = LovelyPink),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لفظ الجملة 🗣️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Bottom Navigation triggers for slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (activeIndex > 0) activeIndex-- else activeIndex = activeList.size - 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("السابق ⬅️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "جملة ${activeIndex + 1} من 25",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Button(
                    onClick = {
                        viewModel.logSessionActivity(
                            activityName = "تعلم الأفعال مذكر ومؤنث",
                            score = 10,
                            notes = "أنجز الطفل التمييز الصرفي لجملة: " + activePair.boyPhrase
                        )
                        Toast.makeText(context, "سجل إنجاز رائع يا بطل! 🏆", Toast.LENGTH_SHORT).show()
                        if (activeIndex < activeList.size - 1) activeIndex++ else activeIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("التالي ➡️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
