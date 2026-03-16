package com.ugurbuga.arrowword.ui.levelselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugurbuga.arrowword.domain.model.GeneratedPuzzleSize
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import com.ugurbuga.arrowword.domain.repository.WordRepository
import com.ugurbuga.arrowword.domain.usecase.GenerateCrossPuzzleUseCase
import com.ugurbuga.arrowword.domain.usecase.GenerateRandomLevelIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class LevelSelectViewModel(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
    private val generatedPuzzleRepository: GeneratedPuzzleRepository,
    private val generateRandomLevelIdUseCase: GenerateRandomLevelIdUseCase = GenerateRandomLevelIdUseCase(),
    private val generateCrossPuzzleUseCase: GenerateCrossPuzzleUseCase = GenerateCrossPuzzleUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectUiState())
    val uiState: StateFlow<LevelSelectUiState> = _uiState.asStateFlow()

    private val _viewEvents = MutableSharedFlow<LevelSelectViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    init {
        loadStats()
    }

    fun onAction(action: LevelSelectAction) {
        when (action) {
            is LevelSelectAction.SelectSize -> {
                _uiState.value = _uiState.value.copy(selectedSize = action.size)
            }

            LevelSelectAction.Play -> {
                viewModelScope.launch {
                    if (_uiState.value.isLoading) return@launch

                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val size = _uiState.value.selectedSize
                    val levelId = generateRandomLevelIdUseCase.generate()

                    try {
                        val words = withContext(Dispatchers.Default) { wordRepository.getWords() }

                        val puzzle = withContext(Dispatchers.Default) {
                            generateCrossPuzzleUseCase.generate(
                                levelId = levelId,
                                width = size.width,
                                height = size.height,
                                words = words,
                            )
                        }

                        generatedPuzzleRepository.put(puzzle)
                        _viewEvents.emit(LevelSelectViewEvent.NavigateToGame(levelId = levelId))
                    } finally {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val completed = progressRepository.getGeneratedCompletedCount()

            _uiState.value = _uiState.value.copy(
                completedCount = completed,
            )
        }
    }

}

data class LevelSelectUiState(
    val completedCount: Int = 0,
    val isLoading: Boolean = false,
    val selectedSize: GeneratedPuzzleSize = GeneratedPuzzleSize.MEDIUM,
)

sealed interface LevelSelectAction {
    data class SelectSize(val size: GeneratedPuzzleSize) : LevelSelectAction
    object Play : LevelSelectAction
}

sealed interface LevelSelectViewEvent {
    data class NavigateToGame(val levelId: String) : LevelSelectViewEvent
}
