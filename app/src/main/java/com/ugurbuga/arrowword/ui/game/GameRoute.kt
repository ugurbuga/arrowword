package com.ugurbuga.arrowword.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import com.ugurbuga.arrowword.ui.navigation.Routes

object GameRoute {
    const val ARG_LEVEL_ID = "levelId"
    const val route = Routes.GAME + "/{" + ARG_LEVEL_ID + "}"

    fun createRoute(levelId: String): String = Routes.GAME + "/" + levelId
}

@Composable
fun GameRoute(
    levelId: String,
    puzzleRepository: PuzzleRepository,
    progressRepository: ProgressRepository,
    onNavigateBack: () -> Unit,
    onNavigateToLevel: (String) -> Unit,
) {
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.factory(
            levelId = levelId,
            puzzleRepository = puzzleRepository,
            progressRepository = progressRepository,
        ),
    )

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect { event ->
            when (event) {
                is GameViewEvent.NavigateToLevel -> onNavigateToLevel(event.levelId)
            }
        }
    }

    GameScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}
