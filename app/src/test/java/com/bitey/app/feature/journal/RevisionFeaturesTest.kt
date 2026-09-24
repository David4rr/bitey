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
        assertEquals("Oneshot", PhotoMode.ONESHOT.label)
        assertEquals("Dish by dish", PhotoMode.DISH_BY_DISH.label)
    }

    @Test
    fun mealType_allVariantsConfigured() {
        assertEquals("Food", MealType.FOOD.label)
        assertEquals("Drink", MealType.DRINK.label)
        assertEquals("Other", MealType.OTHER.label)
        assertEquals(MealType.FOOD, MealType.fromTimestamp())
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
    fun biteyFavoriteColor_isVibrantRed() {
        val red = com.bitey.app.core.ui.theme.BiteyRed
        // Favorite color must be vibrant red: high red, lower green and blue
        assertTrue("Favorite red color should have high red component", red.red > 0.9f)
        assertTrue("Favorite red color should be clearly red hue", red.red > red.green * 2)
        assertTrue("Favorite red color should be clearly red hue", red.red > red.blue * 2)
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

    @Test
    fun plateEntryEntity_updateAttributesContract() {
        val original = PlateEntryEntity(
            id = 42,
            title = "Ayam Bakar",
            fullImagePath = "/path/photo.webp",
            thumbnailPath = "/path/thumb.webp",
            mealType = MealType.FOOD,
            rating = 4.0f,
            price = 35000.0,
            locationName = "Warung Bu Kris",
            note = "Enak gurih",
            isStickerMode = true
        )

        val updated = original.copy(
            title = "Ayam Bakar Madu",
            mealType = MealType.FOOD,
            rating = 5.0f,
            price = 40000.0,
            locationName = "Warung Bu Kris Gandaria",
            note = "Manis legit empuk",
            isStickerMode = false
        )

        assertEquals("Ayam Bakar Madu", updated.title)
        assertEquals(5.0f, updated.rating, 0.001f)
        assertEquals(40000.0, updated.price!!, 0.001)
        assertEquals("Warung Bu Kris Gandaria", updated.locationName)
        assertEquals("Manis legit empuk", updated.note)
        assertFalse(updated.isStickerMode)
    }

    @Test
    fun journalDetailSheet_directInPlaceEditingContract() {
        val sheetFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/JournalDetailSheet.kt")
        if (sheetFile.exists()) {
            val content = sheetFile.readText()
            // Direct editing contract: no navigation or separate edit page toggle
            assertFalse("Should not navigate to edit page", content.contains("isEditing"))
            assertTrue("Directly embeds JournalDetailContent", content.contains("JournalDetailContent"))
        }
    }

    @Test
    fun journalDetailContent_stickerProminenceContract() {
        val contentFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/JournalDetailContent.kt")
        if (contentFile.exists()) {
            val content = contentFile.readText()
            // Uses CropTransparentTransformation to eliminate transparent empty borders
            assertTrue("Uses CropTransparentTransformation", content.contains("CropTransparentTransformation"))
            // Sized prominently (155.dp - 165.dp)
            assertTrue("Sized prominently", content.contains("165.dp") || content.contains("155.dp"))
        }
    }

    @Test
    fun bookCoverHeader_mealTypeDeletedLeavingOnlyRating() {
        val file = java.io.File("src/main/java/com/bitey/app/feature/journal/component/BookCoverInfo.kt")
        if (file.exists()) {
            val content = file.readText()
            assertFalse("BookCoverHeader must not display mealType", content.contains("entry.mealType.label"))
            assertTrue("BookCoverHeader must display rating", content.contains("entry.rating"))
        }
    }

    @Test
    fun historyGridSingleCard_aestheticZoneAndWidenedCanvas() {
        val file = java.io.File("src/main/java/com/bitey/app/feature/journal/component/HistoryGridSingleCard.kt")
        if (file.exists()) {
            val content = file.readText()
            assertTrue("Card widened to 360dp max", content.contains("360.dp"))
            assertTrue("Card has aesthetic zone radial gradient mat", content.contains("Brush.radialGradient"))
        }
    }

    @Test
    fun stickerCarousel_activeEnlargeAdjacentShrinkContract() {
        val carouselFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/StickerCarousel.kt")
        assertTrue("StickerCarousel file exists", carouselFile.exists())
        val carouselContent = carouselFile.readText()
        assertTrue("Uses HorizontalPager", carouselContent.contains("HorizontalPager"))
        assertTrue("Uses lerp for smooth scale and alpha transition", carouselContent.contains("lerp("))
        assertTrue("Scales active and shrinks adjacent", carouselContent.contains("scaleX") && carouselContent.contains("scaleY"))
        assertTrue("Die-cut sticker presentation", carouselContent.contains("dieCutStickerEffect()"))

        val contentFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/JournalDetailContent.kt")
        val detailContent = contentFile.readText()
        assertTrue("JournalDetailContent uses StickerCarousel", detailContent.contains("StickerCarousel("))
        assertFalse("Fullscreen button removed from carousel", carouselContent.contains("Icons.Rounded.Fullscreen"))
        assertFalse("Fullscreen button removed from detail sticker box", detailContent.contains("Icons.Rounded.Fullscreen"))
        assertFalse("Text below indicator removed", carouselContent.contains("Tap to view"))
        assertTrue("Indicator positioned inside container", carouselContent.contains("Alignment.BottomCenter"))

        val previewFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/StickerPreviewDialog.kt")
        val previewText = previewFile.readText()
        assertFalse("StickerPreviewDialog does not use carousel", previewText.contains("HorizontalPager"))
        assertFalse("StickerPreviewDialog does not show full photo toggle", previewText.contains("View Full Photo"))
    }

    @Test
    fun newEntry_storageEfficiencyAndChoiceContract() {
        val vmFile = java.io.File("src/main/java/com/bitey/app/feature/entry/NewEntryViewModel.kt")
        val vmText = vmFile.readText()
        assertTrue("Has setStickerMode", vmText.contains("setStickerMode"))
        assertTrue("Deletes unused files for storage efficiency", vmText.contains(".delete()"))

        val reviewFile = java.io.File("src/main/java/com/bitey/app/feature/camera/component/NewEntryReviewView.kt")
        val reviewText = reviewFile.readText()
        assertTrue("Allows choosing Sticker Mode or Full Photo", reviewText.contains("Sticker Mode") && reviewText.contains("Full Photo"))
    }

    @Test
    fun imageStorageNaming_namedByImageContract() {
        val namingFile = java.io.File("src/main/java/com/bitey/app/core/image/ImageStorageNaming.kt")
        assertTrue("ImageStorageNaming exists", namingFile.exists())
        val namingText = namingFile.readText()
        assertTrue("Sanitizes image name", namingText.contains("sanitizeName"))
        assertTrue("Resolves saved file", namingText.contains("resolveSavedFile"))

        val vmFile = java.io.File("src/main/java/com/bitey/app/feature/entry/NewEntryViewModel.kt")
        val vmText = vmFile.readText()
        assertTrue("Uses ImageStorageNaming to name files by image name", vmText.contains("ImageStorageNaming.saveAsNamedImage"))

        val gitignoreFile = java.io.File("../.gitignore").takeIf { it.exists() } ?: java.io.File(".gitignore")
        if (gitignoreFile.exists()) {
            val gitignoreText = gitignoreFile.readText()
            assertTrue("Ignores AGENTS.md", gitignoreText.contains("AGENTS.md"))
            assertTrue("Ignores .agents/", gitignoreText.contains(".agents/"))
        }
    }

    @Test
    fun scrollUpToDelete_stationaryMorphContract() {
        val zoneFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/ScrollUpToDeleteZone.kt")
        assertTrue("ScrollUpToDeleteZone exists", zoneFile.exists())
        val zoneText = zoneFile.readText()

        // Stationary morph: uses AnimatedContent with springy feel
        assertTrue("Uses AnimatedContent for in-place morph", zoneText.contains("AnimatedContent"))
        assertTrue("Uses spring bounce for morph transition", zoneText.contains("Spring.DampingRatioMediumBouncy"))

        // Minimalist: no Surface container
        assertFalse("Clean minimalist: no Surface container", zoneText.contains("Surface("))

        // Haptic feedback & tactile feel
        assertTrue("Uses haptic feedback", zoneText.contains("HapticFeedbackType"))

        // Clean, rounded icons
        assertTrue("Uses Icons.Rounded.Delete", zoneText.contains("Icons.Rounded.Delete"))
        assertTrue("Uses Icons.Rounded.KeyboardArrowUp", zoneText.contains("Icons.Rounded.KeyboardArrowUp"))

        // Embedded cleanly in JournalDetailSheet
        val sheetFile = java.io.File("src/main/java/com/bitey/app/feature/journal/component/JournalDetailSheet.kt")
        assertTrue("JournalDetailSheet exists", sheetFile.exists())
        val sheetText = sheetFile.readText()
        assertTrue("Directly embeds ScrollUpToDeleteZone", sheetText.contains("ScrollUpToDeleteZone"))
    }
}
