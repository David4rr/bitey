package com.bitey.app.feature.fatetable.component

import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.feature.fatetable.FateSourceFilter
import kotlin.random.Random

data class SpinResult(
    val targetAngle: Float,
    val winningIndex: Int,
    val winningEntry: PlateEntryWithTags
)

object FateTableUtils {
    const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000

    fun filterCandidates(
        allEntries: List<PlateEntryWithTags>,
        filter: FateSourceFilter,
        tagId: Long?,
        now: Long = System.currentTimeMillis()
    ): List<PlateEntryWithTags> {
        return allEntries.filter { item ->
            val matchesFilter = when (filter) {
                FateSourceFilter.ALL -> true
                FateSourceFilter.FAVORITES -> item.entry.isFavorite
                FateSourceFilter.RECENT_30_DAYS -> (now - item.entry.timestamp) <= THIRTY_DAYS_MS
            }
            val matchesTag = tagId == null || item.tags.any { it.tagId == tagId }
            matchesFilter && matchesTag
        }
    }

    fun calculateSpinTarget(
        candidates: List<PlateEntryWithTags>,
        currentAngle: Float,
        isSpinning: Boolean = false
    ): SpinResult? {
        if (candidates.size < 2 || isSpinning) return null
        val n = candidates.size
        val sliceAngle = 360f / n
        val winningIndex = Random.nextInt(n)
        val sliceCenter = sliceAngle * (winningIndex + 0.5f)
        val targetMod = (360f - sliceCenter + 360f) % 360f
        val currentMod = (currentAngle % 360f + 360f) % 360f
        val diff = (targetMod - currentMod + 360f) % 360f
        val fullSpins = Random.nextInt(5, 8)
        val totalTargetAngle = currentAngle + (fullSpins * 360f) + diff
        return SpinResult(totalTargetAngle, winningIndex, candidates[winningIndex])
    }
}
