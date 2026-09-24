package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ChildEntity
import com.example.data.local.SessionLogEntity
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SpeechViewModel,
    onNavigate: (String) -> Unit
) {
    val children by viewModel.childrenList.collectAsState()
    val activeChild by viewModel.activeChild.collectAsState()
    val activeChildLogs by viewModel.activeChildLogs.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    var showAddChildDialog by remember { mutableStateOf(false) }
    var showSelectChildDialog by remember { mutableStateOf(false) }
    var showLogSessionDialog by remember { mutableStateOf(false) }

    // Parent Portal math challenge security gating variables
    var showParentPortalAuth by remember { mutableStateOf(false) }
    var isGateUnlocked by remember { mutableStateOf(false) }
    var mathNum1 by remember { mutableStateOf(5) }
    var mathNum2 by remember { mutableStateOf(7) }
    var mathUserAnswer by remember { mutableStateOf("") }

    // Sky to grass gentle educational gradient background
    val playfulBgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD6F2FE), // Soft Sky Blue
            Color(0xFFE8F7FF),
            Color(0xFFE2F9D3)  // Bottom soft grassy green transition
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(playfulBgBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight

        // 1. Beautiful floating vector cloud items
        Box(modifier = Modifier.fillMaxSize()) {
            // Cloud 1
            Text(
                "☁️",
                fontSize = 110.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 60.dp, y = 30.dp)
                    .alpha(0.40f)
            )
            // Cloud 2
            Text(
                "☁️",
                fontSize = 90.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-120).dp, y = 45.dp)
                    .alpha(0.35f)
            )

            // Smiling Sun in upper right corner matching the image
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = 15.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF176).copy(alpha = 0.85f))
                    .border(3.dp, Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌞", fontSize = 52.sp)
            }

            // Beautiful hanging bunting/triangles decoration at top matching screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 140.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val bannerColors = listOf(
                    Color(0xFFFF8A80), Color(0xFFFFD54F), Color(0xFF81C784),
                    Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFFF8A80)
                )
                repeat(8) { idx ->
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(bannerColors[idx % bannerColors.size])
                    )
                }
            }
        }

        // Grassy green ground hills at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFB4ECB2).copy(alpha = 0.7f), Color(0xFF8CD48A))
                    )
                )
        ) {
            // Decorative Animals Greeting kids on the grassy ground mirroring mock-up
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left animals
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("🧸", fontSize = 42.sp) // Cute Teddy Bear
                    Text("🦌", fontSize = 38.sp) // Little Deer
                }
                // Right kids / friends
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("🐨", fontSize = 42.sp) // Koala
                    Text("👦", fontSize = 40.sp) // Happy jumping kid
                }
            }
        }

        // 2. Playful Grid Canvas Core Layout containing Title & Buttons
        Column(
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .fillMaxHeight()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Star / Child Button + Curved Ribbon Title Banner + Settings & Mute (Smaller, modern, higher up)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-8).dp) // Positioned slightly higher up
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Child & Evaluation Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Child button (👶)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF3393E2), CircleShape)
                            .clickable { showSelectChildDialog = true }
                            .testTag("dashboard_child_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👶", fontSize = 22.sp)
                    }

                    // Evaluation button (📈 representation for testing/logs)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF9C27B0), CircleShape)
                            .clickable {
                                if (activeChild != null) {
                                    showLogSessionDialog = true
                                } else {
                                    showSelectChildDialog = true
                                }
                            }
                            .testTag("dashboard_eval_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📈", fontSize = 22.sp)
                    }
                }

                // Main Title Banner shaped precisely like a decorative ribbon (slightly more compact for top layout)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(3.dp, Color(0xFF005AC1), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تطبيق المرح والتعلم!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34)
                        )
                    }

                    // Display selected active child tag dynamically underneath title banner
                    if (activeChild != null) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-3).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF001D34).copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "الجلسة العلاجية الحالية: ${activeChild!!.name} 👦",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .offset(y = (-3).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PlayfulOrange.copy(alpha = 0.9f))
                                .clickable { showSelectChildDialog = true }
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "اضغط هنا لاختيار طفل لبدء الجلسة 👶",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Right Side: Sound & Settings Control Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Toggle Icon Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, if (isMuted) Color(0xFF9E9E9E) else Color(0xFF4CAF50), CircleShape)
                            .clickable {
                                viewModel.toggleMute()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMuted) "🔇" else "🔊", 
                            fontSize = 22.sp,
                            modifier = Modifier.testTag("dashboard_mute_toggle")
                        )
                    }

                    // Settings Gear Button (Right-top: Navigate safely to Children profile screens behind Math Gate)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFEA5B5B), CircleShape)
                            .clickable {
                                mathNum1 = (3..10).random()
                                mathNum2 = (2..10).random()
                                mathUserAnswer = ""
                                isGateUnlocked = false
                                showParentPortalAuth = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 22.sp)
                    }
                }
            }

            // Game Cards grid containing 11 beautiful visual game cards
            val cardsList = listOf(
                GameModuleItem(
                    title = "كلماتي الأولى",
                    emojis = "👶👩‍🍼👨‍🍼",
                    backgroundColor = Color(0xFFFFF9DB),
                    borderColor = Color(0xFFFAB005),
                    onClick = { onNavigate("first_words") }
                ),
                GameModuleItem(
                    title = "تمارين النطق والتخاطب",
                    emojis = "🗣️👅🦁",
                    backgroundColor = Color(0xFFFFECE0),
                    borderColor = Color(0xFFFB923C),
                    onClick = { onNavigate("speech_exercises") }
                ),
                GameModuleItem(
                    title = "علاج اللدغات والمخارج",
                    emojis = "🗣️🎯🩹",
                    backgroundColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFF3B82F6),
                    onClick = { onNavigate("lisping_treatment") }
                ),
                GameModuleItem(
                    title = "التواصل غير اللفظي",
                    emojis = "🧩👁️👉",
                    backgroundColor = Color(0xFFE8F5E9),
                    borderColor = Color(0xFF4CAF50),
                    onClick = { onNavigate("non_verbal") }
                ),
                GameModuleItem(
                    title = "المجموعات الضمنية",
                    emojis = "🐰🍎🚗",
                    backgroundColor = Color(0xFFFEE6EE),
                    borderColor = Color(0xFFE97FB1),
                    onClick = { onNavigate("cards") }
                ),
                GameModuleItem(
                    title = "لعبة التوصيل",
                    emojis = "🔗🧩🐠",
                    backgroundColor = Color(0xFFE0F7FA),
                    borderColor = Color(0xFF4DD0E1),
                    onClick = { onNavigate("matching") }
                ),
                GameModuleItem(
                    title = "تعلم الساعة",
                    emojis = "⏰🕰️🌞",
                    backgroundColor = Color(0xFFFFF3E0),
                    borderColor = Color(0xFFFFB74D),
                    onClick = { onNavigate("clock") }
                ),
                GameModuleItem(
                    title = "رياضيات الأطفال",
                    emojis = "➕🔢💯",
                    backgroundColor = Color(0xFFFFFDE7),
                    borderColor = Color(0xFFFFF176),
                    onClick = { onNavigate("math") }
                ),
                GameModuleItem(
                    title = "لعبة الأفعال",
                    emojis = "👦👧🗣️",
                    backgroundColor = Color(0xFFF3E5F5),
                    borderColor = Color(0xFFBA68C8),
                    onClick = { onNavigate("verbs") }
                ),
                GameModuleItem(
                    title = "ارسم والعب",
                    emojis = "🎨🖌️🦄",
                    backgroundColor = Color(0xFFD6F2FE),
                    borderColor = Color(0xFF5AB6E5),
                    onClick = {
                        onNavigate("draw_and_play")
                    }
                ),
                GameModuleItem(
                    title = "مرح الحروف",
                    emojis = "أ ب ت 🧱",
                    backgroundColor = Color(0xFFE1F8D6),
                    borderColor = Color(0xFF8DCB6A),
                    onClick = { onNavigate("letters") }
                ),
                GameModuleItem(
                    title = "تعلم الكتابة",
                    emojis = "📝🏫🦁",
                    backgroundColor = Color(0xFFFFECD6),
                    borderColor = Color(0xFFFCA541),
                    onClick = {
                        viewModel.setSelectedDrawingTemplateIndex(1) // Tracing "أ" template directly
                        onNavigate("drawing")
                    }
                ),
                GameModuleItem(
                    title = "الألعاب والأنشطة الذكية",
                    emojis = "🧩🧠💡",
                    backgroundColor = Color(0xFFFFF9D4),
                    borderColor = Color(0xFFF0C42E),
                    onClick = { onNavigate("games") }
                ),
                GameModuleItem(
                    title = "الانتباه والتركيز",
                    emojis = "🧠🎯🎯",
                    backgroundColor = Color(0xFFFFF1F1),
                    borderColor = Color(0xFFFF5252),
                    onClick = { onNavigate("attention_focus") }
                ),
                GameModuleItem(
                    title = "الشطب والإغلاق البصري",
                    emojis = "👁️⭐🔍",
                    backgroundColor = Color(0xFFF3E8FF),
                    borderColor = Color(0xFFA855F7),
                    onClick = { onNavigate("visual_cancellation") }
                ),
                GameModuleItem(
                    title = "المتاهات الذكية",
                    emojis = "🧩🚗🌸",
                    backgroundColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFF22C55E),
                    onClick = { onNavigate("smart_maze") }
                ),
                GameModuleItem(
                    title = "التوصيل المتحرك",
                    emojis = "🔗🍎🦁",
                    backgroundColor = Color(0xFFECFEFF),
                    borderColor = Color(0xFF06B6D4),
                    onClick = { onNavigate("animated_matching_lines") }
                )
            )

            // Smooth horizontal scrolling row of premium child-friendly game cards
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(cardsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxHeight(0.92f)
                            .width(185.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(3.dp, item.borderColor, RoundedCornerShape(24.dp))
                            .clickable { item.onClick() }
                            .testTag("game_card_${item.title}"),
                        colors = CardDefaults.cardColors(containerColor = item.backgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(2.dp, item.borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.emojis, fontSize = 36.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001D34),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Section: Select child (retained and beautiful)
    if (showSelectChildDialog) {
        Dialog(onDismissRequest = { showSelectChildDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "اختر ملف الطفل لتفعيل المتابعة الذكية",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF005AC1),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (children.isEmpty()) {
                        Text(
                            "لا توجد ملفات حالية للأطفال، يرجى إضافة ملف طفل جديد للبدء بجدولة الجلسات ورصد أداء الطفل.",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(children) { child ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (activeChild?.id == child.id) Color(0xFFD3E4FF)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.selectChild(child.id)
                                            showSelectChildDialog = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(child.avatarColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(child.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1.0f)) {
                                        Text(child.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF001D34))
                                        Text("عمر زمني: ${child.age} | عمر عقلي: ${child.mentalAge} سنوات", fontSize = 11.sp, color = Color(0xFF44474E))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                showSelectChildDialog = false
                                showAddChildDialog = true
                            }) {
                                Text("إضافة جديد ➕", fontWeight = FontWeight.Bold)
                            }
                            if (activeChild != null) {
                                TextButton(onClick = {
                                    showSelectChildDialog = false
                                    showLogSessionDialog = true
                                }) {
                                    Text("توثيق الجلسة ⏱️", color = PlayfulOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        TextButton(onClick = { showSelectChildDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                }
            }
        }
    }

    // Modal Section: Add New Child (retained and beautiful)
    if (showAddChildDialog) {
        var name by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        var mentalAge by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        val avatarColorArray = listOf(0xFF4A90E2, 0xFFFF7E67, 0xFF62D2A2, 0xFFFFD166, 0xFF9B5DE5, 0xFFFF92B4)
        var selectedColor by remember { mutableStateOf(avatarColorArray.first()) }

        Dialog(onDismissRequest = { showAddChildDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text(
                        "إضافة ملف طفل جديد",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = PlayfulOrange,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                            Text(
                                text = "خصوصية تامة: تُحفظ كافة البيانات محلياً على هاتفك فقط ولا تُرسل لأي خوادم خارجية، ولا حاجة لإيميل أو حساب لإنشاء ملف الطفل.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الطفل (اختياري)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("العمر الزمني (اختياري)") },
                            modifier = Modifier.weight(1.0f)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("الدولة / بلد الإقامة (اختياري)") },
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات وصعوبات النطق (اختياري)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Text(
                        "لون ملف التقييم المميز:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001D34),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        avatarColorArray.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .clickable { selectedColor = color }
                                    .padding(4.dp)
                            ) {
                                if (selectedColor == color) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.6f))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddChildDialog = false }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val childName = if (name.isNotBlank()) name else "ملف طفل جديد"
                                val childAge = age.toIntOrNull() ?: 5
                                viewModel.addNewChild(
                                    name = childName,
                                    age = childAge,
                                    mentalAge = childAge,
                                    notes = notes,
                                    avatarColor = selectedColor.toInt(),
                                    country = country
                                )
                                showAddChildDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange)
                        ) {
                            Text("حفظ الملف 📝", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Section: Document / Log Session notes (retained and beautiful)
    if (showLogSessionDialog) {
        var activityName by remember { mutableStateOf("البطاقات التعليمية") }
        var score by remember { mutableStateOf("5") }
        var sessionNotes by remember { mutableStateOf("") }
        val activitiesList = listOf("البطاقات التعليمية", "مرح الحروف والأرقام", "ارسم والعب", "الألعاب الذكية")

        Dialog(onDismissRequest = { showLogSessionDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "رصد وتوثيق أداء الجلسة الفوري",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF001D34),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        "النشاط التعليمي بالجلسة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activitiesList.take(2).forEach { act ->
                            Button(
                                onClick = { activityName = act },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activityName == act) Color(0xFF005AC1) else Color.LightGray.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text(
                                    act,
                                    fontSize = 10.sp,
                                    color = if (activityName == act) Color.White else Color.Black,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activitiesList.drop(2).forEach { act ->
                            Button(
                                onClick = { activityName = act },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activityName == act) Color(0xFF005AC1) else Color.LightGray.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text(
                                    act,
                                    fontSize = 10.sp,
                                    color = if (activityName == act) Color.White else Color.Black,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it },
                        label = { Text("تقييم تفاعل الطفل (من 5)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("ملاحظات الأخصائي (مثال: تقدم رائع في الحروف)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLogSessionDialog = false }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.logSessionActivity(
                                    activityName = activityName,
                                    score = score.toIntOrNull() ?: 5,
                                    notes = sessionNotes
                                )
                                showLogSessionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005AC1))
                        ) {
                            Text("حفظ التوثيق ⏱️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Section: Secure Parent Gate Math Challenge
    if (showParentPortalAuth) {
        val context = LocalContext.current
        Dialog(onDismissRequest = { showParentPortalAuth = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "بوابة الأهالي والأخصائيين 🔐",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFFE91E63),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (!isGateUnlocked) {
                        Text(
                            "للأمان ومنع الأطفال من تعديل ملفات المتابعة والتقارير، يرجى حل السؤال الحسابي التالي:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFECEF))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ما ناتج:  $mathNum1   +   $mathNum2   =   ؟",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD81B60)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = mathUserAnswer,
                            onValueChange = { mathUserAnswer = it },
                            placeholder = { Text("اكتب الإجابة بالرقام الإنجليزية") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showParentPortalAuth = false }) {
                                Text("إلغاء الرجوع 🔙", fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    val userVal = mathUserAnswer.trim().toIntOrNull()
                                    val correctVal = mathNum1 + mathNum2
                                    if (userVal == correctVal) {
                                        isGateUnlocked = true
                                        Toast.makeText(context, "تم التحقق بنجاح! الرجاء اختيار الوجهة 🛡️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "إجابة غير صحيحة! يرجى المحاولة بشكل صحيح.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تحقق ودخول 🗝️", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Unlocked! Show Gorgeous Choice Options
                        Text(
                            "مرحباً بك يا بطل في لوحة التحكم الإدارية والإنتاجية. يرجى اختيار وجهتك المهنية:",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Choice 1: Children list (لوحة متابعة الأطفال)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showParentPortalAuth = false
                                    onNavigate("children_list")
                                }
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF3B82F6))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👥", fontSize = 32.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("متابعة ملفات الأطفال ورصد الأداء", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                                    Text("عرض ورصد الجلسات وإضافة ملفات الأطفال الجدد.", fontSize = 11.sp, color = Color(0xFF1E40AF))
                                }
                            }
                        }

                        // Choice 2: About the App (حول التطبيق)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD97706))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ℹ️", fontSize = 28.sp)
                                    Text(
                                        text = "حول التطبيق",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الأخصائي محمد فكري", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تصميم وبرمجة:", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("mfekry225@outlook.com", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("البريد الإلكتروني: 📧", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("@mfekry225", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("انستجرام: 📸", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("v1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("الإصدار الحالي للتطبيق:", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showParentPortalAuth = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إغلاق البوابة ❌", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class GameModuleItem(
    val title: String,
    val emojis: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val onClick: () -> Unit
)
