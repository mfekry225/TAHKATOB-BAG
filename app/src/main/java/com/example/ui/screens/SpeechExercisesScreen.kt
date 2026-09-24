package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.AudioRecorderHelper
import com.example.ui.ImageState
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ExerciseStep(
    val id: Int,
    val badge: String,
    val title: String,
    val how: String,
    val why: String,
    val tip: String,
    val mouthStyle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechExercisesScreen(
    viewModel: SpeechViewModel,
    audioHelper: AudioRecorderHelper,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeChildState by viewModel.activeChild.collectAsState()

    // 20 expert speech, mouth, tongue, jaw, and blowing exercises
    val slides = remember {
        listOf(
            ExerciseStep(
                id = 1,
                badge = "تدريبات الشفاه 💋",
                title = "قبلة هوائية 💋",
                how = "ضم شفتيك للأمام ثم افتحهما كقبلة طائرة كبديل لتمرين ضم شفتين دفتين.",
                why = "تقوية عضلات الشفاه الدائرية وزيادة مرونتها للتحكم بمخارج الحروف المضمومة والشفوية مثل (الواو، الميم، الباء).",
                tip = "شجع طفلك على القيام بالتمرين أمام المرآة بشكل تفاعلي ومرح.",
                mouthStyle = "puckerLips"
            ),
            ExerciseStep(
                id = 2,
                badge = "تدريبات الشفاه 💋",
                title = "الابتسامة العريضة 😁",
                how = "شد شفتيك للجانبين كابتسامة عريضة مع إظهار الأسنان بالكامل لمدة 3 ثوانٍ.",
                why = "تنشيط عضلات الخدين وزيادة حركة الشفتين لتسهيل نطق حروف الصفير مثل (السين، الزاي، الصاد).",
                tip = "شجع طفلك على الابتسام وإظهار كامل أسنانه كأنه يستعد لالتقاط صورة تذكارية ممتعة.",
                mouthStyle = "smileTeeth"
            ),
            ExerciseStep(
                id = 3,
                badge = "تدريبات الشفاه 💋",
                title = "الابتسامة المغلقة 😊",
                how = "ابتسم بأقصى اتساع ممكن مع الحفاظ على إغلاق الشفتين تماماً دون إظهار الأسنان.",
                why = "تطوير التحكم في إغلاق الشفتين اللين وتقوية العضلات المحيطة بالفم والوجنتين.",
                tip = "اجعل التمرين يبدو كلعبة احتفاظ بسر بابتسامة صامتة ومغلقة أمام المرآة لتشجيعه.",
                mouthStyle = "smileClosed"
            ),
            ExerciseStep(
                id = 4,
                badge = "تدريبات الشفاه 💋",
                title = "إطباق الشفتين 🤫",
                how = "أطبق شفتيك بضغط متساوٍ ولطيف لتشكلا خطاً مستقيماً مشدوداً دون تحريك الأسنان.",
                why = "تقوية عضلات إغلاق الشفتين للسيطرة على مخارج الحروف الشفوية والانفجارية كحرف (الباء، الميم).",
                tip = "ضع ورقة خفيفة بين شفتي طفلك واطلب منه الاحتفاظ بها مغلقة دون استخدام أسنانه لبضع ثوانٍ.",
                mouthStyle = "closeLips"
            ),
            ExerciseStep(
                id = 5,
                badge = "تدريبات الشفاه 💋",
                title = "تمرين الشفة العلوية 🐰",
                how = "ارفع الشفة العلوية للأعلى للكشف عن الأسنان العلوية ثم أرخها بلطف.",
                why = "تدريب حركة الشفة العلوية بشكل مستقل، وهو أمر ضروري لمرونة الفم أثناء الكلام ونطق بعض الحروف.",
                tip = "اطلب من طفلك محاكاة وجه الأرنب برفع الشفة العلوية فقط لتكون الحركة مسلية وواضحة.",
                mouthStyle = "smileTeeth"
            ),
            ExerciseStep(
                id = 6,
                badge = "تدريبات اللسان 👅",
                title = "تحريك اللسان جانباً 🔄",
                how = "افتح فمك قليلاً، وأخرج طرف اللسان وحركه بالتناوب وببطء بين الزاوية اليمنى واليسرى للفم.",
                why = "تحسين مرونة عضلات اللسان الجانبية وزيادة القدرة على التحكم السريع بحركته أثناء نطق الكلمات المركبة.",
                tip = "وجّه طفلك للتنقل بلسانه من اليمين إلى اليسار بالتناوب وبإيقاع ممتع وهادئ.",
                mouthStyle = "tongueOut"
            ),
            ExerciseStep(
                id = 7,
                badge = "تدريبات اللسان 👅",
                title = "رفع اللسان للأعلى 🍯",
                how = "افتح فمك بشكل مريح، ثم ارفع طرف اللسان محاولاً ملامسة منتصف الشفة العلوية نحو الأعلى.",
                why = "رفع كفاءة اللسان وتعوده على الارتفاع للأعلى بشكل مستقل عن الفك، وهو حجر الأساس لنطق حروف (اللام، النون، الراء).",
                tip = "ضع لمسة صغيرة من العسل أو المربى على الشفة العلوية لطفلك لتشجيعه على رفع اللسان لاعتصارها بطريقة طبيعية.",
                mouthStyle = "tongueUp"
            ),
            ExerciseStep(
                id = 8,
                badge = "تدريبات اللسان 👅",
                title = "لمس الذقن 👇",
                how = "أخرج لسانك لأسفل بأقصى امتداد مريح لك محاولاً الاقتراب من الذقن دون إمالة الرقبة للأمام.",
                why = "إطالة لجام اللسان وزيادة القدرة على مد لسان الطفل بشكل طولي وتخفيف القيود المفصلية.",
                tip = "ضع إصبعك برفق أسفل ذقن الطفل واطلب منه محاولة لمسه بطرف لسانه بشكل تفاعلي.",
                mouthStyle = "tongueDown"
            ),
            ExerciseStep(
                id = 9,
                badge = "تدريبات اللسان 👅",
                title = "تلميع الأسنان باللسان 🪥",
                how = "مرر طرف اللسان بحركة دائرية هادئة وشاملة بين الأسنان والشفاه من الداخل.",
                why = "تحسين الإدراك الحسي وتدريب حافة اللسان على الدوران والمرونة الفائقة داخل تجويف الفم.",
                tip = "اطلب من طفلك تحريك لسانه بشكل دائري ويمسح به أسنانه من الداخل كأنها لعبة تلميع ممتعة.",
                mouthStyle = "tongueDown"
            ),
            ExerciseStep(
                id = 10,
                badge = "تدريبات اللسان 👅",
                title = "صوت حوافر الحصان 🐎",
                how = "ألصق لسانك بالكامل بسقف الحلق، ثم اسحبه بقوة لأسفل فجأة لتصدر صوتاً شبيهاً بقرقعة حوافر الحصان.",
                why = "تمرين أساسي ومثبت لتدريب اللسان على الارتكاز وتسهيل نطق الأصوات التكرارية مثل حرف (الراء).",
                tip = "شجع طفلك على إصدار صوت حوافر الحصان (طق.. طق.. طق) واجعله تحدياً ممتعاً لأقوى صوت.",
                mouthStyle = "tongueUp"
            ),
            ExerciseStep(
                id = 11,
                badge = "تدريبات اللسان 👅",
                title = "تمرين الأضراس الخلفية 🦷",
                how = "المس بلسانك الأسنان الخلفية والأضراس من الداخل بالتناوب يميناً ويساراً.",
                why = "تدريب عضلات مؤخرة اللسان والمنطقة الصمامية الفمية اللازمة لمخارج حروف مثل (القاف، الكاف).",
                tip = "اطلب من طفلك ملامسة أضراسه الخلفية بلسانه بالتناوب يميناً ويساراً أمام المرآة.",
                mouthStyle = "tongueUp"
            ),
            ExerciseStep(
                id = 12,
                badge = "تدريبات اللسان 👅",
                title = "بسط اللسان المسترخي 🥄",
                how = "افتح فمك واجعل لسانك منبسطاً ومستلقياً تماماً في قاع الفم دون تشنج أو حركة.",
                why = "تخفيف تشنجات عضلات اللسان العميقة وتحقيق الارتخاء اللازم للوقاية من التلعثم أثناء النطق.",
                tip = "اطلب من طفلك فتح فمه وإبقاء لسانه ساكناً ومسترخياً تماماً لثلاث ثوانٍ كأنه نائم في قاع الفم.",
                mouthStyle = "tongueOut"
            ),
            ExerciseStep(
                id = 13,
                badge = "تدريبات الفك 🦁",
                title = "تمرين فتح الفم 🦁",
                how = "افتح فمك بأقصى اتساع عمودي مريح ومناسب لمدة ثلاث ثوانٍ متواصلة.",
                why = "تحسين المدى الحركي للمفاصل الصدغية للفك وتهيئة مخارج الهواء الملائمة لحروف المد وبخاصة الألف (آ).",
                tip = "افتح فمك مع طفلك وادرجا ذلك كلعبة ممتعة ومرحة أمام المرآة لتشجيعه على اتساع الفم.",
                mouthStyle = "openWide"
            ),
            ExerciseStep(
                id = 14,
                badge = "تدريبات الفك 🦁",
                title = "تمثيل مضغ الطعام 🍬",
                how = "شجع الطفل على إبقاء شفتيه مغلقتين وتحريك فكيه بشكل دائري كأنه يمضغ قطعة طعام مستديرة.",
                why = "تقوية العضلات الماضغة وعضلات الوجنتين، وزيادة التنسيق بين البلع والتنفس المنضبط.",
                tip = "تداول مع طفلك نكهات افتراضية مضحكة وابدآ المحاكاة والمضغ سوياً بروح مرحة.",
                mouthStyle = "closeLips"
            ),
            ExerciseStep(
                id = 15,
                badge = "تدريبات الفك 🦁",
                title = "حركة الفك السريعة 🐊",
                how = "افتح الفم وأغلقه بالتناوب وبانتظام وسرعة خفيفة متناسقة.",
                why = "مرونة عضلات الفك السفلي وتدريبها على الصعود والنزول السريع اللازم للتحدث بانسيابية.",
                tip = "استخدم يديك لتمثيل فم التمساح واجعل طفلك يقلد حركة اليد بالفم بحماس وتفاعل.",
                mouthStyle = "openWide"
            ),
            ExerciseStep(
                id = 16,
                badge = "تدريبات الفك 🦁",
                title = "تحريك الفك لليمين 👉",
                how = "حرك الفك السفلي بلطف وبطء شديد نحو الجانب الأيمن فقط مع الحفاظ على ثبات الرأس والرقبة.",
                why = "تحقيق توازن العضلات الجانبية للفك والوجه وتحسين جودة خروج مخارج الحروف الشفوية.",
                tip = "المس برفق وجنة طفلك اليمنى لمساعدته في تحديد الاتجاه السليم للحركة بشكل مريح.",
                mouthStyle = "jawRight"
            ),
            ExerciseStep(
                id = 17,
                badge = "تدريبات الفك 🦁",
                title = "تحريك الفك ليسار 👈",
                how = "حرك الفك السفلي برفق وببطء نحو الجانب الأيسر، محاولاً الحفاظ على ثبات وجهك ورقبتك.",
                why = "تقوية عضلات ورباط الجانب الأيسر لمفصل الفك لتسهيل حركات الضم والكسر في الكلمات.",
                tip = "قم بالحركة مع طفلك بالتزامن واجعلاها ممتعة بابتسامة تشجيعية في النهاية.",
                mouthStyle = "jawLeft"
            ),
            ExerciseStep(
                id = 18,
                badge = "تدريبات النفخ والتنفس 🎈",
                title = "نفخ البالون 🎈",
                how = "اجمع الهواء داخل فمك وانفخ خديك كأنك تنفخ بالوناً، واحبس الهواء لثانيتين.",
                why = "تقوية عضلات الخدين والشفتين والتحكم في مخرج الهواء، وهو أمر أساسي لنطق الحروف بوضوح.",
                tip = "اضغط بلطف بأصبعك على خد الطفل وهو منفوخ ليخرج الهواء كأنه فرقعة بالون لتشجيعه على التكرار.",
                mouthStyle = "puffCheeks"
            ),
            ExerciseStep(
                id = 19,
                badge = "تدريبات النفخ والتنفس 🎈",
                title = "النفخ وإطفاء الشمعة 🎂",
                how = "خذ شهيقاً من الأنف، ثم ضُمّ الشفتين وانفخ تيار الهواء للأمام بقوة كأنك تطفئ شمعة.",
                why = "تدريب الطفل على توجيه تيار الهواء الخارج من الفم بشكل مستمر ومحدد، وهو مفيد للأصوات الاحتكاكية والانفجارية.",
                tip = "ضع ورقة خفيفة أمام فم الطفل واجعله يلاحظ كيف تتحرك بفعل تيار الهواء الخارج من فمه لتشجيعه على مواصلة التمرين.",
                mouthStyle = "blowCandle"
            ),
            ExerciseStep(
                id = 20,
                badge = "تدريبات النفخ والتنفس 🎈",
                title = "تمرين نفخ الريشة 🌬️",
                how = "اضم شفتيك وانفخ زفيراً طويلاً وبطيئاً بشكل متصل وهادئ لأطول فترة ممكنة.",
                why = "زيادة سعة الرئتين والتحكم في طول زفير الهواء ومعدله، مما يقلل من التلعثم ويسهل نطق الجمل الطويلة.",
                tip = "ضع ريشة صغيرة أو قطعة قطن واجعل طفلك ينفخها برفق ليحافظ عليها طائرة في الهواء لأطول فترة ممكنة.",
                mouthStyle = "blowCandle"
            )
        )
    }

    var currentStepIdx by remember { mutableStateOf(0) }
    val currentStep = slides[currentStepIdx]

    // Completed states for 20 stars
    val completedSteps = remember { mutableStateListOf(*Array(slides.size) { false }) }

    // Camera mirror active state
    var isCameraActive by remember { mutableStateOf(false) }
    var showVictoryModal by remember { mutableStateOf(false) }

    // Defaults to "illustration" which is MiloFace (totally robust and beautiful)
    var selectedVisualTab by remember { mutableStateOf("illustration") }

    // Children voice states
    var isRecording by remember { mutableStateOf(false) }
    var isPlayingBack by remember { mutableStateOf(false) }

    // Camera launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (isGranted) {
                isCameraActive = true
            } else {
                Toast.makeText(context, "المرآة التفاعلية تشترط فحص الكاميرا للعمل والمطابقة", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Speech reading assistant speaks instructions on step changes automatically (Text to Speech)
    LaunchedEffect(currentStepIdx) {
        if (isRecording) {
            audioHelper.stopRecording()
            isRecording = false
        }
        if (isPlayingBack) {
            audioHelper.stopPlaying()
            isPlayingBack = false
        }
        delay(300)
        onSpeak("${currentStep.title}. ${currentStep.how}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تمارين النطق والتخاطب 🔊",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "مسار تدريب متكامل لتقوية مخارد وعضلات النطق",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Medical Mirror Toggle in Top Bar
                    Button(
                        onClick = {
                            SoundEffectsHelper.playClick()
                            if (isCameraActive) {
                                isCameraActive = false
                            } else {
                                if (hasCameraPermission) {
                                    isCameraActive = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCameraActive) Color(0xFFE11D48) else Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isCameraActive) "إغلاق المرآة" else "مرآة النطق 📷",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main split screen row layout: Left side main training content, Right side camera preview (or vice-versa)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left section: Main Training Card Screen Area
                    Card(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        if (isPortrait) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Large Illustration Container to maximize space
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(2.dp, Color(0xFF4F46E5), RoundedCornerShape(16.dp))
                                        .clickable { onSpeak("${currentStep.title}. ${currentStep.how}") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MiloFace(
                                            mouthStyle = currentStep.mouthStyle,
                                            isPuffed = currentStep.mouthStyle == "puffCheeks",
                                            isHappy = currentStep.mouthStyle == "cheerVictory",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Interactive Text Details Column
                                Column(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(currentStep.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                        }
                                        Text(
                                            text = "تمرين ${currentStepIdx + 1} من ${slides.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Text(
                                        text = currentStep.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )

                                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                    Text(
                                        text = "كيف تفعل التمرين:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = currentStep.how,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF475569)
                                    )

                                    Text(
                                        text = "الفائدة العلاجية المساعدة:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        text = currentStep.why,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF047857)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(8.dp)
                                    ) {
                                        Text(currentStep.tip, fontSize = 11.sp, color = Color(0xFF78350F), lineHeight = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Voice recording module
                                    ClientRecordingStation(
                                        currentStep = currentStep,
                                        audioHelper = audioHelper,
                                        isRecording = isRecording,
                                        isPlayingBack = isPlayingBack,
                                        onRecordStarted = { isRecording = true },
                                        onRecordStopped = { isRecording = false },
                                        onPlaybackStarted = { isPlayingBack = true },
                                        onPlaybackStopped = { isPlayingBack = false },
                                        onSpeak = onSpeak
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val isStepCompleted = completedSteps[currentStepIdx]
                                    Button(
                                        onClick = {
                                            completedSteps[currentStepIdx] = true
                                            SoundEffectsHelper.playSuccess()
                                            onSpeak("أحسنت! حصلت على نجمة.")
                                            if (completedSteps.all { it }) {
                                                if (activeChildState != null) {
                                                    viewModel.logSessionActivity(
                                                        activityName = "تمارين النطق والتخاطب",
                                                        score = 100,
                                                        notes = "أكمل الطفل مسار التدريب بنجاح."
                                                    )
                                                }
                                                showVictoryModal = true
                                            } else {
                                                coroutineScope.launch {
                                                    delay(1200)
                                                    if (currentStepIdx < slides.size - 1) {
                                                        currentStepIdx++
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isStepCompleted) Color(0xFFFBBF24) else Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (isStepCompleted) "نجمتك الذهبية محققة! ⭐" else "أنجزت التمرين! احصل على نجمة ⭐",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isStepCompleted) Color(0xFF1E293B) else Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            // Landscape layout with side-by-side details
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Left Side: Large illustration Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(3.dp, Color(0xFF4F46E5), RoundedCornerShape(16.dp))
                                        .clickable { onSpeak("${currentStep.title}. ${currentStep.how}") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MiloFace(
                                        mouthStyle = currentStep.mouthStyle,
                                        isPuffed = currentStep.mouthStyle == "puffCheeks",
                                        isHappy = currentStep.mouthStyle == "cheerVictory",
                                        modifier = Modifier.fillMaxSize().padding(14.dp)
                                    )
                                }

                                // Right Side: Details scrollable
                                Column(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(currentStep.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        }
                                        Text(
                                            text = "البطاقة ${currentStepIdx + 1} / ${slides.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    Text(
                                        text = currentStep.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )

                                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                    GuideDescriptionSection(currentStep = currentStep)

                                    Spacer(modifier = Modifier.height(4.dp))

                                    ClientRecordingStation(
                                        currentStep = currentStep,
                                        audioHelper = audioHelper,
                                        isRecording = isRecording,
                                        isPlayingBack = isPlayingBack,
                                        onRecordStarted = { isRecording = true },
                                        onRecordStopped = { isRecording = false },
                                        onPlaybackStarted = { isPlayingBack = true },
                                        onPlaybackStopped = { isPlayingBack = false },
                                        onSpeak = onSpeak
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val isStepCompleted = completedSteps[currentStepIdx]
                                    Button(
                                        onClick = {
                                            completedSteps[currentStepIdx] = true
                                            SoundEffectsHelper.playSuccess()
                                            onSpeak("أحسنت! حصلت على نجمة.")
                                            if (completedSteps.all { it }) {
                                                if (activeChildState != null) {
                                                    viewModel.logSessionActivity(
                                                        activityName = "تمارين النطق والتخاطب",
                                                        score = 100,
                                                        notes = "أكمل الطفل مسار التدريب بنجاح."
                                                    )
                                                }
                                                showVictoryModal = true
                                            } else {
                                                coroutineScope.launch {
                                                    delay(1200)
                                                    if (currentStepIdx < slides.size - 1) {
                                                        currentStepIdx++
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isStepCompleted) Color(0xFFFBBF24) else Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (isStepCompleted) "نجمة التمرين محققة بنجاح باهل! ⭐" else "أوجزت المعجزة! احصل على نجمتك ⭐",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isStepCompleted) Color(0xFF1E293B) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right section: Toggleable Live Clinic Mirror sidebar
                    AnimatedVisibility(
                        visible = isCameraActive,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut(),
                        modifier = if (isPortrait) Modifier.fillMaxWidth().weight(1f) else Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxSize(),
                            border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مرآة العيادة النطقية 🤳",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        IconButton(
                                            onClick = {
                                                SoundEffectsHelper.playClick()
                                                isCameraActive = false
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "إغلاق المرآة",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "دع طفلك يتأمل حركاته وينسق مخارج حروفه بملاحظة ومقارنة نطق فمه مع الرسم التوضيحي باليسار.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 13.sp
                                    )
                                }

                                // Interactive live camera container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black)
                                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasCameraPermission) {
                                        CameraPreviewView(modifier = Modifier.fillMaxSize())
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(text = "📷", fontSize = 32.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "يرجى تفعيل صلاحية الكاميرا", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom row details matching progress
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (currentStepIdx > 0) {
                                currentStepIdx--
                                SoundEffectsHelper.playPrevious()
                            }
                        },
                        enabled = currentStepIdx > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color.LightGray.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(min = 90.dp)
                    ) {
                        Text("السابق ➡️", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = "البطاقة ${currentStepIdx + 1} من ${slides.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStepIdx < slides.size - 1) {
                                currentStepIdx++
                                SoundEffectsHelper.playNext()
                            }
                        },
                        enabled = currentStepIdx < slides.size - 1,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(min = 90.dp)
                    ) {
                        Text("⬅️ التالي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // COMPLETE ALL ACHIEVEMENT VICTORY DIALOG
            // ==========================================
            AnimatedVisibility(
                visible = showVictoryModal,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 380.dp)
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
                                Text("🏆🎉", fontSize = 42.sp)
                                Text(
                                    text = "تهانينا! لقد أكملت التدريب بنجاح! 🥳",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "أكملت جميع بطاقات التخاطب الـ 20 بنجاح باهر وسرور متبادل!",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = "تساعد تمارين اليوم في مرونة وتحفيز عضلات النطق المختلفة وإخراج الحروف بشكل سليم ومستمر.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.DarkGray
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "تم بث وتسجيل هذا الإنجاز الأسري في سجلات تطور الطفل لدى الأخصائيين وأولياء الأمور! 🎓📈",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF065F46),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showVictoryModal = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إغلاق", color = Color.Gray, fontSize = 12.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        completedSteps.fill(false)
                                        currentStepIdx = 0
                                        showVictoryModal = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(2.5f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("العب مجدداً برفقة عائلتي! 🔁", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiloFace(
    mouthStyle: String,
    isPuffed: Boolean,
    isHappy: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2.6f

        val strokeStyleThin = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        val strokeStyleMedium = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        val strokeStyleThick = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)

        // Hair Accent
        drawArc(
            color = Color(0xFF653D1E),
            startAngle = 160f,
            sweepAngle = 220f,
            useCenter = true,
            topLeft = Offset(centerX - radius * 1.1f, centerY - radius * 1.25f),
            size = Size(radius * 2.2f, radius * 2.2f)
        )

        // Ears
        drawCircle(color = Color(0xFFFFD4B2), radius = radius * 0.25f, center = Offset(centerX - radius * 1.05f, centerY))
        drawCircle(color = Color(0xFFFBC396), radius = radius * 0.15f, center = Offset(centerX - radius * 1.05f, centerY))
        drawCircle(color = Color(0xFFFFD4B2), radius = radius * 0.25f, center = Offset(centerX + radius * 1.05f, centerY))
        drawCircle(color = Color(0xFFFBC396), radius = radius * 0.15f, center = Offset(centerX + radius * 1.05f, centerY))

        // Face Round Base
        drawCircle(color = Color(0xFFFFD4B2), radius = radius, center = Offset(centerX, centerY))

        // Rosy Cheeks (Puffed or cozy state)
        val cheekRadius = if (isPuffed) radius * 0.28f else radius * 0.16f
        drawCircle(
            color = Color(0xFFFF9AA2).copy(alpha = 0.65f),
            radius = cheekRadius,
            center = Offset(centerX - radius * 0.58f, centerY + radius * 0.35f)
        )
        drawCircle(
            color = Color(0xFFFF9AA2).copy(alpha = 0.65f),
            radius = cheekRadius,
            center = Offset(centerX + radius * 0.58f, centerY + radius * 0.35f)
        )

        // Eyes
        val eyeY = centerY - radius * 0.2f
        val leftEyeX = centerX - radius * 0.38f
        val rightEyeX = centerX + radius * 0.38f
        val eyeRadius = radius * 0.12f

        if (isHappy) {
            val eyePathLeft = Path().apply {
                moveTo(leftEyeX - eyeRadius, eyeY)
                quadraticTo(leftEyeX, eyeY - eyeRadius * 1.2f, leftEyeX + eyeRadius, eyeY)
            }
            drawPath(path = eyePathLeft, color = Color(0xFF2D3748), style = strokeStyleThick)

            val eyePathRight = Path().apply {
                moveTo(rightEyeX - eyeRadius, eyeY)
                quadraticTo(rightEyeX, eyeY - eyeRadius * 1.2f, rightEyeX + eyeRadius, eyeY)
            }
            drawPath(path = eyePathRight, color = Color(0xFF2D3748), style = strokeStyleThick)
        } else {
            drawCircle(color = Color.White, radius = eyeRadius, center = Offset(leftEyeX, eyeY))
            drawCircle(color = Color(0xFF2D3748), radius = eyeRadius * 0.55f, center = Offset(leftEyeX, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(leftEyeX - eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))

            drawCircle(color = Color.White, radius = eyeRadius, center = Offset(rightEyeX, eyeY))
            drawCircle(color = Color(0xFF2D3748), radius = eyeRadius * 0.55f, center = Offset(rightEyeX, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(rightEyeX - eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))
        }

        // Eyebrows
        val eyebrowPathLeft = Path().apply {
            moveTo(leftEyeX - eyeRadius * 1.1f, eyeY - eyeRadius * 1.6f)
            quadraticTo(leftEyeX, eyeY - eyeRadius * 2.1f, leftEyeX + eyeRadius * 1.1f, eyeY - eyeRadius * 1.6f)
        }
        drawPath(path = eyebrowPathLeft, color = Color(0xFF653D1E), style = strokeStyleThin)

        val eyebrowPathRight = Path().apply {
            moveTo(rightEyeX - eyeRadius * 1.1f, eyeY - eyeRadius * 1.6f)
            quadraticTo(rightEyeX, eyeY - eyeRadius * 2.1f, rightEyeX + eyeRadius * 1.1f, eyeY - eyeRadius * 1.6f)
        }
        drawPath(path = eyebrowPathRight, color = Color(0xFF653D1E), style = strokeStyleThin)

        // Nose
        val nosePath = Path().apply {
            moveTo(centerX - radius * 0.1f, centerY + radius * 0.05f)
            quadraticTo(centerX, centerY - radius * 0.05f, centerX + radius * 0.1f, centerY + radius * 0.05f)
            quadraticTo(centerX, centerY + radius * 0.12f, centerX - radius * 0.1f, centerY + radius * 0.05f)
        }
        drawPath(path = nosePath, color = Color(0xFFFFB28C))

        // Dynamic Mouth, Laryngeal and Lingual Gestures
        val mouthY = centerY + radius * 0.45f
        when (mouthStyle) {
            "openWide" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.25f, mouthY - radius * 0.35f),
                    size = Size(radius * 0.5f, radius * 0.7f)
                )
                // Teeth
                drawRect(
                    color = Color.White,
                    topLeft = Offset(centerX - radius * 0.16f, mouthY - radius * 0.35f),
                    size = Size(radius * 0.32f, radius * 0.08f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(centerX - radius * 0.14f, mouthY + radius * 0.27f),
                    size = Size(radius * 0.28f, radius * 0.08f)
                )
                // Red Tongue
                drawArc(
                    color = Color(0xFFF25C54),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(centerX - radius * 0.18f, mouthY + radius * 0.12f),
                    size = Size(radius * 0.36f, radius * 0.22f)
                )
                // Border lip
                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.27f, mouthY - radius * 0.37f),
                    size = Size(radius * 0.54f, radius * 0.74f),
                    style = strokeStyleMedium
                )
            }
            "closeLips" -> {
                val lipPath = Path().apply {
                    moveTo(centerX - radius * 0.45f, mouthY)
                    quadraticTo(centerX, mouthY - radius * 0.04f, centerX + radius * 0.45f, mouthY)
                }
                drawPath(path = lipPath, color = Color(0xFFF07167), style = strokeStyleThick)
            }
            "puckerLips" -> {
                drawCircle(color = Color(0xFFF07167), radius = radius * 0.16f, center = Offset(centerX, mouthY))
                drawCircle(color = Color(0xFF9E1C1C), radius = radius * 0.1f, center = Offset(centerX, mouthY))
                drawCircle(color = Color.Black, radius = radius * 0.05f, center = Offset(centerX, mouthY))
            }
            "smileClosed" -> {
                val smilePath = Path().apply {
                    moveTo(centerX - radius * 0.5f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.28f, centerX + radius * 0.5f, mouthY - radius * 0.15f)
                }
                drawPath(path = smilePath, color = Color(0xFFF07167), style = strokeStyleThick)
            }
            "smileTeeth" -> {
                val smilePath = Path().apply {
                    moveTo(centerX - radius * 0.55f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.32f, centerX + radius * 0.55f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY - radius * 0.18f, centerX - radius * 0.55f, mouthY - radius * 0.15f)
                    close()
                }
                drawPath(path = smilePath, color = Color(0xFF781D1D))

                val teethPath = Path().apply {
                    moveTo(centerX - radius * 0.53f, mouthY - radius * 0.14f)
                    quadraticTo(centerX, mouthY + radius * 0.11f, centerX + radius * 0.53f, mouthY - radius * 0.14f)
                    quadraticTo(centerX, mouthY - radius * 0.02f, centerX - radius * 0.53f, mouthY - radius * 0.14f)
                    close()
                }
                drawPath(path = teethPath, color = Color.White)

                val lipStrokePath = Path().apply {
                    moveTo(centerX - radius * 0.56f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.36f, centerX + radius * 0.56f, mouthY - radius * 0.15f)
                }
                drawPath(path = lipStrokePath, color = Color(0xFFF07167), style = strokeStyleMedium)
            }
            "tongueOut" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.22f, mouthY - radius * 0.18f),
                    size = Size(radius * 0.44f, radius * 0.36f)
                )
                drawRoundRect(
                    color = Color(0xFFFF70A6),
                    topLeft = Offset(centerX - radius * 0.12f, mouthY - radius * 0.05f),
                    size = Size(radius * 0.24f, radius * 0.42f),
                    cornerRadius = CornerRadius(radius * 0.12f)
                )
                drawRoundRect(
                    color = Color(0xFFE05780),
                    topLeft = Offset(centerX - radius * 0.12f, mouthY - radius * 0.05f),
                    size = Size(radius * 0.24f, radius * 0.42f),
                    cornerRadius = CornerRadius(radius * 0.12f),
                    style = strokeStyleThin
                )
                drawLine(
                    color = Color(0xFFD94E73),
                    start = Offset(centerX, mouthY),
                    end = Offset(centerX, mouthY + radius * 0.25f),
                    strokeWidth = 3f
                )
                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.24f, mouthY - radius * 0.2f),
                    size = Size(radius * 0.48f, radius * 0.40f),
                    style = strokeStyleMedium
                )
            }
            "tongueUp" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.25f, mouthY - radius * 0.2f),
                    size = Size(radius * 0.5f, radius * 0.4f)
                )
                val tonguePath = Path().apply {
                    moveTo(centerX - radius * 0.14f, mouthY - radius * 0.05f)
                    quadraticTo(centerX, mouthY - radius * 0.42f, centerX + radius * 0.14f, mouthY - radius * 0.05f)
                    quadraticTo(centerX, mouthY + radius * 0.10f, centerX - radius * 0.14f, mouthY - radius * 0.05f)
                    close()
                }
                drawPath(path = tonguePath, color = Color(0xFFFF70A6))
                drawPath(path = tonguePath, color = Color(0xFFE05780), style = strokeStyleThin)

                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.27f, mouthY - radius * 0.22f),
                    size = Size(radius * 0.54f, radius * 0.44f),
                    style = strokeStyleMedium
                )
            }
            "tongueDown" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.25f, mouthY - radius * 0.2f),
                    size = Size(radius * 0.5f, radius * 0.4f)
                )
                val tonguePath = Path().apply {
                    moveTo(centerX - radius * 0.14f, mouthY + radius * 0.05f)
                    quadraticTo(centerX, mouthY + radius * 0.42f, centerX + radius * 0.14f, mouthY + radius * 0.05f)
                    quadraticTo(centerX, mouthY - radius * 0.10f, centerX - radius * 0.14f, mouthY + radius * 0.05f)
                    close()
                }
                drawPath(path = tonguePath, color = Color(0xFFFF70A6))
                drawPath(path = tonguePath, color = Color(0xFFE05780), style = strokeStyleThin)

                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.27f, mouthY - radius * 0.22f),
                    size = Size(radius * 0.54f, radius * 0.44f),
                    style = strokeStyleMedium
                )
            }
            "tongueRight" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.25f, mouthY - radius * 0.18f),
                    size = Size(radius * 0.5f, radius * 0.36f)
                )
                val tonguePath = Path().apply {
                    moveTo(centerX - radius * 0.1f, mouthY - radius * 0.05f)
                    quadraticTo(centerX + radius * 0.36f, mouthY - radius * 0.22f, centerX + radius * 0.32f, mouthY + radius * 0.1f)
                    quadraticTo(centerX - radius * 0.1f, mouthY + radius * 0.20f, centerX - radius * 0.1f, mouthY - radius * 0.05f)
                    close()
                }
                drawPath(path = tonguePath, color = Color(0xFFFF70A6))
                drawPath(path = tonguePath, color = Color(0xFFE05780), style = strokeStyleThin)

                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.27f, mouthY - radius * 0.21f),
                    size = Size(radius * 0.54f, radius * 0.42f),
                    style = strokeStyleMedium
                )
            }
            "tongueLeft" -> {
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.25f, mouthY - radius * 0.18f),
                    size = Size(radius * 0.5f, radius * 0.36f)
                )
                val tonguePath = Path().apply {
                    moveTo(centerX + radius * 0.1f, mouthY - radius * 0.05f)
                    quadraticTo(centerX - radius * 0.36f, mouthY - radius * 0.22f, centerX - radius * 0.32f, mouthY + radius * 0.1f)
                    quadraticTo(centerX + radius * 0.1f, mouthY + radius * 0.20f, centerX + radius * 0.1f, mouthY - radius * 0.05f)
                    close()
                }
                drawPath(path = tonguePath, color = Color(0xFFFF70A6))
                drawPath(path = tonguePath, color = Color(0xFFE05780), style = strokeStyleThin)

                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.27f, mouthY - radius * 0.21f),
                    size = Size(radius * 0.54f, radius * 0.42f),
                    style = strokeStyleMedium
                )
            }
            "puffCheeks" -> {
                drawCircle(color = Color(0xFFF07167), radius = radius * 0.09f, center = Offset(centerX, mouthY))
                drawCircle(color = Color(0xFFDF5A50), radius = radius * 0.09f, center = Offset(centerX, mouthY), style = strokeStyleThin)
            }
            "blowCandle" -> {
                drawCircle(color = Color(0xFF9E1C1C), radius = radius * 0.07f, center = Offset(centerX, mouthY))
                drawCircle(color = Color(0xFFF07167), radius = radius * 0.07f, center = Offset(centerX, mouthY), style = strokeStyleMedium)

                drawLine(color = Color(0xFF93C5FD), start = Offset(centerX - radius * 0.12f, mouthY + radius * 0.12f), end = Offset(centerX - radius * 0.4f, mouthY + radius * 0.4f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF93C5FD), start = Offset(centerX + radius * 0.12f, mouthY + radius * 0.12f), end = Offset(centerX + radius * 0.4f, mouthY + radius * 0.4f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF93C5FD), start = Offset(centerX, mouthY + radius * 0.15f), end = Offset(centerX, mouthY + radius * 0.5f), strokeWidth = 5f, cap = StrokeCap.Round)
            }
            "jawRight" -> {
                val shiftX = radius * 0.18f
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.18f + shiftX, mouthY - radius * 0.15f),
                    size = Size(radius * 0.36f, radius * 0.3f)
                )
                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.2f + shiftX, mouthY - radius * 0.17f),
                    size = Size(radius * 0.4f, radius * 0.34f),
                    style = strokeStyleMedium
                )
            }
            "jawLeft" -> {
                val shiftX = -radius * 0.18f
                drawOval(
                    color = Color(0xFF9E1C1C),
                    topLeft = Offset(centerX - radius * 0.18f + shiftX, mouthY - radius * 0.15f),
                    size = Size(radius * 0.36f, radius * 0.3f)
                )
                drawOval(
                    color = Color(0xFFF07167),
                    topLeft = Offset(centerX - radius * 0.2f + shiftX, mouthY - radius * 0.17f),
                    size = Size(radius * 0.4f, radius * 0.34f),
                    style = strokeStyleMedium
                )
            }
            "cheerVictory" -> {
                val laughPath = Path().apply {
                    moveTo(centerX - radius * 0.55f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.48f, centerX + radius * 0.55f, mouthY - radius * 0.15f)
                    close()
                }
                drawPath(path = laughPath, color = Color(0xFF9E1C1C))

                val tonguePath = Path().apply {
                    moveTo(centerX - radius * 0.3f, mouthY + radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.05f, centerX + radius * 0.3f, mouthY + radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.45f, centerX - radius * 0.3f, mouthY + radius * 0.15f)
                    close()
                }
                drawPath(path = tonguePath, color = Color(0xFFFF70A6))

                val outerLipPath = Path().apply {
                    moveTo(centerX - radius * 0.55f, mouthY - radius * 0.15f)
                    quadraticTo(centerX, mouthY + radius * 0.52f, centerX + radius * 0.55f, mouthY - radius * 0.15f)
                }
                drawPath(path = outerLipPath, color = Color(0xFFF07167), style = strokeStyleMedium)
            }
        }
    }
}

