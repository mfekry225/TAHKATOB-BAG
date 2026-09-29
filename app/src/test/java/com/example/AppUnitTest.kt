package com.example

import com.example.data.local.ChildEntity
import com.example.data.local.SessionLogEntity
import com.example.data.models.FlashcardDatabase
import com.example.data.models.FlashcardItem
import com.example.ui.screens.GameCategory
import com.example.ui.screens.MazeLevel
import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Test

class AppUnitTest {

    @Test
    fun `flashcard database categories and items are loaded correctly`() {
        val categories = FlashcardDatabase.categories
        assertNotNull(categories)
        assertTrue("Categories list should not be empty", categories.isNotEmpty())
        assertEquals(21, categories.size)

        val items = FlashcardDatabase.items
        assertNotNull(items)
        assertTrue("Items list should not be empty", items.isNotEmpty())
        assertTrue("Should have more than 100 flashcards", items.size >= 100)

        // Verify each item has non-empty fields
        for (item in items) {
            assertTrue("Arabic name should not be blank", item.arabicName.isNotBlank())
            assertTrue("English name should not be blank", item.englishName.isNotBlank())
            assertTrue("Category should not be blank", item.category.isNotBlank())
            assertTrue("Category should be in defined categories", categories.contains(item.category))
        }
    }

    @Test
    fun `flashcard item real-world image fields defaults to null or can be set`() {
        val itemWithEmojiOnly = FlashcardItem(
            arabicName = "كلب",
            englishName = "Dog",
            category = "الحيوانات الأليفة",
            emoji = "🐶"
        )
        assertNull(itemWithEmojiOnly.imageResName)
        assertNull(itemWithEmojiOnly.imageUrl)

        val itemWithRealPhoto = FlashcardItem(
            arabicName = "كلب",
            englishName = "Dog",
            category = "الحيوانات الأليفة",
            emoji = "🐶",
            imageResName = "img_dog_real_photo",
            imageUrl = "https://example.com/photos/dog.jpg"
        )
        assertEquals("img_dog_real_photo", itemWithRealPhoto.imageResName)
        assertEquals("https://example.com/photos/dog.jpg", itemWithRealPhoto.imageUrl)
    }

    @Test
    fun `game categories enum values are distinct`() {
        val categories = GameCategory.values()
        assertEquals(5, categories.size)
        val keys = categories.map { it.key }
        assertEquals("Keys should be unique", keys.size, keys.toSet().size)
    }

    @Test
    fun `maze level track points configuration is valid`() {
        val level = MazeLevel(
            id = "easy",
            name = "سهل",
            startEmoji = "🚗",
            endEmoji = "🏠",
            targetMessage = "وصل السيارة",
            trackPoints = listOf(
                Offset(0.15f, 0.5f),
                Offset(0.85f, 0.5f)
            ),
            tolerance = 0.12f
        )
        assertEquals("easy", level.id)
        assertEquals(2, level.trackPoints.size)
        assertTrue("Tolerance should be positive", level.tolerance > 0f)
    }

    @Test
    fun `child entity creation and default values`() {
        val child = ChildEntity(
            id = 1,
            name = "أحمد",
            age = 5,
            mentalAge = 5,
            notes = "صعوبة في نطق حرف الراء",
            country = "مصر",
            avatarColor = 0xFF4A90E2.toInt()
        )
        assertEquals(1, child.id)
        assertEquals("أحمد", child.name)
        assertEquals(5, child.age)
        assertEquals("مصر", child.country)
        assertTrue(child.createdAt > 0L)
    }

    @Test
    fun `session log entity fields match log requirements`() {
        val log = SessionLogEntity(
            id = 10,
            childId = 1,
            activityName = "البطاقات التعليمية",
            score = 100,
            notes = "أنجز الطفل بطاقة: كلب"
        )
        assertEquals(10, log.id)
        assertEquals(1, log.childId)
        assertEquals("البطاقات التعليمية", log.activityName)
        assertEquals(100, log.score)
        assertEquals("أنجز الطفل بطاقة: كلب", log.notes)
        assertTrue(log.timestamp > 0L)
    }
}
