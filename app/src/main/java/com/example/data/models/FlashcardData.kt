package com.example.data.models

data class FlashcardItem(
    val arabicName: String,
    val englishName: String,
    val category: String,
    val emoji: String,
    val articulationTip: String = "",
    val companionEmoji: String = "",
    val imageResName: String? = null,
    val imageUrl: String? = null
)

object FlashcardDatabase {
    val categories = listOf(
        "الحيوانات الأليفة",
        "الحيوانات المفترسة والبرية",
        "الفواكه",
        "الخضروات",
        "وسائل المواصلات",
        "الملابس",
        "أثاث وأدوات المنزل",
        "أجهزة كهربائية",
        "أدوات المطبخ",
        "أدوات الحمام",
        "الأدوات المدرسية",
        "أعضاء الجسم",
        "ألعاب الأطفال",
        "الأشكال الهندسية والألوان",
        "أدوات المهن والتصليح",
        "أطعمة ومأكولات",
        "عناصر الطبيعة والطقس",
        "أدوات العناية الشخصية والنظافة",
        "البحر والكائنات البحرية",
        "الطيور والحشرات",
        "المشاعر والانفعالات"
    )

    private val allGeneratedItems = ArrayList<FlashcardItem>()

    init {
        // Category 1: الحيوانات الأليفة
        addItemsCategory("الحيوانات الأليفة", listOf(
            Pair("كلب", "Dog"), Pair("قطة", "Cat"), Pair("أرنب", "Rabbit"), Pair("حمار", "Donkey"),
            Pair("حصان", "Horse"), Pair("خروف", "Sheep"), Pair("ماعز", "Goat"), Pair("بقرة", "Cow"),
            Pair("جاموسة", "Buffalo"), Pair("بطة", "Duck"), Pair("دجاجة", "Chicken"), Pair("ديك", "Rooster"),
            Pair("كتكوت", "Chick"), Pair("حمامة", "Pigeon"), Pair("عصفور", "Bird"), Pair("إوزة", "Goose"),
            Pair("سلحفاة", "Turtle"), Pair("جمل", "Camel"), Pair("ناقة", "She-Camel"), Pair("جرو", "Puppy")
        ), listOf(
            "🐶", "🐱", "🐰", "🫏", "🐴", "🐑", "🐐", "🐄", "🐃", "🦆", "🐔", "🐓", "🐥", "🕊️", "🐦", "🪿", "🐢", "🐪", "🐪", "🐶"
        ))

        // Category 2: الحيوانات المفترسة والبرية
        addItemsCategory("الحيوانات المفترسة والبرية", listOf(
            Pair("أسد", "Lion"), Pair("نمر", "Tiger"), Pair("فهد", "Leopard"), Pair("ذئب", "Wolf"),
            Pair("ثعلب", "Fox"), Pair("دب", "Bear"), Pair("قرد", "Monkey"), Pair("غزال", "Deer"),
            Pair("زرافة", "Giraffe"), Pair("فيل", "Elephant"), Pair("حمار وحشي", "Zebra"), Pair("وحيد القرن", "Rhino"),
            Pair("فرس النهر", "Hippo"), Pair("تمساح", "Crocodile"), Pair("ثعبان", "Snake"), Pair("ضبع", "Hyena"),
            Pair("سنجاب", "Squirrel"), Pair("قنفذ", "Hedgehog"), Pair("كانغورو", "Kangaroo"), Pair("كوالا", "Koala")
        ), listOf(
            "🦁", "🐯", "🐆", "🐺", "🦊", "🐻", "🐒", "🦌", "🦒", "🐘", "ZEBRA", "🦏", "🦛", "🐊", "🐍", "🐺", "🐿️", "🦔", "🦘", "🐨"
        ))

        // Category 3: الفواكه
        addItemsCategory("الفواكه", listOf(
            Pair("تفاح", "Apple"), Pair("موز", "Banana"), Pair("برتقال", "Orange"), Pair("فراولة", "Strawberry"),
            Pair("مانجو", "Mango"), Pair("بطيخ", "Watermelon"), Pair("عنب", "Grapes"), Pair("خوخ", "Peach"),
            Pair("مشمش", "Apricot"), Pair("كمثرى", "Pear"), Pair("أناناس", "Pineapple"), Pair("كيوي", "Kiwi"),
            Pair("رمان", "Pomegranate"), Pair("تين", "Fig"), Pair("كرز", "Cherry"), Pair("توت", "Berry"),
            Pair("ليمون", "Lemon"), Pair("جوافة", "Guava"), Pair("بلح", "Dates"), Pair("شمام", "Melon")
        ), listOf(
            "🍎", "🍌", "🍊", "🍓", "🥭", "🍉", "🍇", "🍑", "🍑", "🍐", "🍍", "🥝", "🫐", "🍇", "🍒", "🫐", "🍋", "🍏", "🌴", "🍈"
        ))

        // Category 4: الخضروات
        addItemsCategory("الخضروات", listOf(
            Pair("طماطم", "Tomato"), Pair("خيار", "Cucumber"), Pair("بطاطس", "Potato"), Pair("جزر", "Carrot"),
            Pair("بصل", "Onion"), Pair("ثوم", "Garlic"), Pair("فلفل", "Pepper"), Pair("باذنجان", "Eggplant"),
            Pair("كوسة", "Zucchini"), Pair("بسلة", "Peas"), Pair("فاصوليا", "Beans"), Pair("سبانخ", "Spinach"),
            Pair("ملوخية", "Mallow"), Pair("بامية", "Okra"), Pair("كرنب", "Cabbage"), Pair("قرنبيط", "Cauliflower"),
            Pair("خس", "Lettuce"), Pair("جرجير", "Arugula"), Pair("فجل", "Radish"), Pair("لفت", "Turnip")
        ), listOf(
            "🍅", "🥒", "🥔", "🥕", "🧅", "🧄", "🫑", "🍆", "🥒", "🫛", "🫛", "🥬", "🥬", "🥒", "🥬", "🥦", "🥬", "🥬", "🥬", "🧅"
        ))

        // Category 5: وسائل المواصلات
        addItemsCategory("وسائل المواصلات", listOf(
            Pair("سيارة", "Car"), Pair("باص", "Bus"), Pair("قطار", "Train"), Pair("طائرة", "Plane"),
            Pair("سفينة", "Ship"), Pair("قارب", "Boat"), Pair("دراجة هوائية", "Bicycle"), Pair("دراجة نارية", "Motorcycle"),
            Pair("شاحنة", "Truck"), Pair("سيارة إسعاف", "Ambulance"), Pair("سيارة مطافئ", "Fire Engine"), Pair("سيارة شرطة", "Police Car"),
            Pair("مترو", "Metro"), Pair("هليكوبتر", "Helicopter"), Pair("صاروخ", "Rocket"), Pair("جرافة", "Bulldozer"),
            Pair("رافعة", "Crane"), Pair("جرار زراعي", "Tractor"), Pair("توكتوك", "Tuk-Tuk"), Pair("مركبة فضائية", "Spacecraft")
        ), listOf(
            "🚗", "🚌", "🚂", "✈️", "🚢", "⛵", "🚲", "🏍️", "🚚", "🚑", "🚒", "🚓", "🚇", "🚁", "🚀", "🚜", "🏗️", "🚜", "🛺", "🚀"
        ))

        // Category 6: الملابس
        addItemsCategory("الملابس", listOf(
            Pair("قميص", "Shirt"), Pair("سروال", "Pants"), Pair("فستان", "Dress"), Pair("تنورة", "Skirt"),
            Pair("قميص قطني", "T-Shirt"), Pair("سترة", "Jacket"), Pair("معطف", "Coat"), Pair("كنزة صوفية", "Sweater"),
            Pair("جورب", "Socks"), Pair("حذاء", "Shoes"), Pair("قبعة", "Hat"), Pair("قفاز", "Gloves"),
            Pair("وشاح", "Scarf"), Pair("ملابس نوم", "Pajamas"), Pair("عباءة", "Robe"), Pair("حزام", "Belt"),
            Pair("ربطة عنق", "Tie"), Pair("سروال قصير", "Shorts"), Pair("ملابس سباحة", "Swimsuit"), Pair("قبعة صوفية", "Beanie")
        ), listOf(
            "👔", "👖", "👗", "👗", "👕", "🧥", "🧥", "🧶", "🧦", "👟", "🎩", "🧤", "🧣", "🛌", "🥋", "🎗️", "👔", "🩳", "🩱", "🧦"
        ))

        // Category 7: أثاث وأدوات المنزل
        addItemsCategory("أثاث وأدوات المنزل", listOf(
            Pair("سرير", "Bed"), Pair("خزانة ملابس", "Wardrobe"), Pair("طاولة", "Table"), Pair("كرسي", "Chair"),
            Pair("أريكة", "Sofa"), Pair("سجادة", "Carpet"), Pair("ستارة", "Curtain"), Pair("مرآة", "Mirror"),
            Pair("مصباح", "Lamp"), Pair("ساعة حائط", "Wall Clock"), Pair("لوحة جدارية", "Painting"), Pair("رف خشب", "Shelf"),
            Pair("باب", "Door"), Pair("نافذة", "Window"), Pair("مفتاح", "Key"), Pair("قفل", "Lock"),
            Pair("سلة مهملات", "Trash Bin"), Pair("شماعة ملابس", "Hanger"), Pair("وسادة", "Pillow"), Pair("بطانية", "Blanket")
        ), listOf(
            "🛏️", "🚪", "🪵", "🪑", "🛋️", "🪵", "🪟", "🪞", "💡", "🕰️", "🖼️", "🪵", "🚪", "🪟", "🔑", "🔒", "🗑️", "🪵", "🛌", "🛌"
        ))

        // Category 8: أجهزة كهربائية
        addItemsCategory("أجهزة كهربائية", listOf(
            Pair("تلفاز", "TV"), Pair("ثلاجة", "Fridge"), Pair("غسالة", "Washing Machine"), Pair("موقد كهربائي", "Stove"),
            Pair("ميكروويف", "Microwave"), Pair("خلاط كهربائي", "Blender"), Pair("مكواة ملابس", "Iron"), Pair("مكنسة كهربائية", "Vacuum"),
            Pair("مروحة", "Fan"), Pair("مكيف هواء", "Air Conditioner"), Pair("سخان مياه", "Heater"), Pair("حاسوب بمكتب", "Computer"),
            Pair("حاسوب محمول", "Laptop"), Pair("هاتف محمول", "Phone"), Pair("مجفف شعر", "Hair Dryer"), Pair("غلاية مياه", "Kettle"),
            Pair("غسالة أطباق", "Dishwasher"), Pair("سماعة", "Speaker"), Pair("كشاف ضوئي", "Flashlight"), Pair("شاحن هاتف", "Charger")
        ), listOf(
            "📺", "🧊", "🫧", "🍳", "🎛️", "🥤", "🔌", "🧹", "🌀", "❄️", "🔥", "🖥️", "💻", "📱", "🪮", "☕", "🍽️", "🔊", "🔦", "🔌"
        ))

        // Category 9: أدوات المطبخ
        addItemsCategory("أدوات المطبخ", listOf(
            Pair("ملعقة", "Spoon"), Pair("شوكة", "Fork"), Pair("سكين", "Knife"), Pair("طبق طعام", "Plate"),
            Pair("كوب ماء", "Cup"), Pair("إبريق شاي", "Kettle"), Pair("قدر طهي", "Pot"), Pair("مقلاة", "Pan"),
            Pair("صينية تقديم", "Tray"), Pair("مصفاة طعام", "Strainer"), Pair("مبشرة خضار", "Grater"), Pair("فنجان قهوة", "Teacup"),
            Pair("زجاجة ماء", "Water Bottle"), Pair("علبة طعام", "Lunchbox"), Pair("إسفنجة غسيل", "Sponge"), Pair("فوطة مطبخ", "Towel"),
            Pair("مقشرة خضار", "Peeler"), Pair("هراسة طعام", "Masher"), Pair("قداحة ميكانيكية", "Lighter"), Pair("مصفاة ماء", "Drainer")
        ), listOf(
            "🥄", "🍴", "🔪", "🍽️", "🥛", "🫖", "🍲", "🍳", "🍛", "🥫", "🫖", "☕", "🍼", "🍱", "🧼", "🧻", "🔪", "🥔", "🔥", "🥫"
        ))

        // Category 10: أدوات الحمام
        addItemsCategory("أدوات الحمام", listOf(
            Pair("فرشاة أسنان", "Toothbrush"), Pair("معجون أسنان", "Toothpaste"), Pair("صابون يدين", "Soap"), Pair("مستحضر شامبو", "Shampoo"),
            Pair("منشفة قطنية", "Towel"), Pair("حوض غسيل", "Sink"), Pair("حوض استحمام", "Bathtub"), Pair("قاعدة تواليت", "Toilet"),
            Pair("مرش مياه دُش", "Shower"), Pair("ورق تواليت", "Toilet Paper"), Pair("غسول جلد", "Lotion"), Pair("مشط شعر", "Comb"),
            Pair("مقص أظافر", "Nail Clipper"), Pair("مرآة حمام", "Mirror"), Pair("سدادة حوض", "Stopper"), Pair("ممسحة أرضيات", "Mop"),
            Pair("سطل مياه", "Bucket"), Pair("حبل غسيل", "Clothesline"), Pair("مشابك غسيل", "Clips"), Pair("حفاضة أطفال", "Diaper")
        ), listOf(
            "🪥", "🪥", "🧼", "🧼", "🧻", "🚰", "🛁", "🚽", "🚿", "🧻", "🧴", "🪮", "✂️", "🪞", "🔌", "🧹", "🪣", "🧺", "🧺", "👶"
        ))

        // Category 11: الأدوات المدرسية
        addItemsCategory("الأدوات المدرسية", listOf(
            Pair("قلم رصاص", "Pencil"), Pair("قلم جاف", "Pen"), Pair("دفتر", "Notebook"), Pair("كتاب", "Book"),
            Pair("ممحاة", "Eraser"), Pair("براية", "Sharpener"), Pair("مسطرة", "Ruler"), Pair("مقلمة", "Pencil Case"),
            Pair("حقيبة مدرسية", "School Bag"), Pair("ألوان تلوين", "Colors"), Pair("صمغ", "Glue"), Pair("مقص ورق", "Scissors"),
            Pair("فرجار", "Compass"), Pair("لوحة مجسمة", "Board"), Pair("طباشير", "Chalk"), Pair("مقعد مدرسي", "Desk"),
            Pair("سبورة", "Whiteboard"), Pair("دباسة", "Stapler"), Pair("مكتب", "Desk"), Pair("خريطة", "Map")
        ), listOf(
            "✏️", "🖊️", "📓", "📖", "🧼", "✏️", "📏", "🎒", "🎒", "🎨", "🧴", "✂️", "📐", "📋", "🖍️", "🪑", "📋", "🖇️", "🪑", "🗺️"
        ))

        // Category 12: أعضاء الجسم
        addItemsCategory("أعضاء الجسم", listOf(
            Pair("عين", "Eye"), Pair("أذن", "Ear"), Pair("أنف", "Nose"), Pair("فم", "Mouth"),
            Pair("لسان", "Tongue"), Pair("أسنان", "Teeth"), Pair("شعر", "Hair"), Pair("رأس", "Head"),
            Pair("وجه", "Face"), Pair("يد", "Hand"), Pair("أصابع", "Fingers"), Pair("قدم", "Foot"),
            Pair("رجل", "Leg"), Pair("ذراع", "Arm"), Pair("بطن", "Belly"), Pair("ظهر", "Back"),
            Pair("صدر", "Chest"), Pair("ركبة", "Knee"), Pair("رقبة", "Neck"), Pair("كتف", "Shoulder")
        ), listOf(
            "👁️", "👂", "👃", "👄", "👅", "🦷", "🦱", "🧒", "👦", "✋", "☝️", "🦶", "🦵", "💪", "🤰", "🧍", "👕", "🦵", "🦒", "👕"
        ))

        // Category 13: ألعاب الأطفال
        addItemsCategory("ألعاب الأطفال", listOf(
            Pair("كرة", "Ball"), Pair("دمية صغيرة", "Doll"), Pair("سيارة لعبة", "Toy Car"), Pair("قطار خشبي", "Toy Train"),
            Pair("مكعبات ملونة", "Blocks"), Pair("أرجوحة", "Swing"), Pair("منزلق", "Slide"), Pair("طائرة ورقية", "Kite"),
            Pair("بالون طائر", "Balloon"), Pair("صلصال ملون", "Playdough"), Pair("نرد الألعاب", "Dice"), Pair("طبلة إيقاع", "Toy Drum"),
            Pair("يويو دوار", "Yo-Yo"), Pair("دب لعبة", "Teddy Bear"), Pair("حصان هزاز", "Hobby Horse"), Pair("خشخاشة أطفال", "Rattle"),
            Pair("لغز تركيب", "Puzzle"), Pair("نفق الألعاب", "Toy Tunnel"), Pair("نطاطة ترامبولين", "Trampoline"), Pair("دراجة تزلج سكوتر", "Scooter")
        ), listOf(
            "⚽", "🪆", "🚗", "🚂", "🧱", "🛝", "🛝", "🪁", "🎈", "🎨", "🎲", "🥁", "🪀", "🧸", "🐴", "🪇", "🧩", "🚇", "🤸", "🛴"
        ))

        // Category 14: الأشكال الهندسية والألوان
        addItemsCategory("الأشكال الهندسية والألوان", listOf(
            Pair("دائرة", "Circle"), Pair("مربع", "Square"), Pair("مثلث", "Triangle"), Pair("مستطيل", "Rectangle"),
            Pair("نجمة", "Star"), Pair("قلب", "Heart"), Pair("شكل بيضاوي", "Oval"), Pair("معين", "Rhombus"),
            Pair("أحمر", "Red"), Pair("أزرق", "Blue"), Pair("أخضر", "Green"), Pair("أصفر", "Yellow"),
            Pair("أسود", "Black"), Pair("أبيض", "White"), Pair("برتقالي", "Orange"), Pair("بنفسجي", "Purple"),
            Pair("وردي", "Pink"), Pair("بني", "Brown"), Pair("رمادي", "Grey"), Pair("ذهبي", "Gold")
        ), listOf(
            "🔴", "🟧", "🔺", "🟩", "⭐", "❤️", "🥚", "🔷", "🔴", "🔵", "🟢", "🟡", "⚫", "⚪", "🟠", "🟣", "🌸", "🟤", "🩶", "✨"
        ))

        // Category 15: أدوات المهن والتصليح
        addItemsCategory("أدوات المهن والتصليح", listOf(
            Pair("مطرقة", "Hammer"), Pair("مفك براغي", "Screwdriver"), Pair("زرادية", "Pliers"), Pair("منشار يدوي", "Saw"),
            Pair("مسمار حديدي", "Nail"), Pair("صامولة ربط", "Nut"), Pair("شريط قياس", "Tape"), Pair("مقص حديد", "Shears"),
            Pair("ميزان قياس", "Scale"), Pair("سماعة طبيب", "Stethoscope"), Pair("حقنة طبية", "Syringe"), Pair("ميزان حرارة", "Thermometer"),
            Pair("فرشاة دهان", "Paintbrush"), Pair("رول دهان", "Paint Roller"), Pair("خوذة سلامة", "Helmet"), Pair("مجرفة", "Shovel"),
            Pair("قلم سبورة", "Marker"), Pair("كشاف", "Flashlight"), Pair("صفارة", "Whistle"), Pair("لوحة ألوان", "Palette")
        ), listOf(
            "🔨", "🪛", "✂️", "🪚", "🔩", "🔩", "📏", "✂️", "⚖️", "🩺", "💉", "🌡️", "🖌️", "🖌️", "🪖", "🧹", "🖊️", "🔦", "📢", "🎨"
        ))

        // Category 16: أطعمة ومأكولات
        addItemsCategory("أطعمة ومأكولات", listOf(
            Pair("أرز", "Rice"), Pair("مكرونة", "Pasta"), Pair("خبز", "Bread"), Pair("بيض", "Egg"),
            Pair("جبن", "Cheese"), Pair("حليب", "Milk"), Pair("لحم", "Meat"), Pair("دجاج", "Chicken"),
            Pair("سمك", "Fish"), Pair("شوربة", "Soup"), Pair("عسل", "Honey"), Pair("مربى", "Jam"),
            Pair("زبدة", "Butter"), Pair("بسكويت", "Biscuit"), Pair("كعك", "Cake"), Pair("شوكولاتة", "Chocolate"),
            Pair("حلوى", "Candy"), Pair("آيس كريم", "Ice Cream"), Pair("شيبس", "Chips"), Pair("مكسرات", "Nuts")
        ), listOf(
            "🍚", "🍝", "🍞", "🥚", "🧀", "🥛", "🥩", "🍗", "🐟", "🍲", "🍯", "🍯", "🧈", "🍪", "🍰", "🍫", "🍬", "🍦", "🍟", "🥜"
        ))

        // Category 17: عناصر الطبيعة والطقس
        addItemsCategory("عناصر الطبيعة والطقس", listOf(
            Pair("شمس", "Sun"), Pair("قمر", "Moon"), Pair("نجوم", "Stars"), Pair("سحاب", "Cloud"),
            Pair("مطر", "Rain"), Pair("ثلج", "Snow"), Pair("شجرة", "Tree"), Pair("وردة", "Flower"),
            Pair("عشب", "Grass"), Pair("جبل", "Mountain"), Pair("بحر", "Sea"), Pair("نهر", "River"),
            Pair("رمل", "Sand"), Pair("صخرة", "Rock"), Pair("قوس قزح", "Rainbow"), Pair("رعد", "Thunder"),
            Pair("برق", "Lightning"), Pair("نار", "Fire"), Pair("ريح", "Wind"), Pair("تراب", "Dust")
        ), listOf(
            "☀️", "🌙", "⭐", "☁️", "🌧️", "❄️", "🌳", "🌹", "🌱", "🏔️", "🌊", "🌊", "🏖️", "🪨", "🌈", "⛈️", "⚡", "🔥", "💨", "💨"
        ))

        // Category 18: أدوات العناية الشخصية والنظافة
        addItemsCategory("أدوات العناية الشخصية والنظافة", listOf(
            Pair("مناديل ورقية", "Tissues"), Pair("مناديل مبللة", "Wipes"), Pair("عطر فواح", "Perfume"), Pair("مرطب بشرة", "Cream"),
            Pair("لوشن مغذي", "Lotion"), Pair("مقص شعر", "Haircut"), Pair("مجفف شعر", "Dryer"), Pair("مشبك شعر", "Hairclip"),
            Pair("عصابة رأس", "Headband"), Pair("مرطب شفاه", "Lip Balm"), Pair("بودرة أطفال", "Baby Powder"), Pair("عود قطني", "Cotton Bud"),
            Pair("مبرد أظافر", "Nail File"), Pair("مطهر جروح", "Antiseptic"), Pair("غسول فم", "Mouthwash"), Pair("معقم يدين", "Sanitizer"),
            Pair("فرشاة شعر", "Hairbrush"), Pair("إسفنجة استحمام", "Sponge"), Pair("مئزر حمام", "Apron"), Pair("كمامة وجه", "Mask")
        ), listOf(
            "🧻", "🧻", "🧴", "🧴", "🧴", "✂️", "🪮", "🎀", "🎀", "💄", "🧴", "🧽", "💅", "🧪", "🧪", "🧴", "🪮", "🧽", "🎽", "😷"
        ))

        // Category 19: البحر والكائنات البحرية
        addItemsCategory("البحر والكائنات البحرية", listOf(
            Pair("سمكة", "Fish"), Pair("دولفين", "Dolphin"), Pair("حوت", "Whale"), Pair("قرش", "Shark"),
            Pair("أخطبوط", "Octopus"), Pair("قنديل البحر", "Jellyfish"), Pair("نجم البحر", "Starfish"), Pair("سرطان البحر", "Crab"),
            Pair("جمبري", "Shrimp"), Pair("سلحفاة بحرية", "Sea Turtle"), Pair("فقمة", "Seal"), Pair("سبع البحر", "Walrus"),
            Pair("محار", "Oyster"), Pair("قوقعة", "Shell"), Pair("مرجان", "Coral"), Pair("شبكة صيد", "Fishing Net"),
            Pair("صنارة", "Fishing Rod"), Pair("عوامة", "Buoy"), Pair("سفينة صيد", "fishing boat"), Pair("غواصة", "Submarine")
        ), listOf(
            "🐟", "🐬", "🐋", "🦈", "🐙", "🪼", "⭐", "🦀", "🍤", "🐢", "🦭", "🦭", "🦪", "🐚", "🪸", "🕸️", "🎣", "🛟", "⛵", "🦺"
        ))

        // Category 20: الطيور والحشرات
        addItemsCategory("الطيور والحشرات", listOf(
            Pair("صقر", "Falcon"), Pair("نسر", "Eagle"), Pair("بومة", "Owl"), Pair("هدهد", "Hoopoe"),
            Pair("ببقاء", "Parrot"), Pair("نعامة", "Ostrich"), Pair("بطريق", "Penguin"), Pair("طاووس", "Peacock"),
            Pair("غراب", "Crow"), Pair("بجعة", "Swan"), Pair("نملة", "Ant"), Pair("نحلة", "Bee"),
            Pair("فراشة", "Butterfly"), Pair("ذبابة", "Fly"), Pair("ناموسة", "Mosquito"), Pair("صرصور", "Roach"),
            Pair("عنكبوت", "Spider"), Pair("خنفساء", "Beetle"), Pair("دودة", "Worm"), Pair("جرادة", "Locust")
        ), listOf(
            "🦅", "🦅", "🦉", "🐦", "🦜", "🦩", "🐧", "🦚", "🐦", "🦢", "🐜", "🐝", "🦋", "🪰", "🦟", "🪳", "🕷️", "🪲", "🐛", "🦗"
        ))

        // Category 21: المشاعر والانفعالات
        addItemsCategory("المشاعر والانفعالات", listOf(
            Pair("سعيد", "Happy"), Pair("حزين", "Sad"), Pair("غاضب", "Angry"), Pair("خائف", "Scared"),
            Pair("متفاجئ", "Surprised"), Pair("متملل", "Bored"), Pair("متحمس", "Excited"), Pair("فخور", "Proud"),
            Pair("قلق", "Anxious"), Pair("هادئ", "Calm"), Pair("خجول", "Shy"), Pair("تعبان", "Tired"),
            Pair("مريض", "Sick"), Pair("جائع", "Hungry"), Pair("عطشان", "Thirsty"), Pair("نعسان", "Sleepy"),
            Pair("غيور", "Jealous"), Pair("محرج", "Embarrassed"), Pair("محب", "Loving"), Pair("واثق", "Confident"),
            Pair("صبور", "Patient"), Pair("نادم", "Regretful"), Pair("نشيط", "Energetic"), Pair("كسول", "Lazy"),
            Pair("ممتن", "Grateful")
        ), listOf(
            "😊", "😢", "😠", "😨", "😲", "🥱", "🤩", "😌", "😟", "🧘", "🫣", "😫", "🤒", "😋", "🥵", "😴", "😒", "😳", "🥰", "😎", "⏳", "😔", "⚡", "🦥", "🙏"
        ))
    }

    private fun addItemsCategory(catName: String, namePairs: List<Pair<String, String>>, emojis: List<String>) {
        for (i in namePairs.indices) {
            val ar = namePairs[i].first
            val en = namePairs[i].second
            val emo = if (i < emojis.size) emojis[i] else "⭐"
            val sanitizedEng = en.lowercase().replace(" ", "_").replace("-", "_")
            val resName = "img_$sanitizedEng"
            allGeneratedItems.add(
                FlashcardItem(
                    arabicName = ar,
                    englishName = en,
                    category = catName,
                    emoji = emo,
                    articulationTip = "احرص على نطق حرف (${ar.take(1)}) بوضوح تام، بطيء ونبرة مريحة للطفل.",
                    imageResName = resName
                )
            )
        }
    }

    val items: List<FlashcardItem>
        get() = allGeneratedItems
}
