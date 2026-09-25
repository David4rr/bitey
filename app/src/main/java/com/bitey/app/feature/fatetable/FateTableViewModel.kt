package com.bitey.app.feature.fatetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.feature.fatetable.component.FateTableUtils
import com.bitey.app.feature.fatetable.component.SpinResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random
private const val MAX_WHEEL_CANDIDATES = 8

@HiltViewModel
class FateTableViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FateSourceFilter.ALL)
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    private val _shuffleSeed = MutableStateFlow(0)
    private val _manualCandidates = MutableStateFlow<List<PlateEntryWithTags>?>(null)
    private val _showMenuPicker = MutableStateFlow(false)
    private val _isSpinning = MutableStateFlow(false)
    private val _winningEntry = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _showWinningDialog = MutableStateFlow(false)
    private val _currentRotationAngle = MutableStateFlow(0f)

    private data class FilterParams(
        val filter: FateSourceFilter,
        val tagId: Long?,
        val seed: Int,
        val manual: List<PlateEntryWithTags>?,
        val showPicker: Boolean
    )

    private val combinedParams = combine(
        _selectedFilter, _selectedTagId, _shuffleSeed, _manualCandidates, _showMenuPicker
    ) { filter, tagId, seed, manual, picker ->
        FilterParams(filter, tagId, seed, manual, picker)
    }
    val uiState: StateFlow<FateTableUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        tagDao.getAllTags(),
        combinedParams,
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

        val candidates = if (params.manual != null) {
            params.manual
        } else {
            val filtered = FateTableUtils.filterCandidates(allEntries, params.filter, params.tagId)
            if (filtered.size > MAX_WHEEL_CANDIDATES) {
                filtered.shuffled(Random(params.seed)).take(MAX_WHEEL_CANDIDATES)
            } else {
                filtered
            }
        }

        FateTableUiState(
            candidates = candidates,
            allEntries = allEntries,
            totalEntriesCount = allEntries.size,
            selectedFilter = params.filter,
            selectedTagId = params.tagId,
            availableTags = allTags,
            isSpinning = isSpinning,
            winningEntry = winningEntry,
            showWinningDialog = showWinningDialog,
            currentRotationAngle = currentRotationAngle,
            isCustomSelection = params.manual != null,
            showMenuPicker = params.showPicker
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), FateTableUiState())

    fun selectFilter(filter: FateSourceFilter) {
        if (_isSpinning.value) return
        _selectedFilter.value = filter
        _manualCandidates.value = null
    }

    fun selectTag(tagId: Long?) {
        if (_isSpinning.value) return
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
        _manualCandidates.value = null
    }

    fun shuffleCandidates() {
        if (_isSpinning.value) return
        if (_manualCandidates.value != null) {
            _manualCandidates.update { it?.shuffled() }
        } else {
            _shuffleSeed.update { it + 1 }
        }
    }

    fun openMenuPicker(open: Boolean) {
        if (_isSpinning.value) return
        _showMenuPicker.value = open
    }

    fun addCandidate(entry: PlateEntryWithTags) {
        if (_isSpinning.value) return
        val current = _manualCandidates.value ?: uiState.value.candidates
        if (current.any { it.entry.id == entry.entry.id } || current.size >= 12) return
        _manualCandidates.value = current + entry
    }

    fun removeCandidate(entryId: Long) {
        if (_isSpinning.value) return
        val current = _manualCandidates.value ?: uiState.value.candidates
        _manualCandidates.value = current.filterNot { it.entry.id == entryId }
    }

    fun toggleCandidate(entry: PlateEntryWithTags) {
        if (_isSpinning.value) return
        val current = _manualCandidates.value ?: uiState.value.candidates
        if (current.any { it.entry.id == entry.entry.id }) {
            removeCandidate(entry.entry.id)
        } else {
            addCandidate(entry)
        }
    }

    fun resetToAutoCandidates() {
        if (_isSpinning.value) return
        _manualCandidates.value = null
    }

    fun calculateSpinTarget(candidates: List<PlateEntryWithTags>): SpinResult? =
        FateTableUtils.calculateSpinTarget(candidates, _currentRotationAngle.value, _isSpinning.value)

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
