package com.ugurbuga.arrowword.ui.levelselect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository

@Composable
fun LevelSelectRoute(
    puzzleRepository: PuzzleRepository,
    progressRepository: ProgressRepository,
    onNavigateToGame: (String) -> Unit,
) {
    val viewModel: LevelSelectViewModel = viewModel(
        factory = LevelSelectViewModel.factory(
            puzzleRepository = puzzleRepository,
            progressRepository = progressRepository,
        ),
    )

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect { event ->
            when (event) {
                is LevelSelectViewEvent.NavigateToGame -> onNavigateToGame(event.levelId)
            }
        }
    }

    LevelSelectScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
