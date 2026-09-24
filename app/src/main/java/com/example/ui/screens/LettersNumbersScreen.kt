package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpeechViewModel
import com.example.ui.theme.*

data class LetterModel(val letter: String, val word: String, val emoji: String, val articulation: String)
data class NumberModel(val value: Int, val arabicWord: String, val englishWord: String, val emojiRepresentation: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LettersNumbersScreen(
    viewModel: SpeechViewModel,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Arabic Letters, 1: English Letters, 2: Numbers & Quantity
    val isMuted by viewModel.isMuted.collectAsState()

    val arabicLetters = remember {
        listOf(
            LetterModel("أ", "أرنب", "🐰", "مخرجها من أقصى الحلق (أدنى موضع بالحلق)."),
            LetterModel("ب", "بقرة", "🐄", "مخرجها من بين الشفتين بإطباقهما."),
            LetterModel("ت", "تفاحة", "🍎", "من طرف اللسان مع أصول الثنايا العليا."),
            LetterModel("ث", "ثعلب", "🦊", "من طرف اللسان مع أطراف الثنايا العليا (لثوي)."),
            LetterModel("ج", "جمل", "🐫", "من وسط اللسان مع ما يحاذيه من الحنك الأعلى."),
            LetterModel("ح", "حليب", "🥛", "من وسط الحلق (صوت دافئ حلقي المستوي)."),
            LetterModel("خ", "خروف", "🐑", "من أدنى الحلق (أقرب موضع للفم)."),
            LetterModel("د", "دبدوب", "🧸", "من طرف اللسان مع أصول الثنايا العليا."),
            LetterModel("ذ", "ذرة", "🌽", "مخرج لثوي من طرف اللسان وأطراف السنايا العليا."),
            LetterModel("ر", "رمان", "🧋", "من طرف اللسان مع ما يحاذيه من لثة الأسنان العليا."),
            LetterModel("ز", "زرافة", "🦒", "من طرف اللسان وفوق الثنايا السفلى (صوت صفير)."),
            LetterModel("س", "سمكة", "🐟", "من طرف اللسان وفوي الثنايا السفلى (صوت صفير مرقق)."),
            LetterModel("ش", "شمس", "☀️", "من وسط اللسان مع تفشي الهواء داخل الفم."),
            LetterModel("ص", "صقر", "🦅", "من طرف اللسان وفوق الثنايا المفخمة المطبقة."),
            LetterModel("ض", "ضفدع", "🐸", "من إحدى حافتي اللسان أو كلتيهما مع الأضراس العليا."),
            LetterModel("ط", "طائرة", "✈️", "من طرف اللسان مفخم مطبق مع أصول الثنايا العليا."),
            LetterModel("ظ", "ظرف", "✉️", "من طرف اللسان ملامس أطراف الثنايا العليا مفخم."),
            LetterModel("ع", "عين", "👁️", "مخرج حلقي نظيف من وسط الحلق."),
            LetterModel("غ", "غزال", "🦌", "مخرج حلقي رخو من أدنى الحلق."),
            LetterModel("ف", "فراولة", "🍓", "بين بطن الشفة السفلى وأطراف الثنايا العليا."),
            LetterModel("ق", "قلم", "✏️", "من أقصى اللسان فوق مخرج الكاف قليلاً مفخم."),
            LetterModel("ك", "كرسي", "🪑", "من أقصى اللسان تحت مخرج القاف بقليل مرقق."),
            LetterModel("ل", "ليمون", "🍋", "من أدنى حافة اللسان إلى منتهى طرفه مع لثة الأسنان."),
            LetterModel("م", "موز", "🍌", "من بين الشفتين بإطباقهما مع غنة تخرج من الخيشوم."),
            LetterModel("ن", "نخلة", "🌴", "من طرف اللسان بقرب اللثة مع غنة من الخيشوم."),
            LetterModel("هـ", "هلال", "🌙", "مخرج حلقي عميق من أقصى الحلق مجهور."),
            LetterModel("و", "وردة", "🌹", "بتباعد الشفتين واستدارتهما دون إتصال كامل."),
            LetterModel("ي", "يد", "✋", "من وسط اللسان بتباعد طفيف مجهور رخو.")
        )
    }

    val englishLetters = remember {
        listOf(
            LetterModel("A", "Apple", "🍎", "Pronounced as 'Ay' helper for children vocabulary."),
            LetterModel("B", "Bear", "🐻", "Bilabial sound starting with lips compression."),
            LetterModel("C", "Cat", "🐱", "Usually sounds like k, back tongue touch."),
            LetterModel("D", "Dog", "🐶", "Alveolar sound, tongue tip on upper teeth ridge."),
            LetterModel("E", "Elephant", "🐘", "Vowel sound, open mouth half-wide."),
            LetterModel("F", "Fish", "🐟", "Labiodental, top teeth on bottom lip gently."),
            LetterModel("G", "Giraffe", "🦒", "Soft or hard sound, tongue middle elevation."),
            LetterModel("H", "Horse", "🐴", "Breathy sound directly from the throat."),
            LetterModel("I", "Ice Cream", "🍦", "Vowel sound helper with smile gesture."),
            LetterModel("J", "Juice", "🧃", "Palato-alveolar sound, friction of air."),
            LetterModel("K", "Kite", "🪁", "Voiceless back oral stop sound."),
            LetterModel("L", "Lion", "🦁", "Alveolar lateral, tongue tip touches gums."),
            LetterModel("M", "Milk", "🥛", "Bilabial nasal, lips completely sealed."),
            LetterModel("N", "Nose", "👃", "Nasal alveolar, air flows through nose."),
            LetterModel("O", "Orange", "🍊", "Vowel sound with rounded open lips."),
            LetterModel("P", "Parrot", "🦜", "Voiceless bilabial stop, burst of air!"),
            LetterModel("Q", "Queen", "👑", "Usually coupled with U sound, back oral state."),
            LetterModel("R", "Rabbit", "🐰", "Alveolar sound, retroflex or bunched tongue."),
            LetterModel("S", "Sun", "☀️", "Alveolar sibilant, friction hiss air."),
            LetterModel("T", "Train", "🚂", "Alveolar stop, tip of tongue release."),
            LetterModel("U", "Umbrella", "⛱️", "Vowel sound from mid tongue base."),
            LetterModel("V", "Van", "🚐", "Voiced labiodental, vibration on bottom lip."),
            LetterModel("W", "Water", "💧", "Lips rounded then quickly relaxed."),
            LetterModel("X", "Xylophone", "🪘", "Usually starts with Z or Ks sound pattern."),
            LetterModel("Y", "Yellow", "💛", "Gliding sound starting from a high state."),
            LetterModel("Z", "Zebra", "🦓", "Voiced alveolar sibilant, buzzed hum.")
        )
    }

    val quantities = remember {
        listOf(
            NumberModel(1, "واحد", "One", "🍎"),
            NumberModel(2, "اثنان", "Two", "🍌🍌"),
            NumberModel(3, "ثلاثة", "Three", "🎈🎈🎈"),
            NumberModel(4, "أربعة", "Four", "🚗🚗🚗🚗"),
            NumberModel(5, "خمسة", "Five", "⭐     ⭐     ⭐     ⭐     ⭐"),
            NumberModel(6, "ستة", "Six", "🎨🎨🎨🎨🎨🎨"),
            NumberModel(7, "سبعة", "Seven", "🦁🦁🦁🦁🦁🦁🦁"),
            NumberModel(8, "ثمانية", "Eight", "🪁🪁🪁🪁🪁🪁🪁🪁"),
            NumberModel(9, "تسعة", "Nine", "🐱🐱🐱🐱🐱🐱🐱🐱🐱"),
            NumberModel(10, "عشرة", "Ten", "💎💎💎💎💎💎💎💎💎💎")
        )
    }

    var activeLetterDetail by remember { mutableStateOf<LetterModel?>(null) }
    var activeNumberDetail by remember { mutableStateOf<NumberModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مرح الحروف والأرقام 🗣️", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF001D34)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF1A1C1E))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.5.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.5f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sliding tab select buttons
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(bottom = 12.dp)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; activeLetterDetail = null; activeNumberDetail = null }) {
                    Text("أ ب ت (عربي)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; activeLetterDetail = null; activeNumberDetail = null }) {
                    Text("A B C (إنجليزي)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2; activeLetterDetail = null; activeNumberDetail = null }) {
                    Text("الأرقام والكميات", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Quick display of letter or number details at the top if tapped
            AnimatedVisibility(
                visible = activeLetterDetail != null || activeNumberDetail != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(PlayfulOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        activeLetterDetail?.letter ?: activeNumberDetail?.value?.toString() ?: "",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    val titleWord = activeLetterDetail?.word ?: "${activeNumberDetail?.arabicWord} / ${activeNumberDetail?.englishWord}"
                                    val emoji = activeLetterDetail?.emoji ?: (activeNumberDetail?.emojiRepresentation?.take(2) ?: "")
                                    Text(
                                        "$titleWord $emoji",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        if (selectedTab < 2) "انطق الحرف والكلمة للمدرب" else "ربط العدد بالكمية الفعلية",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    activeLetterDetail?.let {
                                        onSpeak(it.letter + "،  " + it.word)
                                    }
                                    activeNumberDetail?.let {
                                        onSpeak(it.arabicWord + "،  " + it.englishWord)
                                    }
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "استماع بالصوت", tint = SoftBluePrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Unique Articulation Help ("مخارج الحروف")
                        if (selectedTab < 2) {
                            activeLetterDetail?.let {
                                Text(
                                    "🗣️ مخرج ونطق الحرف وأجزاء الجسم المسؤولة:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PlayfulOrange
                                )
                                Text(
                                    it.articulation,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        } else {
                            activeNumberDetail?.let {
                                Text(
                                    "📊 نشاط عد وحساب الكمية بالرموز للأطفال:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MintGreen
                                )
                                Text(
                                    it.emojiRepresentation,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Log training performance button
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = {
                                    viewModel.logSessionActivity(
                                        activityName = "مرح الحروف والأرقام",
                                        score = 5,
                                        notes = "أتقن مخرج الكلمة: " + (activeLetterDetail?.word ?: activeNumberDetail?.arabicWord ?: "")
                                    )
                                    Toast.makeText(context, "تم رصد النطق الناجح في سجل المتابعة!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("✓ رصد كمنجز", fontWeight = FontWeight.Bold, color = MintGreen)
                            }
                        }
                    }
                }
            }

            // The main grid
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(arabicLetters) { item ->
                                LetterGridSquare(
                                    letter = item.letter,
                                    word = item.word,
                                    emoji = item.emoji,
                                    isSelected = activeLetterDetail?.letter == item.letter,
                                    onClick = {
                                        activeLetterDetail = item
                                        onSpeak(item.letter)
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(englishLetters) { item ->
                                LetterGridSquare(
                                    letter = item.letter,
                                    word = item.word,
                                    emoji = item.emoji,
                                    isSelected = activeLetterDetail?.letter == item.letter,
                                    onClick = {
                                        activeLetterDetail = item
                                        onSpeak(item.letter)
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(quantities) { item ->
                                Card(
                                    modifier = Modifier
                                        .aspectRatio(1.0f)
                                        .clickable {
                                            activeNumberDetail = item
                                            onSpeak(item.arabicWord)
                                        }
                                        .testTag("number_card_${item.value}"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (activeNumberDetail?.value == item.value) SoftBluePrimary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            item.value.toString(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PlayfulOrange
                                        )
                                        Text(
                                            item.arabicWord,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            item.englishWord,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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

@Composable
fun LetterGridSquare(
    letter: String,
    word: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1.0f)
            .clickable(onClick = onClick)
            .testTag("letter_grid_square_$letter"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PlayfulOrange.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                letter,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SoftBluePrimary
            )
            Text(
                word,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
