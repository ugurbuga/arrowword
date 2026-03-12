package com.ugurbuga.arrowword.ui.levelselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ugurbuga.arrowword.domain.model.Difficulty
import com.ugurbuga.arrowword.domain.model.LevelSummary
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LevelSelectViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectUiState())
    val uiState: StateFlow<LevelSelectUiState> = _uiState.asStateFlow()

    private val _viewEvents = MutableSharedFlow<LevelSelectViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    init {
        loadLevels()
    }

    fun onAction(action: LevelSelectAction) {
        when (action) {
            is LevelSelectAction.SelectDifficulty -> {
                _uiState.value = _uiState.value.copy(selectedDifficulty = action.difficulty)
                loadLevels()
            }

            is LevelSelectAction.SelectLevel -> {
                val item = _uiState.value.levels.firstOrNull { it.id == action.levelId } ?: return
                if (!item.isEnabled) return
                viewModelScope.launch {
                    _viewEvents.emit(LevelSelectViewEvent.NavigateToGame(levelId = action.levelId))
                }
            }
        }
    }

    private fun loadLevels() {
        val difficulty = _uiState.value.selectedDifficulty
        viewModelScope.launch {
            val summaries = puzzleRepository.getLevelSummaries(difficulty)
            val items = summaries
                .sortedBy { it.order }
                .map { summary ->
                    val completed = progressRepository.getProgress(summary.id)?.isCompleted ?: false
                    LevelSelectItem(
                        id = summary.id,
                        order = summary.order,
                        isCompleted = completed,
                        isEnabled = true,
                    )
                }

            val enabledItems = computeEnabledItems(items)
            _uiState.value = _uiState.value.copy(levels = enabledItems)
        }
    }

    private fun computeEnabledItems(items: List<LevelSelectItem>): List<LevelSelectItem> {
        if (items.isEmpty()) return items

        val result = ArrayList<LevelSelectItem>(items.size)
        for (i in items.indices) {
            val prevCompleted = if (i == 0) true else items[i - 1].isCompleted
            val enabled = items[i].isCompleted || prevCompleted
            result += items[i].copy(isEnabled = enabled)
        }
        return result
    }

    companion object {
        fun factory(
            puzzleRepository: PuzzleRepository,
            progressRepository: ProgressRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LevelSelectViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return LevelSelectViewModel(
                            puzzleRepository = puzzleRepository,
                            progressRepository = progressRepository,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

data class LevelSelectUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val levels: List<LevelSelectItem> = emptyList(),
)

data class LevelSelectItem(
    val id: String,
    val order: Int,
    val isCompleted: Boolean,
    val isEnabled: Boolean,
)

sealed interface LevelSelectAction {
    data class SelectDifficulty(val difficulty: Difficulty) : LevelSelectAction
    data class SelectLevel(val levelId: String) : LevelSelectAction
}

sealed interface LevelSelectViewEvent {
    data class NavigateToGame(val levelId: String) : LevelSelectViewEvent
}
