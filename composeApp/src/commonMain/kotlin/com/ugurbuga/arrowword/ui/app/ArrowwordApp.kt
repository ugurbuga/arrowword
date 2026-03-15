package com.ugurbuga.arrowword.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ugurbuga.arrowword.ui.game.GameRoute
import com.ugurbuga.arrowword.ui.levelselect.LevelSelectRoute

@Composable
fun ArrowwordApp(
    dependencies: ArrowwordDependencies = remember { ArrowwordDependencies() },
) {
    var screen by remember { mutableStateOf<ArrowwordScreen>(ArrowwordScreen.LevelSelect) }

    when (val s = screen) {
        ArrowwordScreen.LevelSelect -> {
            LevelSelectRoute(
                progressRepository = dependencies.progressRepository,
                wordRepository = dependencies.wordRepository,
                generatedPuzzleRepository = dependencies.generatedPuzzleRepository,
                onNavigateToGame = { levelId ->
                    screen = ArrowwordScreen.Game(levelId = levelId)
                },
            )
        }

        is ArrowwordScreen.Game -> {
            GameRoute(
                levelId = s.levelId,
                puzzleRepository = dependencies.puzzleRepository,
                progressRepository = dependencies.progressRepository,
                wordRepository = dependencies.wordRepository,
                generatedPuzzleRepository = dependencies.generatedPuzzleRepository,
                onNavigateBack = { screen = ArrowwordScreen.LevelSelect },
                onNavigateToLevel = { nextLevelId ->
                    screen = ArrowwordScreen.Game(levelId = nextLevelId)
                },
            )
        }
    }
}

sealed interface ArrowwordScreen {
    data object LevelSelect : ArrowwordScreen
    data class Game(val levelId: String) : ArrowwordScreen
}
