package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AudioRecorderHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class FirstWordItem(
    val arabicName: String,
    val alternativeSaying: String,
    val enName: String,
    val emoji: String,
    val soundTip: String,
    val therapyTip: String,
    val companionGraphic: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstWordsScreen(
    viewModel: SpeechViewModel,
    audioHelper: AudioRecorderHelper,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isMuted by viewModel.isMuted.collectAsState()
    
    // UI Mode state: true for sliding pager immersive view, false for general item grid
    var isImmersivePagerMode by remember { mutableStateOf(false) }
    var initialPageIndex by remember { mutableStateOf(0) }

    // List of 20 high-quality vocabulary words representing a child's first words
    val firstWordsList = remember {
        listOf(
            FirstWordItem(
                arabicName = "ماما",
                alternativeSaying = "والدتي الحبيبة",
                enName = "Mama",
                emoji = "👩‍🍼",
                soundTip = "مَـا مَـا 💕",
                therapyTip = "انطق بحرص مع ضم الشفتين برفق مع ملامسة يد الطفل لخدي الأم للشعور بالرنين والاهتزاز الصوتي."
            ),
            FirstWordItem(
                arabicName = "بابا",
                alternativeSaying = "والدي الحبيب",
                enName = "Baba / Papa",
                emoji = "👨‍🍼",
                soundTip = "بَـا بَـا ✨",
                therapyTip = "حبب الطفل في تفجير شفتي الحرف (ب) بصوت لطيف وابتسامة واسعة، متبوعة بفتحة مريحة."
            ),
            FirstWordItem(
                arabicName = "نونو",
                alternativeSaying = "طفل صغير",
                enName = "Baby",
                emoji = "👶",
                soundTip = "نُـو نُـو 🍼",
                therapyTip = "تدريب على حرف النون الأنفي السلس بتكرار إشارة السبابة وتوجيه اللسان لمس لثة الأسنان العلوية."
            ),
            FirstWordItem(
                arabicName = "هم",
                alternativeSaying = "أكل / طعام",
                enName = "Yum / Eat",
                emoji = "🥣",
                soundTip = "هَـمّ 😋",
                therapyTip = "تساعد الطفل على التعبير عن احتياجه للطعام والربط بين الحركة والمضغ والابتلاع الطبيعي."
            ),
            FirstWordItem(
                arabicName = "امبو",
                alternativeSaying = "ماء / شرب",
                enName = "Water",
                emoji = "🥛",
                soundTip = "أَمْـبُـو 💧",
                therapyTip = "تسهل طلب الشرب السريع، كرر نطق الكلمة عند تقديم الكوب مباشرة للربط الشرطي المباشر."
            ),
            FirstWordItem(
                arabicName = "باي",
                alternativeSaying = "إلى اللقاء",
                enName = "Bye Bye",
                emoji = "👋",
                soundTip = "بَـايْ بَـايْ 👋",
                therapyTip = "تعزيز التواصل الحركي والإشاري بمزامنة نطق الكلمة مع تحريك الكف دليلاً على الموادعة."
            ),
            FirstWordItem(
                arabicName = "ددو",
                alternativeSaying = "سيارة / تيت تيت",
                enName = "Car / Vroom",
                emoji = "🚗",
                soundTip = "بِـيبْ بِـيبْ 🚗",
                therapyTip = "استخدم تعبيرات رنين محرك السيارة مع نغمة 'بيب بيب...' لتحفيز انتباه سمع الطفل البصري."
            ),
            FirstWordItem(
                arabicName = "قطة",
                alternativeSaying = "بسة / نونو",
                enName = "Cat / Meow",
                emoji = "🐱",
                soundTip = "مِـيـاوْ 🐱",
                therapyTip = "استعارة المواء المرتفع والمرقق لصوت القطة لتحفيز عضلات الفم والتفاعل العاطفي السريع."
            ),
            FirstWordItem(
                arabicName = "كلب",
                alternativeSaying = "هوهو / حيوان أليف",
                enName = "Dog / Woof",
                emoji = "🐶",
                soundTip = "هَوْ هَوْ 🐶",
                therapyTip = "شجع الطفل على إصدار صوت الكلب الصدر الصريح لتدريب عضلات الحجاب الحاجز والتنفس العميق."
            ),
            FirstWordItem(
                arabicName = "ننة",
                alternativeSaying = "نام / نوم",
                enName = "Sleep",
                emoji = "😴",
                soundTip = "نَـنْـة 🛌",
                therapyTip = "ساعد الطفل على مط نغمة الصوت بهدوء مع تمثيل وضع النوم بوضع الكف تحت الخد الأيمن."
            ),
            FirstWordItem(
                arabicName = "ألو",
                alternativeSaying = "هاتف / مكالمة",
                enName = "Hello / Phone",
                emoji = "📱",
                soundTip = "أَلُـو 📞",
                therapyTip = "تدريب على التواصل الصوتي المتبادل، ضع لعبة الهاتف على أذن الطفل وشجعه لترديد ألو."
            ),
            FirstWordItem(
                arabicName = "جدو",
                alternativeSaying = "الجد الحنون",
                enName = "Grandpa",
                emoji = "👴",
                soundTip = "جَـدُّو 🌻",
                therapyTip = "تمكين نطق الجيم الرصينة الشديدة المتبوعة بالدال لتعزيز الروابط الأسرية الدافئة للطفل."
            ),
            FirstWordItem(
                arabicName = "تيتة",
                alternativeSaying = "الجدة الحنونة",
                enName = "Grandma",
                emoji = "👵",
                soundTip = "تِـيـتَـة 🧺",
                therapyTip = "مخرج أسنان أمامي سهل يتسم باللطف، شجع الطفل لطلب تيتة بمجرد لمس يدها الكريمة."
            ),
            FirstWordItem(
                arabicName = "كرة",
                alternativeSaying = "كورة / طابة",
                enName = "Ball",
                emoji = "⚽",
                soundTip = "كُـورَة ⚽",
                therapyTip = "مفردة حركية مشجعة، احرص على دحرجة الكلمة بلسان مرن عند تمرير الكرة الفعلي للطفل."
            ),
            FirstWordItem(
                arabicName = "بوم",
                alternativeSaying = "وقع على الأرض",
                enName = "Boom / Fall",
                emoji = "💥",
                soundTip = "بُـومْ 💥",
                therapyTip = "استخدام التعبيرات المتفجرة الحركية اللطيفة لتمثيل وتطوير نطق عضلات الفكين بروح ترفيهية."
            ),
            FirstWordItem(
                arabicName = "واوا",
                alternativeSaying = "ألم / وجع",
                enName = "Ouch / Hurt",
                emoji = "🩹",
                soundTip = "وَاوَا 🩹",
                therapyTip = "تساعد الطفل على التعبير السريع والآمن عن مواضع الألم بمجرد الإشارة العضلية والنطق الواضح."
            ),
            FirstWordItem(
                arabicName = "دبدوب",
                alternativeSaying = "لعبة الدب",
                enName = "Teddy Bear",
                emoji = "🧸",
                soundTip = "دَبْـدُوبْ 🧸",
                therapyTip = "تكرار مقاطع الدال والباء مرتان يساهم في تدريب مخارج الطفل بنبض سمعي نغعي متناغم."
            ),
            FirstWordItem(
                arabicName = "حليب",
                alternativeSaying = "لبن دافئ",
                enName = "Milk",
                emoji = "🍼",
                soundTip = "حَـلِـيبْ 🥛",
                therapyTip = "توجيه نطق حرف الحاء الصافي مع رفع الكوب، لتعزيز طلب الغذاء الأساسي للطفل."
            ),
            FirstWordItem(
                arabicName = "كوكا",
                alternativeSaying = "جزمة / حذاء",
                enName = "Shoes",
                emoji = "👟",
                soundTip = "كُـوكَـا 👟",
                therapyTip = "كلمة بليغة وسهلة اللفظ للغاية تساعد الطفل على الاستعداد والتأهب الحركي للخروج."
            ),
            FirstWordItem(
                arabicName = "لا",
                alternativeSaying = "أرفض / ليس هذا",
                enName = "No",
                emoji = "👎",
                soundTip = "لاَ ⛔",
                therapyTip = "تمكين الطفل من مهارة الرفض الصحي السليم بلفظها القاطع بدلاً من التوتر أو الصراخ."
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDF5), Color(0xFFF0F9FF))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Core Top Action bar
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
                            if (isImmersivePagerMode) {
                                isImmersivePagerMode = false
                            } else {
                                onBack()
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
                        text = if (isImmersivePagerMode) "عرض البطاقات التفاعلية 👶" else "كلماتي الأولى ❤️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001D34)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mute/Unmute Switch
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 15.sp)
                    }

                    // View switch button
                    Button(
                        onClick = { isImmersivePagerMode = !isImmersivePagerMode },
                        colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isImmersivePagerMode) "عرض الشبكة 📱" else "عرض البطاقات 🚀",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = Color(0xFFFFE0B2).copy(alpha = 0.6f), thickness = 1.dp)

            if (!isImmersivePagerMode) {
                // =============== GRID LIST VIEW ===============
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    // Feature visual banner illustration (Purely Composed for maximum stability and visual polish)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFECE0), Color(0xFFFFF9DB))
                                    )
                                )
                        ) {
                            // Left-aligned cheerful toddler floating objects
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 20.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("👶", fontSize = 44.sp)
                                    Text("💬", fontSize = 34.sp)
                                    Text("🧸", fontSize = 38.sp)
                                }
                            }

                            // Right-aligned RTL explanatory text
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 20.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "كلماتي الأولى في عمر الطفل 👶",
                                    color = Color(0xFF5D4037),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تدريب نطق ٢٠ مفردة لتشجيع بداية التخاطب السليم",
                                    color = Color(0xFF8D6E63),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    // Grid layout of 20 child cards
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 12.dp)
                    ) {
                        itemsIndexed(firstWordsList) { index, item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.05f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.5.dp, Color(0xFFFFE0B2), RoundedCornerShape(18.dp))
                                    .clickable {
                                        initialPageIndex = index
                                        isImmersivePagerMode = true
                                        onSpeak(item.arabicName)
                                    }
                                    .testTag("first_word_grid_${item.arabicName}"),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(85.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF9EE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 54.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.arabicName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SoftBluePrimary
                                    )
                                    Text(
                                        text = item.enName,
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // =============== IMMERSIVE PAGER & RECORDER INTERACTIVITY ===============
                val pagerState = rememberPagerState(
                    initialPage = initialPageIndex,
                    pageCount = { firstWordsList.size }
                )

                val activeWord = firstWordsList.getOrNull(pagerState.currentPage) ?: firstWordsList.first()
                var isRecording by remember { mutableStateOf(false) }
                var isPlayingBack by remember { mutableStateOf(false) }

                // Automatically announce active word on swipe state change
                LaunchedEffect(pagerState.currentPage) {
                    onSpeak(activeWord.arabicName)
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Left hand: Large Interactive swiper
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Interactive Pager view
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
                                val item = firstWordsList[page]
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                        .testTag("first_words_interactive_surface"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp)
                                            .clickable { onSpeak(item.arabicName) },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // Visual graphic background
                                        Box(
                                            modifier = Modifier
                                                .size(175.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFF7ED)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(item.emoji, fontSize = 115.sp)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Bilingual and simple child sound labels
                                        Text(
                                            text = item.arabicName,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SoftBluePrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "(${item.alternativeSaying})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Surface(
                                            color = PlayfulOrange.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = item.soundTip,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PlayfulOrange,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Low swiping triggers
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("السابق ⬅️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "${pagerState.currentPage + 1} من ${firstWordsList.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001D34)
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if (pagerState.currentPage < firstWordsList.size - 1) {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        } else {
                                            Toast.makeText(context, "يا بطل! لقد أكملت كل الكلمات الأولى بنجاح 🏆", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PlayfulOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("التالي ➡️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Right hand: Speech Guidance + Toddler Microphone station
                    Column(
                        modifier = Modifier
                            .width(220.dp)
                            .fillMaxHeight()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.5.dp, Color(0xFFFFECB3), RoundedCornerShape(24.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PlayfulOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("توجيهات علاج وتخاطب نطقك:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PlayfulOrange)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFFBF0))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    activeWord.therapyTip,
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        Divider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        // Interaction: Record & playback
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🎙️ تسجيل صوت الطفل الفوري:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRecording) Color(0xFFFFEBEE) else Color(0xFFFFF4E5))
                                    .clickable {
                                        if (isRecording) {
                                            audioHelper.stopRecording()
                                            isRecording = false
                                            onSpeak("أحسنت نطق رائع")
                                            Toast.makeText(context, "تم تسجيل الصوت بنجاح! 🌟", Toast.LENGTH_SHORT).show()
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
                                        Text("🎙️ اضغط وسجل صوت طفلك", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PlayfulOrange)
                                    }
                                }
                            }

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
                                        if (isPlayingBack) "جاري الاستماع..." else "استمع لصوت طفلك 🎧",
                                        fontSize = 10.sp,
                                        color = if (audioHelper.hasRecording() && !isRecording) Color.Black else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Divider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        // Immediate Action logging onto progress ledger
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .clickable {
                                    viewModel.logSessionActivity(
                                        activityName = "كلماتي الأولى",
                                        score = 5,
                                        notes = "نجح الطفل في نطق كلمة: " + activeWord.arabicName
                                    )
                                    Toast.makeText(context, "تم رصد منجز الكلمة بنجاح! ✓", Toast.LENGTH_SHORT).show()
                                    onSpeak("سجلنا منجز طلك الحبيب بنجاح")
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
