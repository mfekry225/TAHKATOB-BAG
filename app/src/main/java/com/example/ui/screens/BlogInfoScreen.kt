package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BlogArticle(val title: String, val author: String, val content: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogInfoScreen(
    onBack: () -> Unit
) {
    val articles = listOf(
        BlogArticle(
            title = "أهمية التدريب بالمجموعات الضمنية للأطفال",
            author = "أخصائي مدونة جلسة تخاطب",
            content = "المجموعات الضمنية (كالأشكال والألوان والحيوانات) تعتبر الحجر الأساسي لتكوين المخزن اللغوي المعرفي للطفل. تمكنه من تصنيف الأشياء من حوله وهو ما يزيد من استيعابه العقلي (من عمر سنة حتى 10 سنوات).",
            label = "تطوير المعرفة واللغة"
        ),
        BlogArticle(
            title = "كيفية تدريب الأطفال المصابين بصعوبة مخارج الحروف",
            author = "فريق الإرشاد الصوتي",
            content = "نوصي بتجزئة مخارج الحروف للطفل. ركز على اهتزاز الحبال الصوتية أو ملامسة الشفاه واللسان للأسنان. نطق الحرف منفرداً ثم مدمجاً بكلمة ومثال مكرر مع استخدام مسجل الصوت لتعزيز الملاحظة الذاتية.",
            label = "علاج النطق وتصحيح الأحرف"
        ),
        BlogArticle(
            title = "طريقة دمج اللعب في جلسات تعديل السلوك والتخاطب",
            author = "قسم التربية الخاصة",
            content = "الألعاب مثل تتبع كتابة الأحرف، التلوين، والشكل والظل، ومقارنة الأحجام تزيد فترات انتباه الأطفال وتزيل الخوف أو الرهاب من الجلسات الأكاديمية الصارمة. اجعل الجلسة كغرفة ألعاب تفاعلية!",
            label = "المنهج الترفيهي التفاعلي"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مقالات جلسة تخاطب 🌐", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF001D34)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF1A1C1E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.5f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SoftBluePrimary.copy(alpha = 0.12f))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "مرحبًا بك في منصة المقالات والإرشادات العلمية المرفقة بمدونتك (مدونة جلسة تخاطب) لمساعدة المعلمين والأولياء خطوة بخطوة!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SoftBluePrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(articles) { art ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                art.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlayfulOrange
                            )

                            Text(
                                art.author,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            art.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            art.content,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
