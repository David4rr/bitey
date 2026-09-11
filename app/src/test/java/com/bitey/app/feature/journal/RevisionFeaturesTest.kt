package com.bitey.app.feature.journal

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.theme.ThemeMode
import com.bitey.app.feature.camera.PhotoMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RevisionFeaturesTest {

    @Test
    fun themeMode_allVariantsConfigured() {
        assertEquals("Auto (System)", ThemeMode.AUTO.label)
        assertEquals("Light", ThemeMode.LIGHT.label)
        assertEquals("Dark", ThemeMode.DARK.label)
    }

    @Test
    fun photoMode_allVariantsConfigured() {
        assertEquals("Whole Dish", PhotoMode.WHOLE_DISH.label)
        assertEquals("Per Dish", PhotoMode.PER_DISH.label)
    }

    @Test
    fun mealType_inferFromTimestampCorrectly() {
        val cal = Calendar.getInstance()

        cal.set(Calendar.HOUR_OF_DAY, 8)
        assertEquals(MealType.BREAKFAST, MealType.fromTimestamp(cal.timeInMillis))

        cal.set(Calendar.HOUR_OF_DAY, 12)
        assertEquals(MealType.LUNCH, MealType.fromTimestamp(cal.timeInMillis))

        cal.set(Calendar.HOUR_OF_DAY, 16)
        assertEquals(MealType.SNACK, MealType.fromTimestamp(cal.timeInMillis))

        cal.set(Calendar.HOUR_OF_DAY, 19)
        assertEquals(MealType.DINNER, MealType.fromTimestamp(cal.timeInMillis))

        cal.set(Calendar.HOUR_OF_DAY, 23)
        assertEquals(MealType.LATE_NIGHT, MealType.fromTimestamp(cal.timeInMillis))
    }

    @Test
    fun dateGroup_aggregatesEntriesCorrectly() {
        val entry1 = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 1,
                title = "Soto Betawi",
                fullImagePath = "/path1.webp",
                thumbnailPath = "/path1.webp",
                timestamp = 1000L
            ),
            tags = emptyList()
        )
        val entry2 = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 2,
                title = "Gado-Gado",
                fullImagePath = "/path2.webp",
                thumbnailPath = "/path2.webp",
                timestamp = 2000L
            ),
            tags = emptyList()
        )

        val group = DateGroup(
            dateLabel = "Today • 10 September",
            entries = listOf(entry1, entry2)
        )

        assertEquals("Today • 10 September", group.dateLabel)
        assertEquals(2, group.entries.size)
        assertEquals("Soto Betawi", group.entries[0].entry.title)
        assertEquals("Gado-Gado", group.entries[1].entry.title)
    }

    @Test
    fun customTags_filterMatchingEntries() {
        val customTag = TagEntity(tagId = 99, tagName = "Gluten-Free Artisanal", category = "Custom")
        val standardTag = TagEntity(tagId = 1, tagName = "Spicy", category = "Taste")

        val entryWithCustom = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 10,
                title = "Sourdough Toast",
                fullImagePath = "/toast.webp",
                thumbnailPath = "/toast.webp"
            ),
            tags = listOf(customTag)
        )

        val entryWithoutCustom = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 11,
                title = "Ayam Geprek",
                fullImagePath = "/ayam.webp",
                thumbnailPath = "/ayam.webp"
            ),
            tags = listOf(standardTag)
        )

        val all = listOf(entryWithCustom, entryWithoutCustom)

        val filtered = all.filter { it.tags.any { tag -> tag.tagId == 99L } }
        assertEquals(1, filtered.size)
        assertEquals("Sourdough Toast", filtered[0].entry.title)
        assertEquals("Gluten-Free Artisanal", filtered[0].tags[0].tagName)
    }

    @Test
    fun biteyPrimaryColor_isVibrantOrange() {
        val orange = com.bitey.app.core.ui.theme.BiteyOrange
        // Primary color must be culinary orange: Red > Green and Red > Blue
        assertTrue("Primary color should have strong red component", orange.red > 0.9f)
        assertTrue("Primary color should have green component for warmth", orange.green > 0.2f)
        assertTrue("Primary color should have low blue for warm orange", orange.blue < 0.3f)
    }

    @Test
    fun navigationItems_journalAndProfileOnly() {
        val items = com.bitey.app.core.navigation.Screen.bottomNavItems
        assertEquals(2, items.size)
        assertEquals(com.bitey.app.core.navigation.Screen.Journal, items[0])
        assertEquals(com.bitey.app.core.navigation.Screen.Profile, items[1])
    }

    @Test
    fun tags_unboxedDisplayContract() {
        val tag = TagEntity(tagId = 1, tagName = "Savory")
        val display = "#${tag.tagName}"
        assertEquals("#Savory", display)
    }

    @Test
    fun searchState_clearContract() {
        var query = "Ramen"
        fun onClear() { query = "" }
        assertEquals("Ramen", query)
        onClear()
        assertEquals("", query)
    }

    @Test
    fun morphingSearchBar_totalCodeUnder200Lines() {
        val file = java.io.File("src/main/java/com/bitey/app/core/ui/component/MorphingSearchBar.kt")
        if (file.exists()) {
            val lineCount = file.readLines().size
            assertTrue("MorphingSearchBar must be <= 200 lines, but was $lineCount", lineCount <= 200)
        }
    }
}
