package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel

@Composable
fun RenderStaticExerciseImage(
    stepId: Int,
    isLisping: Boolean,
    viewModel: SpeechViewModel,
    mouthStyle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechMetadataMap by viewModel.speechExerciseImages.collectAsState()
    val lispMetadataMap by viewModel.lispExerciseImages.collectAsState()

    val metadata = remember(stepId, isLisping, speechMetadataMap, lispMetadataMap) {
        if (isLisping) {
            lispMetadataMap[stepId]
        } else {
            speechMetadataMap[stepId]
        }
    }

    val resourceId = remember(metadata?.imagePath) {
        if (metadata != null && metadata.imagePath.isNotEmpty()) {
            context.resources.getIdentifier(metadata.imagePath, "drawable", context.packageName)
        } else {
            0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            // Static image from JSON was found in the project's drawable folder!
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = metadata?.title ?: "صورة التدريب",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Medical / Educational Info from JSON
                metadata?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = it.medicalDescription,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اسم ملف الصورة المخصصة: ${it.imagePath}.png",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            // Fallback when the custom drawable doesn't exist yet
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Procedural MiloFace as fallback image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MiloFace(
                        mouthStyle = mouthStyle,
                        isPuffed = mouthStyle == "puffCheeks",
                        isHappy = mouthStyle == "cheerVictory",
                        modifier = Modifier.fillMaxSize(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Beautiful medical instruction card from JSON
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🩺", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "الوصف الطبي والتعليمي المعتمد لكل تمرين:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metadata?.medicalDescription ?: "صورة مخصصة لوصف التدريب وطريقة النطق السليمة علمياً.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 متاح استبدالها بصورتك الحقيقية، فقط ارفع ملف باسم: ${metadata?.imagePath ?: "speech_ex_x"}.png",
                        fontSize = 9.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
