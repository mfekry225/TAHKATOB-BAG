package com.example.ui.screens
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.ImageState
import com.example.ui.SoundEffectsHelper
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LispingExerciseStep(
    val id: Int,
    val title: String,
    val category: String,
    val desc: String,
    val timer: Int?, // in seconds, null if none
    val emoji: String,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LispingTreatmentScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 29 total slides derived precisely from the provided course training syllabus
    val exercises = remember {
        listOf(
            // Section 1: Introduction
            LispingExerciseStep(
                id = 1,
                title = "مقدمة البرنامج التدريبي النطقي الحركي",
                category = "المقدمة",
                desc = "برنامج تدريبي بصري متكامل لتقوية عضلات الفم واللسان والوجنتين لتأسيس مخارج الحروف الصحيحة وحل مشاكل النطق والخنَف للأطفال.",
                timer = null,
                emoji = "🗣️",
                steps = listOf(
                    "يهدف هذا البرنامج إلى زيادة طاقة المدى الحركي لأعضاء النطق المختلفة.",
                    "يناسب الأطفال الذين يعانون من اضطرابات مخارج الحروف أو الخنف أو الحذف والتراخي الحركي.",
                    "يفضل ممارسة التدريبات بشكل ممتع وتدريجي دون إحداث إجهاد عضلي للطفل."
                )
            ),
            LispingExerciseStep(
                id = 2,
                title = "ما هو ضعف النطق العضلي الحركي؟",
                category = "المقدمة",
                desc = "صعوبة في تشكيل الأصوات الحرفية ناتجة عن عدم قدرة عضلات الشفاه أو اللسان على اتخاذ الأوضاع الدقيقة المطلوبة للفظ الحرف بوضوح.",
                timer = null,
                emoji = "👄",
                steps = listOf(
                    "ينشأ غالباً بسبب ضعف التآزر الحركي العصبي العضلي للأعضاء النطقية.",
                    "يتسبب في ظواهر سلبية مثل تشويه الحرف، حذف الحرف، أو تبديله بالكامل.",
                    "التقييم البصري والتدريبي يساعد العضلات على استعادة قوتها ومرونتها المفقودة."
                )
            ),
            LispingExerciseStep(
                id = 3,
                title = "أهداف ومخرجات البرنامج العلاجي",
                category = "المقدمة",
                desc = "تحقيق التوازن العضلي النطقي والتحكم الكامل في اتجاهات وضغوط الهواء الخارج من الفراغ الفمي والأنفي.",
                timer = null,
                emoji = "🎯",
                steps = listOf(
                    "تقوية العضلة الدائرية للشفتين لإغلاق فمي محكم ومنع سيلان اللعاب.",
                    "تحسين مدى رفع اللسان للأعلى والجانبين لنطق الحروف اللثوية والحنكية.",
                    "توجيه ممر الهواء بشكل صحيح لتقليل وعلاج حالات الخنف المفتوح واللثغات."
                )
            ),

            // Section 2: Lip Exercises
            LispingExerciseStep(
                id = 4,
                title = "القسم الثاني: تمارين الشفتين العضلية",
                category = "تمارين الشفاه",
                desc = "تأسيس حركي للشفتين كبوابة أساسية لإصدار الأصوات الشفوية (ب، م، و) وتحسين الإغلاق الفمي.",
                timer = null,
                emoji = "💋",
                steps = listOf(
                    "تنشيط التروية الدموية والعصبية للعضلة الدائرية للشفتين.",
                    "تهيئة الطفل لبدء تطبيق المقاومة العلاجية على الشفاه.",
                    "التدرب في بيئة مريحة أمام المرآة لتحقيق التغذية الراجعة البصرية للطفل."
                )
            ),
            LispingExerciseStep(
                id = 5,
                title = "تمرين نفخ الخدود وحبس الهواء",
                category = "تمارين الشفاه",
                desc = "ملء الخدين بالهواء بالكامل وحبسه في الفم مع إطباق الشفاه بقوة لمنع تسربه.",
                timer = 5,
                emoji = "🌬️",
                steps = listOf(
                    "اطلب من الطفل ملء فمه بالكامل بالهواء كالبلون.",
                    "يجب إغلاق الشفتين بإحكام شديد لمنع تسريب الهواء من الجوانب.",
                    "ابدأ بمؤقت الثبات العضلي لـ 5 ثوانٍ كاملة ثم أرخِ العضلات، وكرر ذلك 5 مرات."
                )
            ),
            LispingExerciseStep(
                id = 6,
                title = "تمرين الضغط القوي على الشفتين",
                category = "تمارين الشفاه",
                desc = "زم الشفتين بقوة ومدهما للأمام معاً كوضعية التقبيل ثم إطباقهما في خط مستقيم.",
                timer = 5,
                emoji = "🤐",
                steps = listOf(
                    "زم الشفتين للأمام كأن الطفل يصفر أو يتهيأ لإخراج قبلة طائرة.",
                    "اضغط الشفتين على بعضهما بأقصى قوة ممكنة.",
                    "حافظ على الضغط في خط مستقيم لمدة 5 ثوانٍ كاملة في كل دورة."
                )
            ),
            LispingExerciseStep(
                id = 7,
                title = "تمرين الابتسامة العريضة مع المقاومة",
                category = "تمارين الشفاه",
                desc = "الابتسام العريض لفتح زوايا الفم لأقصى مدى مع قيام الأخصائي أو الطفل بوضع ضغط خفيف للداخل.",
                timer = 8,
                emoji = "😁",
                steps = listOf(
                    "اطلب من الطفل الابتسام بأقصى عرض ممكن من الأذن للأذن.",
                    "يمكن الضغط بخفة بإصبعين على زوايا الفم لمقاومة الحركة وتسهيل النطق الجانبي.",
                    "يساعد هذا التمرين على تقوية الوجنتين وعضلات الرفع الجانبية للشفة."
                )
            ),
            LispingExerciseStep(
                id = 8,
                title = "تمرين تحريك الشفتين يميناً ويساراً",
                category = "تمارين الشفاه",
                desc = "زم الشفتين ودفعهما للأمام ثم تحريكهما بالكامل ككتلة واحدة جهة اليمين ثم جهة اليسار بالتناوب.",
                timer = 10,
                emoji = "➡️",
                steps = listOf(
                    "ضم الشفتين للأمام لتكوين مخرج حرف (الواو).",
                    "حركهما إلى اليمين بأقصى قدر ممكن، اثبت، ثم حركهما للأيسر بمرونة تامة.",
                    "يكرر التمرين 10 مرات متتالية ببطء وثبات حركي لتنشيط العصب الفمي."
                )
            ),
            LispingExerciseStep(
                id = 9,
                title = "تمرين الشفاه بالمصاصة العلاجية",
                category = "تمارين الشفاه",
                desc = "إمساك مصاصة حلوى أو عصا دائرية بلاستيكية نظيفة بين الشفتين فقط والاحتفاظ بها أفقية دون الاستعانة بالأسنان.",
                timer = 10,
                emoji = "🍭",
                steps = listOf(
                    "ضع عصا بلاستيكية مسطحة أو مصاصة بين شفتي طفلك.",
                    "تأكد تماماً من عدم ملامسة الأسنان للعصا لمنع العض المساعد.",
                    "شجع الطفل على إبقائها مستقيمة أفقياً باستخدام قوة الشفتين فقط لـ 10 ثوانٍ."
                )
            ),
            LispingExerciseStep(
                id = 10,
                title = "تمرين التناوب السريع بين الضم والابتسام",
                category = "تمارين الشفاه",
                desc = "الانتقال السريع والمنظم بين وضعي ضم الشفتين للأمام بزمّ ثم فردهما تماماً بابتسامة تامة وواسعة.",
                timer = 5,
                emoji = "🙂",
                steps = listOf(
                    "وجه الطفل: 'ضم شفتيك كالبوق، ثم ابتسم فجأة وبأقصى سرعة!'.",
                    "الهدف هو تحسين التآزر وسرعة تلبية الأوامر العصبية الحركية لعضلات الفم.",
                    "كرر الدورة (ضم ثم ابتسام) 10 مرات متتالية بانتظام وتوافق حركي."
                )
            ),

            // Section 3: Tongue Exercises
            LispingExerciseStep(
                id = 11,
                title = "القسم الثالث: تمارين اللسان المتطورة",
                category = "تمارين اللسان",
                desc = "مجموعة تدريبات لزيادة قوة حركة اللسان وتحسين مجاله الحركي بالاتجاهات الأربعة الأساسية لضمان سلامة نطق الحروف كـ (ل، ر، د، ت).",
                timer = null,
                emoji = "👅",
                steps = listOf(
                    "اللسان هو أهم عضو ناطق مرن ويحتاج مرونة وقوة عضلية عالية للتعبير واللفظ.",
                    "تمارين اللسان تتطلب تطبيقاً ثابتاً وتكراراً دورياً هادئاً لتحفيز اللجام السفلي.",
                    "راقب الطفل وتأكد من ثبات الرأس وعدم ميله أثناء حركة اللسان."
                )
            ),
            LispingExerciseStep(
                id = 12,
                title = "تمرين رفع اللسان للأعلى (خارج الفم)",
                category = "تمارين اللسان",
                desc = "مد اللسان لخارج الفم بالكامل ومحاولة رفع طرفه لأعلى باتجاه الأنف بأقصى حد.",
                timer = 8,
                emoji = "⬆️",
                steps = listOf(
                    "افتح الفم بمدى مريح ومناسب للطفل.",
                    "أخرج لسانك وحاول توجيه طرفه للأعلى كأنه يريد لمس أرنبة الأنف.",
                    "اثبت على هذه الوضعية لـ 8 ثوانٍ كاملة لتمطيط العضلات السفلية."
                )
            ),
            LispingExerciseStep(
                id = 13,
                title = "تمرين خفض اللسان للأسفل (خارج الفم)",
                category = "تمارين اللسان",
                desc = "إخراج اللسان ومحاولة توجيه طرفه للأسفل باتجاه الذقن بأقصى استطالة ممكنة.",
                timer = 8,
                emoji = "⬇️",
                steps = listOf(
                    "مد اللسان خارج حدود الفم بمسافة كافية للطفل.",
                    "اضغط بطرف اللسان للأسفل باتجاه عظمة الذقن أو الشفة السفلية.",
                    "حافظ على استقامة اللسان وثباته دون ثني للشفتين لمدة 8 ثوانٍ."
                )
            ),
            LispingExerciseStep(
                id = 14,
                title = "تمرين لمس زوايا الفم الجانبية",
                category = "تمارين اللسان",
                desc = "تحريك اللسان من الداخل للمس زاوية الشفاه اليمنى ثم اليسرى بتبادل منظم وثابت.",
                timer = 10,
                emoji = "↔️",
                steps = listOf(
                    "افتح الفم قليلاً بالتوازي.",
                    "حرك الجزء الأمامي من اللسان للمس أقصى الزاوية اليمنى ثم اليسرى للفم.",
                    "حاول الحفاظ على عدم ملامسة الأسنان أو الشفة السفلى أثناء الانتقال الجانبي."
                )
            ),
            LispingExerciseStep(
                id = 15,
                title = "تمرين دفع الخد باللسان من الداخل",
                category = "تمارين اللسان",
                desc = "دفع جدار الخد الأيمن من الداخل بواسطة طرف اللسان بقوة، مع توفير مقاومة خارجية لطيفة بإصبع الأخصائي.",
                timer = 5,
                emoji = "👉",
                steps = listOf(
                    "أبقِ الفم مغلقاً وهادئاً تماماً أثناء التدريب.",
                    "ادفع الخد الأيمن من الداخل باللسان لتشكيل جيب بارز بالخد.",
                    "اضغط من الخارج بإصبعك برفق لمقاومة اللسان، واثبت لـ 5 ثوانٍ، ثم اعكس للخد الأيسر."
                )
            ),
            LispingExerciseStep(
                id = 16,
                title = "تمرين دوران اللسان حول الأسنان",
                category = "تمارين اللسان",
                desc = "لف اللسان بشكل دائري مستمر في التجويف الفمي بين الشفتين والأسنان من الخارج كأنه ينظف الأسنان.",
                timer = 10,
                emoji = "🔄",
                steps = listOf(
                    "أغلق الشفتين برفق فوق الأسنان.",
                    "مرر اللسان دائرياً بالكامل حول الصف العلوي ثم السفلي للأسنان.",
                    "قم بـ 5 دورات باتجاه عقارب الساعة و5 دورات بالاتجاه المعاكس لتنشيط المفاصل اللسانية."
                )
            ),
            LispingExerciseStep(
                id = 17,
                title = "تمرين رفع اللسان للحنك الأعلى الخلفي",
                category = "تمارين اللسان",
                desc = "وضع طرف اللسان على اللثة العلوية خلف الأسنان الأمامية مباشرة والضغط للأعلى بقوة ثبات كافية.",
                timer = 8,
                emoji = "⭐",
                steps = listOf(
                    "افتح الفم بشكل واسع ومكشوف أمام المرآة.",
                    "المس سقف الحلق العظمي الصلب الواقع خلف الأسنان العلوية مباشرة بطرف لسانك.",
                    "اضغط للأعلى بقوة لدعم الحروف السنية اللثوية كـ (ت، د، ن، ل)."
                )
            ),
            LispingExerciseStep(
                id = 18,
                title = "تمرين فرقعة اللسان (صوت النقر)",
                category = "تمارين اللسان",
                desc = "شفط اللسان بالكامل وتثبيته في الحنك الأعلى ثم سحبه للأسفل بقوة لإنتاج صوت فرقعة حاد ومسموع.",
                timer = 5,
                emoji = "🎵",
                steps = listOf(
                    "الاطباق الكامل بظهر اللسان على الحنك العلوي الرخو والصلب.",
                    "شد اللسان للأسفل فجأة وبقوة لإصدار صوت نقرة الحصان المسموعة (طق طق).",
                    "كرر هذا التمرين بانتظام، فهو يقوي عضلات البلع وقاعدة اللسان لتسهيل حرف (الراء)."
                )
            ),
            LispingExerciseStep(
                id = 19,
                title = "تمرين دفع اللسان ضد خافض اللسان",
                category = "تمارين اللسان",
                desc = "مد اللسان بشكل مستقيم للأمام ودفعه ضد أداة خشبية أو ملعقة يمسكها المعالج لمقاومة الدفع الأمامي.",
                timer = 5,
                emoji = "🪵",
                steps = listOf(
                    "أخرج لسانك بشكل مستقيم وموازٍ أمام الفم.",
                    "يقوم المعالج بوضع خافض اللسان الخشبي أمام اللسان مباشرة بضغط آمن ومتوسط.",
                    "ادفع المعالج بلسانك للأمام وقاوم تراجع اللسان لـ 5 ثوانٍ كاملة لتمكين العضلة الطولية."
                )
            ),
            LispingExerciseStep(
                id = 20,
                title = "تمرين إخراج اللسان مستوياً وعريضاً",
                category = "تمارين اللسان",
                desc = "إخراج اللسان ومحاولة بسطه ليكون عريضاً ومسطحاً يغطي الشفة السفلية بالكامل دون تشنج أو انكماش.",
                timer = 8,
                emoji = "🟰",
                steps = listOf(
                    "افتح فمك بشكل طبيعي ومسترخٍ.",
                    "أخرج لسانك واجعله عريضاً وممدداً كالسرير فوق الشفة السفلية دون تكتيل.",
                    "يساعد هذا على السيطرة على الانقباض والتوتر للسان أثناء نطق وتلوين الكلمات."
                )
            ),
            LispingExerciseStep(
                id = 21,
                title = "تمرين طي أو لف اللسان (الأنبوب اللساني)",
                category = "تمارين اللسان",
                desc = "لف الجانبين الأيمن والأيسر للسان للأعلى ليتخذ اللسان شكل أنبوب أو لفة مفرغة بالمنتصف.",
                timer = 5,
                emoji = "🌮",
                steps = listOf(
                    "شجع الطفل على ثني حواف لسانه الجانبية للأعلى لتكوين شكل أنبوب أو تاكو.",
                    "إذا تعذر ذلك، يمكن مساعدته بضغط خفيف بالخافض في منتصف اللسان ليرتفع الجانبان تلقائياً.",
                    "يساعد على عزل حركات اللسان الجانبية وتحسين نطق الحروف كـ (الصاد وضاد)."
                )
            ),

            // Section 4: Speech & Integration Exercises
            LispingExerciseStep(
                id = 22,
                title = "القسم الرابع: تمارين النطق والتكامل الصوتي",
                category = "التكامل النطقي",
                desc = "الآن حان الوقت لدمج القدرات العضلية المكتسبة للشفتين واللسان لإنتاج أصوات ومقاطع بطلاقة وتناسق كامل.",
                timer = null,
                emoji = "📣",
                steps = listOf(
                    "الهدف هو تحويل القوة العضلية الساكنة إلى طاقة نطقية حركية ديناميكية وسريعة.",
                    "سيتم نطق الحروف منفردة ثم في مقاطع صوتية متكررة تزداد صعوبتها تدريجياً.",
                    "انتبه جيداً لموضع النطق وشكل الفم وتدفق الهواء أثناء خروج الأصوات."
                )
            ),
            LispingExerciseStep(
                id = 23,
                title = "نطق المقاطع الشفوية (با - با - با)",
                category = "التكامل النطقي",
                desc = "نطق صوت حرف الباء ممدوداً بالفتح مع التركيز التام على غلق الشفتين وفتحهما فجأة لإنتاج صوت انفجاري قوي.",
                timer = 5,
                emoji = "👶",
                steps = listOf(
                    "اطباق كامل للشفتين، ثم تمرير هواء الزفير مع صوت الباء المضموم: 'بَــا'.",
                    "كرر: 'با - با - با' متتالية ببطء ثم بسرعة لتمكين العضلة الشفوية.",
                    "هذا التدريب يعالج ضعف عضلات الشفاه المسببة للثغات وصعوبات نطق حروف (الباء والميم)."
                )
            ),
            LispingExerciseStep(
                id = 24,
                title = "نطق المقاطع الحنكية (تا - تا - تا)",
                category = "التكامل النطقي",
                desc = "نطق مقطع 'تا' بتثبيت طرف اللسان خلف منابت الأسنان العلوية وسحبه فجأة للأسفل بوضوح كامل.",
                timer = 5,
                emoji = "🦷",
                steps = listOf(
                    "المس الحنك الأعلى خلف الأسنان الأمامية بطرف لسانك واحبس الهواء تماماً خلف الفك.",
                    "أطلق الهواء فجأة بلفظ واضح مبهر لصوت: 'تَــا'.",
                    "كرر بانتظام 10 مرات متتالية لملاحظة دقة اللفظ الحنكي وتدريب الحافة الأمامية للسان."
                )
            ),
            LispingExerciseStep(
                id = 25,
                title = "نطق المقاطع الخلفية (كا - كا - كا)",
                category = "التكامل النطقي",
                desc = "نطق مقطع 'كا' برفع قاعدة اللسان الخلفية لتلمس سقف الحلق الرخو بهدف تفعيل صمام الإغلاق ومخارج الحروف الخلفية.",
                timer = 5,
                emoji = "🦖",
                steps = listOf(
                    "تراجع باللسان للخلف وارفع قاعدته لتلامس سقف الحلق اللين.",
                    "انطق مقطع: 'كَـــا' بضغط حنجري فمي متسق ومحسوس.",
                    "مفيد جداً لتقوية الجزء الخلفي وعلاج التراخي المسبب لتشوهات حروف مثل (ق، ك، غ، خ)."
                )
            ),
            LispingExerciseStep(
                id = 26,
                title = "تدريب التنفس البطني العميق أثناء الكلام",
                category = "التكامل النطقي",
                desc = "تعليم الطفل أخذ شهيق عميق من الأنف ينفخ البطن ثم نطق الحروف والمقاطع دفعة واحدة وببطء أثناء زفير طويل ومستقر.",
                timer = 10,
                emoji = "🫁",
                steps = listOf(
                    "ضع يد الطفل على بطنه ليشعر بارتفاعها مع أخذ شهيق من الأنف (بدون رفع الأكتاف).",
                    "اطلب منه نطق حرف ممدود مثل 'آآآآآآ' أو 'سسسسسس' لأطول فترة ممكنة أثناء الزفير.",
                    "يؤسس للتنسيق الصوتي التنفسي وتفادي تقطع الكلام ونفاد الهواء ومقاومة التأتأة."
                )
            ),
            LispingExerciseStep(
                id = 27,
                title = "تمارين تقليل الخنف وضبط الصمام اللهاوي",
                category = "التكامل النطقي",
                desc = "تمارين لتدريب اللهاة وسقف الحلق الرخو على إغلاق الممر الأنفي أثناء نطق الأصوات الفمية غير الأنفية لمنع حدوث الغنة الزائدة.",
                timer = 8,
                emoji = " Nose ",
                steps = listOf(
                    "اجعل الطفل ينطق: 'آآآآآ' ثم اطلب منه إغلاق فتحتي أنفه بأصابعه فجأة (يجب ألا يتغير رنين الصوت).",
                    "درب الطفل على نفخ بالونات أو صفارات لتقوية عضلات سقف الحلق الرخو وسد التجويف الأنفي كلياً.",
                    "تكرار يومي حاسم لحالات الخنف المفتوح واضطرابات الشفة الأرنبية وشق الحنك."
                )
            ),
            LispingExerciseStep(
                id = 28,
                title = "تدريب التناوب الحركي السريع (با - تا - كا)",
                category = "التكامل النطقي",
                desc = "دمج الأصوات الثلاثة معاً في تتابع سريع ومنظم ومستمر لتدريب الدماغ وأعضاء النطق على التبديل السلس والسريع والمتوازن.",
                timer = 10,
                emoji = "🗣️",
                steps = listOf(
                    "اطلب من الطفل تكرار: 'با - تا - كا' في تتابع سريع ومستمر.",
                    "ابدأ ببطء لضمان اللفظ الصحيح لكل مقطع على حدة.",
                    "زد السرعة بالتدريج للوصول لأسرع معدل حركي متناسق ومفهوم."
                )
            )
        )
    }

    var currentIdx by remember { mutableStateOf(0) }
    val currentExercise = exercises[currentIdx]
    var timeLeft by remember { mutableStateOf(currentExercise.timer ?: 0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var selectedVisualTab by remember { mutableStateOf("illustration") }
    var isCameraActive by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(isCameraActive) {
        if (isCameraActive) {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Effect to update timeLeft when exercise changes
    LaunchedEffect(currentIdx) {
        timeLeft = exercises[currentIdx].timer ?: 0
        isTimerRunning = false
    }

    // Timer countdown effect
    LaunchedEffect(isTimerRunning, timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
            if (timeLeft == 0) {
                isTimerRunning = false
                SoundEffectsHelper.playSuccess() // Optional success chime when timer finishes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "علاج اللدغات ومخارج الحروف 🗣️",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "برنامج تدريب مخارج الحروف وعلاج اللدغات",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "فهرس التمارين")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Interactive Training Camera Mirror Sidebar Card (Active only if requested) - First Child (Left Side)
                    AnimatedVisibility(
                        visible = isCameraActive,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(4.dp, RoundedCornerShape(24.dp)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مرآة العيادة النطقية 🤳",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
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
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                    Text(
                                        text = "اجعل طفلك ينظر لصورته في الكاميرا أثناء التمرين لمطابقة حركة الفم واللسان وملاحظة الفرق.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                                    )

                                    // Live CameraX preview box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
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
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "📷",
                                                    fontSize = 32.sp
                                                )
                                                Text(
                                                    text = "الكاميرا غير نشطة",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                                Text(
                                                    text = "الرجاء توفير إذن الكاميرا للتشغيل",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                // Clinic Instruction Tips box
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("💡", fontSize = 12.sp)
                                        Column {
                                            Text(
                                                text = "إرشاد بصري علاجي:",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                            Text(
                                                text = "يمكنك استخدام خافض لسان خشبي نظيف أو مصاصة لمساعدة الطفل على توجيه اللسان وإدراك مخارج الحروف بشكل سليم.",
                                                fontSize = 9.sp,
                                                color = Color(0xFFB45309),
                                                lineHeight = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Main Slide Card Screen Area - Second Child (Right Side)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Slide Header with slide category and badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (currentExercise.category) {
                                                "المقدمة" -> Color(0xFFEFF6FF)
                                                "تمارين الشفاه" -> Color(0xFFFDF2F8)
                                                "تمارين اللسان" -> Color(0xFFECFDF5)
                                                else -> Color(0xFFF5F3FF)
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currentExercise.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentExercise.category) {
                                            "المقدمة" -> Color(0xFF1D4ED8)
                                            "تمارين الشفاه" -> Color(0xFFBE185D)
                                            "تمارين اللسان" -> Color(0xFF047857)
                                            else -> Color(0xFF6D28D9)
                                        }
                                    )
                                }

                                Text(
                                    text = "تدريب ${currentIdx + 1} من ${exercises.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Highly responsive split layout based on screen orientation
                            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                            val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

                            if (isPortrait || isCameraActive) {
                                // Vertical Stack Layout (Optimal for narrow screens or when camera side-bar is active)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    // 1. Responsive Image Box (Takes up a clean aspect ratio, filling the top area)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFFFF9F5))
                                            .border(2.dp, Color(0xFFCFD8DC), RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MiloFace(
                                            mouthStyle = getLispMouthStyle(currentExercise.id),
                                            isPuffed = getLispMouthStyle(currentExercise.id) == "puffCheeks",
                                            isHappy = getLispMouthStyle(currentExercise.id) == "cheerVictory",
                                            modifier = Modifier.fillMaxSize().padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 2. Details Column (Taking up remaining height, with navigation fixed at the bottom)
                                    Column(
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Scrollable content area
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = currentExercise.title,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1E293B),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )

                                            Text(
                                                text = currentExercise.desc,
                                                fontSize = 13.sp,
                                                color = Color(0xFF475569),
                                                lineHeight = 20.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )

                                            // Instructional checklist Card
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text(
                                                        text = "خطوات وتأهيل التدريب العملي:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2563EB),
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )
                                                    currentExercise.steps.forEachIndexed { stepIndex, step ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.Top
                                                        ) {
                                                            Text(
                                                                text = "⭐",
                                                                fontSize = 11.sp,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                            Text(
                                                                text = step,
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF334155),
                                                                lineHeight = 18.sp,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Optional active countdown timer card on supported screens
                                            if (currentExercise.timer != null) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            // Dynamic circular ring drawing Canvas
                                                            Box(
                                                                modifier = Modifier.size(46.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                                    // Track
                                                                    drawCircle(
                                                                        color = Color(0xFFD1FAE5),
                                                                        style = Stroke(width = 3.dp.toPx())
                                                                    )
                                                                    // Progress arc
                                                                    val sweep = if (currentExercise.timer > 0) {
                                                                        (timeLeft.toFloat() / currentExercise.timer.toFloat()) * 360f
                                                                    } else 0f
                                                                    drawArc(
                                                                        color = Color(0xFF10B981),
                                                                        startAngle = -90f,
                                                                        sweepAngle = sweep,
                                                                        useCenter = false,
                                                                        style = Stroke(
                                                                            width = 3.dp.toPx(),
                                                                            cap = StrokeCap.Round
                                                                        )
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "$timeLeft",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF047857)
                                                                )
                                                            }

                                                            Column {
                                                                Text(
                                                                    text = "مؤقت الاستقرار العضلي ⏱️",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF065F46)
                                                                )
                                                                Text(
                                                                    text = "حث الطفل على الحفاظ على هذا الوضع النطقي",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF047857)
                                                                )
                                                            }
                                                        }

                                                        Button(
                                                            onClick = {
                                                                SoundEffectsHelper.playClick()
                                                                if (isTimerRunning) {
                                                                    isTimerRunning = false
                                                                } else {
                                                                    if (timeLeft == 0) {
                                                                        timeLeft = currentExercise.timer
                                                                    }
                                                                    isTimerRunning = true
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            shape = RoundedCornerShape(10.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                            modifier = Modifier.height(34.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isTimerRunning) "إيقاف مؤقت" else "ابدأ الآن ▶️",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Navigation Buttons (Fixed at the bottom of the words area)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (currentIdx > 0) {
                                                        SoundEffectsHelper.playPrevious()
                                                        currentIdx -= 1
                                                    } else {
                                                        currentIdx = exercises.size - 1
                                                    }
                                                    timeLeft = exercises[currentIdx].timer ?: 0
                                                    isTimerRunning = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "السابق ➡️",
                                                    color = Color(0xFF475569),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "شريحة ${currentIdx + 1} / ${exercises.size}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )

                                            Button(
                                                onClick = {
                                                    if (currentIdx < exercises.size - 1) {
                                                        SoundEffectsHelper.playNext()
                                                        currentIdx += 1
                                                    } else {
                                                        currentIdx = 0
                                                    }
                                                    timeLeft = exercises[currentIdx].timer ?: 0
                                                    isTimerRunning = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "التالي ⬅️",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Side-by-Side Row Layout (Optimal for widescreen/landscape viewports when camera is closed)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Left Column: Visual Image (Filling the entire area)
                                    Box(
                                        modifier = Modifier
                                            .weight(1.1f) // Give a nice relative width to the image column
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFFFF9F5))
                                            .border(2.dp, Color(0xFFCFD8DC), RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MiloFace(
                                            mouthStyle = getLispMouthStyle(currentExercise.id),
                                            isPuffed = getLispMouthStyle(currentExercise.id) == "puffCheeks",
                                            isHappy = getLispMouthStyle(currentExercise.id) == "cheerVictory",
                                            modifier = Modifier.fillMaxSize().padding(14.dp)
                                        )
                                    }

                                    // Right Column: Words, Title, Checklist, and Timer, and Navigation Slide Buttons at the bottom
                                    Column(
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Scrollable Content
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = currentExercise.title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1E293B)
                                            )

                                            Text(
                                                text = currentExercise.desc,
                                                fontSize = 13.sp,
                                                color = Color(0xFF475569),
                                                lineHeight = 20.sp
                                            )

                                            // Instructional checklist Card
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text(
                                                        text = "خطوات وتأهيل التدريب العملي:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2563EB),
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )
                                                    currentExercise.steps.forEachIndexed { stepIndex, step ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.Top
                                                        ) {
                                                            Text(
                                                                text = "⭐",
                                                                fontSize = 11.sp,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                            Text(
                                                                text = step,
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF334155),
                                                                lineHeight = 18.sp,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Optional active countdown timer card on supported screens
                                            if (currentExercise.timer != null) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            // Dynamic circular ring drawing Canvas
                                                            Box(
                                                                modifier = Modifier.size(46.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                                    // Track
                                                                    drawCircle(
                                                                        color = Color(0xFFD1FAE5),
                                                                        style = Stroke(width = 3.dp.toPx())
                                                                    )
                                                                    // Progress arc
                                                                    val sweep = if (currentExercise.timer > 0) {
                                                                        (timeLeft.toFloat() / currentExercise.timer.toFloat()) * 360f
                                                                    } else 0f
                                                                    drawArc(
                                                                        color = Color(0xFF10B981),
                                                                        startAngle = -90f,
                                                                        sweepAngle = sweep,
                                                                        useCenter = false,
                                                                        style = Stroke(
                                                                            width = 3.dp.toPx(),
                                                                            cap = StrokeCap.Round
                                                                        )
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "$timeLeft",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF047857)
                                                                )
                                                            }

                                                            Column {
                                                                Text(
                                                                    text = "مؤقت الاستقرار العضلي ⏱️",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF065F46)
                                                                )
                                                                Text(
                                                                    text = "حث الطفل على الحفاظ على هذا الوضع النطقي",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF047857)
                                                                )
                                                            }
                                                        }

                                                        Button(
                                                            onClick = {
                                                                SoundEffectsHelper.playClick()
                                                                if (isTimerRunning) {
                                                                    isTimerRunning = false
                                                                } else {
                                                                    if (timeLeft == 0) {
                                                                        timeLeft = currentExercise.timer
                                                                    }
                                                                    isTimerRunning = true
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            shape = RoundedCornerShape(10.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                            modifier = Modifier.height(34.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isTimerRunning) "إيقاف مؤقت" else "ابدأ الآن ▶️",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Navigation Buttons (Fixed at bottom of Right Column)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (currentIdx > 0) {
                                                        SoundEffectsHelper.playPrevious()
                                                        currentIdx -= 1
                                                    } else {
                                                        currentIdx = exercises.size - 1
                                                    }
                                                    timeLeft = exercises[currentIdx].timer ?: 0
                                                    isTimerRunning = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "السابق ➡️",
                                                    color = Color(0xFF475569),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "شريحة ${currentIdx + 1} / ${exercises.size}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )

                                            Button(
                                                onClick = {
                                                    if (currentIdx < exercises.size - 1) {
                                                        SoundEffectsHelper.playNext()
                                                        currentIdx += 1
                                                    } else {
                                                        currentIdx = 0
                                                    }
                                                    timeLeft = exercises[currentIdx].timer ?: 0
                                                    isTimerRunning = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "التالي ⬅️",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
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
    }
    // Comprehensive selection list Dialog organiser
    if (showJumpDialog) {
        Dialog(onDismissRequest = { showJumpDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اختر وجهة التدريب والتمارين:",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = { showJumpDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section Grouping headers helper
                        val sections = listOf("المقدمة", "تمارين الشفاه", "تمارين اللسان", "التكامل النطقي", "الخاتمة")
                        sections.forEach { sectionHeader ->
                            val sectionSteps = exercises.filter { it.category == sectionHeader }
                            if (sectionSteps.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "⚡ $sectionHeader",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when (sectionHeader) {
                                            "المقدمة" -> Color(0xFF1D4ED8)
                                            "تمارين الشفاه" -> Color(0xFFBE185D)
                                            "تمارين اللسان" -> Color(0xFF047857)
                                            else -> Color(0xFF6D28D9)
                                        },
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                                    )
                                }

                                itemsIndexed(sectionSteps) { _, step ->
                                    val indexInAll = exercises.indexOf(step)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (indexInAll == currentIdx) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                                            )
                                            .clickable {
                                                SoundEffectsHelper.playClick()
                                                currentIdx = indexInAll
                                                showJumpDialog = false
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = step.emoji,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = "شريحة ${indexInAll + 1}: ${step.title}",
                                            fontSize = 12.sp,
                                            fontWeight = if (indexInAll == currentIdx) FontWeight.Bold else FontWeight.Normal,
                                            color = if (indexInAll == currentIdx) Color(0xFF2563EB) else Color(0xFF334155),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (step.timer != null) {
                                            Text(
                                                text = "⏱️ ${step.timer}ث",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
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
}

fun getLispMouthStyle(id: Int): String {
    return when (id) {
        1 -> "smileClosed"
        2 -> "smileClosed"
        3 -> "smileTeeth"
        4 -> "smileClosed"
        5 -> "puffCheeks"
        6 -> "closeLips"
        7 -> "smileTeeth"
        8 -> "puckerLips"
        9 -> "closeLips"
        10 -> "puckerLips"
        11 -> "tongueOut"
        12 -> "tongueUp"
        13 -> "tongueDown"
        14 -> "tongueOut"
        15 -> "puffCheeks"
        16 -> "openWide"
        17 -> "tongueUp"
        18 -> "openWide"
        19 -> "tongueOut"
        20 -> "tongueOut"
        21 -> "tongueOut"
        22 -> "openWide"
        23 -> "openWide"
        24 -> "smileTeeth"
        25 -> "openWide"
        26 -> "blowCandle"
        27 -> "blowCandle"
        28 -> "openWide"
        29 -> "smileClosed"
        else -> "openWide"
    }
}

@Composable
fun RenderLispAiStateSquare(
    currentImageState: ImageState,
    currentStep: LispingExerciseStep,
    viewModel: SpeechViewModel
) {
    val cleanPrompt = currentStep.title
        .replace(Regex("[^\u0621-\u064A\\s\\d]"), "")
        .trim()
    val mouthStyle = getLispMouthStyle(currentStep.id)

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
                        onClick = { viewModel.generateImageForLispStep(currentStep.id, cleanPrompt, mouthStyle) },
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
                            .clickable { viewModel.generateImageForLispStep(currentStep.id, cleanPrompt, mouthStyle) }
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
                        onClick = { viewModel.generateImageForLispStep(currentStep.id, cleanPrompt, mouthStyle) },
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
