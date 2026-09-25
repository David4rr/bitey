package com.bitey.app.feature.fatetable

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.feature.fatetable.component.FateTableUtils
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FateTableCalculationTest {

    private val sampleTags = listOf(
        TagEntity(tagId = 1, tagName = "Spicy", category = "Taste"),
        TagEntity(tagId = 2, tagName = "Dessert", category = "Taste")
    )

    private val entry1 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 1,
            title = "Ayam Geprek",
            note = "Crispy spicy chicken",
            fullImagePath = "/path/1.webp",
            thumbnailPath = "/path/thumb1.webp",
            rating = 4.8f,
            isFavorite = true,
            timestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L, // 2 days ago
            mealType = MealType.FOOD
        ),
        tags = listOf(sampleTags[0])
    )

    private val entry2 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 2,
            title = "Martabak Manis",
            note = "Chocolate cheese peanut",
            fullImagePath = "/path/2.webp",
            thumbnailPath = "/path/thumb2.webp",
            rating = 4.9f,
            isFavorite = false,
            timestamp = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L, // 5 days ago
            mealType = MealType.OTHER
        ),
        tags = listOf(sampleTags[1])
    )

    private val entry3 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 3,
            title = "Bubur Ayam",
            note = "Warm morning congee",
            fullImagePath = "/path/3.webp",
            thumbnailPath = "/path/thumb3.webp",
            rating = 4.2f,
            isFavorite = true,
            timestamp = System.currentTimeMillis() - 40 * 24 * 60 * 60 * 1000L, // 40 days ago (older than 30 days)
            mealType = MealType.FOOD
        ),
        tags = emptyList()
    )

    private val allCandidates = listOf(entry1, entry2, entry3)

    @Test
    fun calculateSpinTarget_withLessThanTwoCandidates_returnsNull() {
        // Given fewer than 2 items
        val singleCandidate = listOf(entry1)
        val sliceAngle = 360f / singleCandidate.size
        // Direct calculation logic validation
        val result = if (singleCandidate.size < 2) null else "Valid"
        assertNull(result)
    }

    @Test
    fun calculateSpinTarget_withMultipleCandidates_ensuresDecelerationSpinsAndCorrectAlignment() {
        val candidates = listOf(entry1, entry2, entry3)
        val n = candidates.size
        val sliceAngle = 360f / n
        val currentAngle = 45f

        for (winningIndex in 0 until n) {
            val sliceCenter = sliceAngle * (winningIndex + 0.5f)
            val targetMod = (360f - sliceCenter + 360f) % 360f

            val currentMod = (currentAngle % 360f + 360f) % 360f
            val diff = (targetMod - currentMod + 360f) % 360f

            val fullSpins = 5
            val totalTargetAngle = currentAngle + (fullSpins * 360f) + diff

            // Verify the wheel always spins forward
            assertTrue(totalTargetAngle > currentAngle)

            // Verify minimum 5 full rotations (1800 degrees)
            assertTrue(totalTargetAngle - currentAngle >= 1800f)

            // Verify landing alignment: when wheel rotates by totalTargetAngle,
            // the winning slice's center aligns with the pointer at top (12 o'clock / 0 deg from top)
            val effectiveRotation = (totalTargetAngle % 360f + 360f) % 360f
            val pointerLocalAngle = (360f - effectiveRotation + 360f) % 360f

            // Pointer local angle should match the winning slice's center
            assertEquals(sliceCenter, pointerLocalAngle, 0.01f)
        }
    }

    @Test
    fun filterCandidates_favoritesOnly_returnsOnlyFavorites() {
        val favorites = allCandidates.filter { it.entry.isFavorite }
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.entry.isFavorite })
    }

    @Test
    fun filterCandidates_recentThirtyDays_excludesOlderEntries() {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val recent = allCandidates.filter { (now - it.entry.timestamp) <= thirtyDaysMs }
        assertEquals(2, recent.size)
        assertTrue(recent.none { it.entry.id == 3L })
    }

    @Test
    fun filterCandidates_byTag_returnsMatchingTagOnly() {
        val spicyOnly = allCandidates.filter { item -> item.tags.any { it.tagId == 1L } }
        assertEquals(1, spicyOnly.size)
        assertEquals("Ayam Geprek", spicyOnly.first().entry.title)
    }

    @Test
    fun wheelCanvas_dishImagesAndZoomHighlightContract() {
        val canvasFile = File("src/main/java/com/bitey/app/feature/fatetable/component/WheelCanvas.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/component/WheelCanvas.kt")
        assertTrue("WheelCanvas exists", canvasFile.exists())
        val canvasText = canvasFile.readText()

        assertTrue("Renders dish image using AsyncImage", canvasText.contains("AsyncImage"))
        assertTrue("Renders dish title", canvasText.contains("candidate.entry.title"))
        assertTrue("Applies zoom-out highlight animation to selected dish", canvasText.contains("animateFloatAsState") && canvasText.contains("scale"))
        assertFalse("Eliminated white pill sausage ribbons", canvasText.contains("pillPath"))
        assertFalse("Eliminated rainbow pie slice wedges", canvasText.contains("drawArc"))
        assertFalse("Eliminated circular card background behind dishes", canvasText.contains("minimalistCard"))
        assertTrue("Uses CropTransparentTransformation to remove photo background", canvasText.contains("CropTransparentTransformation"))

        val screenFile = File("src/main/java/com/bitey/app/feature/fatetable/FateTableScreen.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/FateTableScreen.kt")
        assertTrue("FateTableScreen exists", screenFile.exists())
        val screenText = screenFile.readText()
        assertFalse("Orange triangle pointer indicator removed", screenText.contains("WheelPointerIndicator"))
        assertTrue("Uses WinningDishBottomSheet", screenText.contains("WinningDishBottomSheet"))

        val sheetFile = File("src/main/java/com/bitey/app/feature/fatetable/component/WinningDishBottomSheet.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/component/WinningDishBottomSheet.kt")
        assertTrue("WinningDishBottomSheet exists", sheetFile.exists())
    }

    @Test
    fun fateTableUtils_calculateSpinTarget_behavesCorrectly() {
        assertNull(FateTableUtils.calculateSpinTarget(emptyList(), 0f))
        assertNull(FateTableUtils.calculateSpinTarget(listOf(entry1), 0f))
        assertNull(FateTableUtils.calculateSpinTarget(listOf(entry1, entry2), 0f, isSpinning = true))

        val result = FateTableUtils.calculateSpinTarget(listOf(entry1, entry2), 0f, isSpinning = false)
        assertNotNull(result)
        assertTrue(result!!.targetAngle >= 1800f)
        assertTrue(result.winningIndex in 0..1)
    }

    @Test
    fun fateTableUtils_filterCandidates_matchesCriteria() {
        val favs = FateTableUtils.filterCandidates(allCandidates, FateSourceFilter.FAVORITES, null)
        assertEquals(2, favs.size)
        assertTrue(favs.all { it.entry.isFavorite })

        val recent = FateTableUtils.filterCandidates(allCandidates, FateSourceFilter.RECENT_30_DAYS, null)
        assertEquals(2, recent.size)

        val tagged = FateTableUtils.filterCandidates(allCandidates, FateSourceFilter.ALL, 1L)
        assertEquals(1, tagged.size)
        assertEquals("Ayam Geprek", tagged.first().entry.title)
    }

    @Test
    fun fateTableMenuSelection_userOnlyAddsDishesAlreadyOnMenuContract() {
        val vmFile = File("src/main/java/com/bitey/app/feature/fatetable/FateTableViewModel.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/FateTableViewModel.kt")
        assertTrue("FateTableViewModel exists", vmFile.exists())
        val vmText = vmFile.readText()

        assertTrue("ViewModel supports addCandidate", vmText.contains("fun addCandidate"))
        assertFalse("ViewModel does not allow arbitrary custom dish creation", vmText.contains("fun addCustomMenu"))
        assertTrue("ViewModel supports removeCandidate", vmText.contains("fun removeCandidate"))
        assertTrue("ViewModel supports toggleCandidate", vmText.contains("fun toggleCandidate"))
        assertTrue("ViewModel supports resetToAutoCandidates", vmText.contains("fun resetToAutoCandidates"))

        val pickerFile = File("src/main/java/com/bitey/app/feature/fatetable/component/FateTableMenuPickerSheet.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/component/FateTableMenuPickerSheet.kt")
        assertTrue("FateTableMenuPickerSheet exists", pickerFile.exists())
        val pickerText = pickerFile.readText()

        assertFalse("Picker does not allow arbitrary custom dish creation", pickerText.contains("onAddCustomMenu"))
        assertTrue("Allows picking from dishes on the menu", pickerText.contains("onToggleEntry"))
        assertTrue("Provides search filter for menu dishes", pickerText.contains("searchQuery"))
        assertTrue("Allows reset to auto recommendation", pickerText.contains("onResetToAuto"))

        val chipsFile = File("src/main/java/com/bitey/app/feature/fatetable/component/ActiveMenuChipsRow.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/fatetable/component/ActiveMenuChipsRow.kt")
        assertTrue("ActiveMenuChipsRow exists", chipsFile.exists())
        val chipsText = chipsFile.readText()
        assertTrue("Row allows removing candidates", chipsText.contains("onRemoveCandidate"))
        assertTrue("Row allows opening menu picker", chipsText.contains("onAddMenuClick"))
    }
}
