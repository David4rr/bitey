package com.bitey.app.feature.journal

import androidx.compose.ui.geometry.Rect
import com.bitey.app.feature.journal.component.SharedDishTransition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DishDetailAnimationTest {

    private fun resolveFile(path: String): File {
        val f1 = File(path)
        if (f1.exists()) return f1
        val f2 = File("app", path)
        if (f2.exists()) return f2
        return f1
    }

    @Test
    fun journalFiles_existAndUnder200Lines() {
        val files = listOf(
            "src/main/java/com/bitey/app/feature/journal/JournalScreen.kt",
            "src/main/java/com/bitey/app/feature/journal/component/JournalDetailSheet.kt",
            "src/main/java/com/bitey/app/feature/journal/component/JournalFeedView.kt",
            "src/main/java/com/bitey/app/feature/journal/component/JournalDetailBottomSheet.kt",
            "src/main/java/com/bitey/app/feature/journal/component/StickerPreviewOverlay.kt",
            "src/main/java/com/bitey/app/feature/journal/component/StickerPreviewDialog.kt",
            "src/main/java/com/bitey/app/feature/journal/component/SharedDishAnimation.kt",
            "src/main/java/com/bitey/app/feature/journal/component/HistoryGridSingleCard.kt",
            "src/main/java/com/bitey/app/feature/journal/component/StickerCard.kt",
            "src/main/java/com/bitey/app/core/ui/SharedTransitionScopes.kt"
        )

        files.forEach { path ->
            val file = resolveFile(path)
            assertTrue("File ${file.name} must exist", file.exists())
            val lineCount = file.readLines().size
            assertTrue("File ${file.name} must be <= 200 lines, was $lineCount", lineCount <= 200)
        }
    }

    @Test
    fun journalScreen_usesInHierarchyBottomSheetAndSharedTransitionLayout() {
        val screenFile = resolveFile("src/main/java/com/bitey/app/feature/journal/JournalScreen.kt")
        assertTrue(screenFile.exists())
        val text = screenFile.readText()
        assertTrue("Uses SharedTransitionLayout for connected animations", text.contains("SharedTransitionLayout("))
        assertTrue("Uses in-hierarchy JournalDetailBottomSheet", text.contains("JournalDetailBottomSheet("))
        assertTrue("Hosts in-hierarchy StickerPreviewOverlay", text.contains("StickerPreviewOverlay("))
    }

    @Test
    fun journalDetailContent_usesSharedTransitionElements() {
        val detailFile = resolveFile("src/main/java/com/bitey/app/feature/journal/component/JournalDetailContent.kt")
        assertTrue(detailFile.exists())
        val text = detailFile.readText()
        assertTrue("Applies dishSharedElement to hero sticker", text.contains("dishSharedElement("))
        assertTrue("Uses SharedDishTransition", text.contains("SharedDishTransition.create"))
        assertTrue("Tracks progress with spring physics", text.contains("rememberSharedDishTransitionProgress"))
        assertTrue("Applies sharedDishTransform", text.contains("sharedDishTransform"))
    }

    @Test
    fun stickerPreviewOverlay_usesSharedTransitionElements() {
        val previewFile = resolveFile("src/main/java/com/bitey/app/feature/journal/component/StickerPreviewOverlay.kt")
        assertTrue(previewFile.exists())
        val text = previewFile.readText()
        assertTrue("Applies dishSharedElement to preview image", text.contains("dishSharedElement(dishKey)"))
        assertTrue("Accepts onDismiss callback", text.contains("onDismiss: () -> Unit"))
    }

    @Test
    fun sharedDishTransition_mathCalculations() {
        val source = Rect(100f, 200f, 200f, 300f) // center: (150, 250), width: 100
        val target = Rect(200f, 400f, 400f, 600f) // center: (300, 500), width: 200

        val transition = SharedDishTransition.create(source, target)
        assertNotNull(transition)
        assertEquals(-150f, transition!!.deltaX, 0.001f)
        assertEquals(-250f, transition.deltaY, 0.001f)
        assertEquals(0.5f, transition.initialScale, 0.001f)

        // At progress 0 (start)
        assertEquals(-150f, transition.calculateTranslationX(0f), 0.001f)
        assertEquals(-250f, transition.calculateTranslationY(0f), 0.001f)
        assertEquals(0.5f, transition.calculateScale(0f), 0.001f)

        // At progress 1 (target reached)
        assertEquals(0f, transition.calculateTranslationX(1f), 0.001f)
        assertEquals(0f, transition.calculateTranslationY(1f), 0.001f)
        assertEquals(1.0f, transition.calculateScale(1f), 0.001f)
    }
}
