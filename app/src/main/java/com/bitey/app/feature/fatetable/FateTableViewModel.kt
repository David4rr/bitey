package com.bitey.app.feature.fatetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

private const val MAX_WHEEL_CANDIDATES = 8
private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000

@HiltViewModel
class FateTableViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FateSourceFilter.ALL)
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    private val _shuffleSeed = MutableStateFlow(0)
    private val _isSpinning = MutableStateFlow(false)
    private val _winningEntry = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _showWinningDialog = MutableStateFlow(false)
    private val _currentRotationAngle = MutableStateFlow(0f)

    private data class FilterParams(
        val filter: FateSourceFilter,
        val tagId: Long?,
        val shuffleSeed: Int
    )

    private val filterParams = combine(
        _selectedFilter,
        _selectedTagId,
        _shuffleSeed
    ) { filter, tagId, seed ->
        FilterParams(filter, tagId, seed)
    }

    val uiState: StateFlow<FateTableUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        tagDao.getAllTags(),
        filterParams,
        _isSpinning,
        _winningEntry,
        _showWinningDialog,
        _currentRotationAngle
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allEntries = args[0] as List<PlateEntryWithTags>
        @Suppress("UNCHECKED_CAST")
        val allTags = args[1] as List<TagEntity>
        val params = args[2] as FilterParams
        val isSpinning = args[3] as Boolean
        val winningEntry = args[4] as PlateEntryWithTags?
        val showWinningDialog = args[5] as Boolean
        val currentRotationAngle = args[6] as Float

        val now = System.currentTimeMillis()
        val filtered = allEntries.filter { item ->
            val matchesFilter = when (params.filter) {
                FateSourceFilter.ALL -> true
                FateSourceFilter.FAVORITES -> item.entry.isFavorite
                FateSourceFilter.RECENT_30_DAYS -> (now - item.entry.timestamp) <= THIRTY_DAYS_MS
            }
            val matchesTag = params.tagId == null || item.tags.any { it.tagId == params.tagId }
            matchesFilter && matchesTag
        }

        // Shuffle if candidates exceed max limit, or pick top items
        val candidates = if (filtered.size > MAX_WHEEL_CANDIDATES) {
            val random = Random(params.shuffleSeed)
            filtered.shuffled(random).take(MAX_WHEEL_CANDIDATES)
        } else {
            filtered
        }

        FateTableUiState(
            candidates = candidates,
            totalEntriesCount = allEntries.size,
            selectedFilter = params.filter,
            selectedTagId = params.tagId,
            availableTags = allTags,
            isSpinning = isSpinning,
            winningEntry = winningEntry,
            showWinningDialog = showWinningDialog,
            currentRotationAngle = currentRotationAngle
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FateTableUiState()
    )

    fun selectFilter(filter: FateSourceFilter) {
        if (_isSpinning.value) return
        _selectedFilter.value = filter
    }

    fun selectTag(tagId: Long?) {
        if (_isSpinning.value) return
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
    }

    fun shuffleCandidates() {
        if (_isSpinning.value) return
        _shuffleSeed.update { it + 1 }
    }

    /**
     * Calculates the target rotation angle and winning item index.
     * The pointer is at 12 o'clock (top center).
     */
    fun calculateSpinTarget(candidates: List<PlateEntryWithTags>): SpinResult? {
        if (candidates.size < 2 || _isSpinning.value) return null

        val n = candidates.size
        val sliceAngle = 360f / n
        val winningIndex = Random.nextInt(n)

        // Center of winning slice from slice start
        val sliceCenter = sliceAngle * (winningIndex + 0.5f)
        val targetMod = (360f - sliceCenter + 360f) % 360f

        val currentAngle = _currentRotationAngle.value
        val currentMod = (currentAngle % 360f + 360f) % 360f
        val diff = (targetMod - currentMod + 360f) % 360f

        // 5 to 7 full rotations for deceleration duration
        val fullSpins = Random.nextInt(5, 8)
        val totalTargetAngle = currentAngle + (fullSpins * 360f) + diff

        return SpinResult(
            targetAngle = totalTargetAngle,
            winningIndex = winningIndex,
            winningEntry = candidates[winningIndex]
        )
    }

    fun onSpinStarted() {
        _isSpinning.value = true
        _showWinningDialog.value = false
        _winningEntry.value = null
    }

    fun onSpinCompleted(finalAngle: Float, winningEntry: PlateEntryWithTags) {
        _currentRotationAngle.value = finalAngle
        _isSpinning.value = false
        _winningEntry.value = winningEntry
        _showWinningDialog.value = true
    }

    fun dismissWinningDialog() {
        _showWinningDialog.value = false
    }
}

data class SpinResult(
    val targetAngle: Float,
    val winningIndex: Int,
    val winningEntry: PlateEntryWithTags
)