@Composable
fun RenderAiStateSquare(
    currentImageState: ImageState,
    currentStep: ExerciseStep,
    viewModel: SpeechViewModel
) {
    val cleanPrompt = currentStep.title
        .replace(Regex("[^\u0621-\u064A\\s\\d]"), "")
        .trim()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (currentImageState) {
            is ImageState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text("🎨", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Imagen ذكي",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.generateImageForStep(currentStep.id, cleanPrompt, currentStep.mouthStyle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("إنشــاء 🪄", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is ImageState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4F46E5), strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يرسم مخرج النطق... 🎨", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E46E5))
                }
            }
            is ImageState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = currentImageState.bitmap.asImageBitmap(),
                        contentDescription = "AI Image representation of exercise",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                            .clickable { viewModel.generateImageForStep(currentStep.id, cleanPrompt, currentStep.mouthStyle) }
                            .padding(4.dp)
                    ) {
                        Text("🔄", fontSize = 10.sp)
                    }
                }
            }
            is ImageState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text("⚠️ لم يكتمل", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.generateImageForStep(currentStep.id, cleanPrompt, currentStep.mouthStyle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("إعادة", fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GuideDescriptionSection(currentStep: ExerciseStep) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "دليل التدريب والتوجيه الأسري:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF0F172A)
            )
        }

        Column {
            Text(text = "طريقة التدريب ومخارج الحروف:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = currentStep.how, fontSize = 11.sp, color = Color(0xFF334155), lineHeight = 15.sp)
        }

        Column {
            Text(text = "القيمة والتفسير العلاجي الهام للأخصائيين:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF10B981))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = currentStep.why, fontSize = 11.sp, color = Color(0xFF047857), lineHeight = 15.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFFAF0))
                .padding(8.dp)
        ) {
            Column {
                Text(text = "تلميحة التشجيع والمرح للوالدين المعالجين:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFD97706))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = currentStep.tip, fontSize = 10.sp, color = Color(0xFF78350F), lineHeight = 14.sp)
            }
        }
    }
}

@Composable
fun ClientRecordingStation(
    currentStep: ExerciseStep,
    audioHelper: AudioRecorderHelper,
    isRecording: Boolean,
    isPlayingBack: Boolean,
    onRecordStarted: () -> Unit,
    onRecordStopped: () -> Unit,
    onPlaybackStarted: () -> Unit,
    onPlaybackStopped: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val hasLocalRecording = audioHelper.hasRecording()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "🎙️ مسجل الصوت التفاعلي لمتابعة مخارج الحروف",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Record mic button
            Button(
                onClick = {
                    if (isRecording) {
                        try {
                            audioHelper.stopRecording()
                            onRecordStopped()
                            Toast.makeText(context, "تم حفظ التسجيل الصوتي بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        onRecordStarted()
                        try {
                            audioHelper.startRecording()
                        } catch (e: Exception) {
                            onRecordStopped()
                            Toast.makeText(context, "الرجاء التأكد من صلاحية الميكروفون", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                    contentColor = if (isRecording) Color.White else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isRecording) "جاري التسجيل... 🟥" else "سجل صوت طفلك 🎙️",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Play icon
            Button(
                onClick = {
                    if (isPlayingBack) {
                        audioHelper.stopPlaying()
                        onPlaybackStopped()
                    } else {
                        if (hasLocalRecording) {
                            onPlaybackStarted()
                            audioHelper.startPlaying {
                                onPlaybackStopped()
                            }
                        } else {
                            onSpeak("لم تسجل نطق طفلك بعد. قم بضغط الزر وبدء النطق ومحاذاته مع الطفل.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlayingBack) Color(0xFF6366F1) else {
                        if (hasLocalRecording) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)
                    },
                    contentColor = if (isPlayingBack) Color.White else {
                        if (hasLocalRecording) Color(0xFF065F46) else Color.Gray
                    }
                ),
                border = BorderStroke(1.dp, if (hasLocalRecording) Color(0xFF34D399) else Color.LightGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(0.9f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isPlayingBack) "إيقاف 🛑" else {
                            if (hasLocalRecording) "استمع لنطق طفلك 🎧" else "الاستماع للنطق 🔇"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val attributionContext = remember(context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.createAttributionContext("camera_attribution")
        } else {
            context
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(attributionContext) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}
