package com.ugurbuga.arrowword.ui.levelselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
            LevelSelectAction.RandomPuzzle -> {
                viewModelScope.launch {
                    if (_uiState.value.isLoading) return@launch

                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val size = GeneratedPuzzleSize.entries.random()
                    val levelId = generateRandomLevelIdUseCase.generate()

                    try {
                        val words = withContext(Dispatchers.IO) { wordRepository.getWords() }

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

    companion object {
        fun factory(
            progressRepository: ProgressRepository,
            wordRepository: WordRepository,
            generatedPuzzleRepository: GeneratedPuzzleRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LevelSelectViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return LevelSelectViewModel(
                            progressRepository = progressRepository,
                            wordRepository = wordRepository,
                            generatedPuzzleRepository = generatedPuzzleRepository,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

data class LevelSelectUiState(
    val completedCount: Int = 0,
    val isLoading: Boolean = false,
)

sealed interface LevelSelectAction {
    object RandomPuzzle : LevelSelectAction
}

sealed interface LevelSelectViewEvent {
    data class NavigateToGame(val levelId: String) : LevelSelectViewEvent
}
