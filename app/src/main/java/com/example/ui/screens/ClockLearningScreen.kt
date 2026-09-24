package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockLearningScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()
    var currentHour by remember { mutableStateOf(3) }
    var currentMinute by remember { mutableStateOf(0) }

    // Friendly Arabic time helper names
    val arabicTimeSpoken = remember(currentHour, currentMinute) {
        val hourText = when (currentHour) {
            1 -> "الواحدة"
            2 -> "الثانية"
            3 -> "الثالثة"
            4 -> "الرابعة"
            5 -> "الخامسة"
            6 -> "السادسة"
            7 -> "السابعة"
            8 -> "الثامنة"
            9 -> "التاسعة"
            10 -> "العاشرة"
            11 -> "الحادية عشر"
            12 -> "الثانية عشر"
            else -> "الواحدة"
        }
        val minText = when (currentMinute) {
            0 -> "تماماً"
            5 -> "وخمس دقائق"
            15 -> "والربع"
            30 -> "والنصف"
            45 -> "إلا ربعاً"
            else -> "ودقائق"
        }
        "الساعة الآن هي: $hourText $minText ⏰"
    }

    LaunchedEffect(currentHour, currentMinute) {
        onSpeak(arabicTimeSpoken)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDFA), Color(0xFFEFFBFF))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // ================= LEFT CONTROL STATION =================
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(Color.White)
                .border(2.dp, Color(0xFFFFF4E0), RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Minimalist back control + Mute toggle
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
                            .background(Color(0xFFEEEEEE), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعلّم الساعة ⏰", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF001D34))
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

            // Target Hours Quick click list
            Text(
                "اختر ساعة محددة لرؤية العقارب 👇:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Dynamic grid of 12 hours for toddler tapping!
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            ) {
                listOf(
                    listOf(12, 1, 2),
                    listOf(3, 4, 5),
                    listOf(6, 7, 8),
                    listOf(9, 10, 11)
                ).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowList.forEach { valHour ->
                            val isSelected = currentHour == valHour
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PlayfulOrange else Color(0xFFFFF9F2))
                                    .clickable {
                                        currentHour = valHour
                                        currentMinute = 0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$valHour",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else Color(0xFF6E4F02)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Minutes Adjuster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(0, "تَمَاماً"),
                    Pair(15, "والرُّبْع"),
                    Pair(30, "والنِّصْف"),
                    Pair(45, "إلا رُبْع")
                ).forEach { (min, label) ->
                    val isSelected = currentMinute == min
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SoftBluePrimary else Color(0xFFEDF4FE))
                            .clickable { currentMinute = min }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // ================= CENTER CANVAS CLOCK AREA =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Sleek info card showing reading
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
                            "الساعة الآن باللغة العربية:",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = arabicTimeSpoken,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34)
                        )
                    }
                    Button(
                        onClick = { onSpeak(arabicTimeSpoken) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استمع 🗣️", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Giant interactive Clock face drawn with accurate canvas hands!
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White)
                    .border(6.dp, PlayfulOrange, CircleShape)
                    .testTag("clock_interactive_face"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2f
                    val center = Offset(radius, radius)

                    // Draw center dot
                    drawCircle(color = PlayfulOrange, radius = 12f)

                    // Draw ticks for numbers
                    for (i in 1..12) {
                        val angle = (i * 30 - 90) * (Math.PI / 180f)
                        val startPos = Offset(
                            (radius + (radius - 18.dp.toPx()) * cos(angle)).toFloat(),
                            (radius + (radius - 18.dp.toPx()) * sin(angle)).toFloat()
                        )
                        val endPos = Offset(
                            (radius + (radius - 8.dp.toPx()) * cos(angle)).toFloat(),
                            (radius + (radius - 8.dp.toPx()) * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = Color.LightGray,
                            start = startPos,
                            end = endPos,
                            strokeWidth = 4f
                        )
                    }

                    // Draw Hour Hand (currentHour angle)
                    // Hour hand rotates 30 degrees per hour, plus 0.5 degrees per minute
                    val hourAngle = ((currentHour % 12) * 30 + currentMinute * 0.5f - 90) * (Math.PI / 180f)
                    val hourHandLength = radius * 0.5f
                    val hourHandEnd = Offset(
                        (radius + hourHandLength * cos(hourAngle)).toFloat(),
                        (radius + hourHandLength * sin(hourAngle)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF001D34),
                        start = center,
                        end = hourHandEnd,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )

                    // Draw Minute Hand (currentMinute angle)
                    val minuteAngle = (currentMinute * 6 - 90) * (Math.PI / 180f)
                    val minuteHandLength = radius * 0.75f
                    val minuteHandEnd = Offset(
                        (radius + minuteHandLength * cos(minuteAngle)).toFloat(),
                        (radius + minuteHandLength * sin(minuteAngle)).toFloat()
                    )
                    drawLine(
                        color = PlayfulOrange,
                        start = center,
                        end = minuteHandEnd,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                // Numbers placement inside clock face
                Box(modifier = Modifier.fillMaxSize()) {
                    listOf(
                        Pair("١٢", Alignment.TopCenter),
                        Pair("٣", Alignment.CenterEnd),
                        Pair("٦", Alignment.BottomCenter),
                        Pair("٩", Alignment.CenterStart)
                    ).forEach { (numStr, align) ->
                        Text(
                            text = numStr,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D34),
                            modifier = Modifier
                                .align(align)
                                .padding(18.dp)
                        )
                    }
                }
            }

            // Visual time display numbers
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                ) {
                    val formattedMin = String.format("%02d", currentMinute)
                    Text(
                        text = "$currentHour : $formattedMin",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftBluePrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.logSessionActivity(
                            activityName = "تعلم الساعة ومفهوم الوقت",
                            score = 10,
                            notes = "تعلم الطفل الساعات والدقائق بنجاح لقيمة: " + currentHour
                        )
                        onSpeak("ممتاز! حفظنا إنجازك في قراءة الساعة")
                        Toast.makeText(context, "تم حفظ الإنجاز بنجاح 🏆!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("رصد نجاح الساعة ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
