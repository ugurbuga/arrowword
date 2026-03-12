package com.ugurbuga.arrowword.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ugurbuga.arrowword.ui.game.GameRoute
import com.ugurbuga.arrowword.ui.levelselect.LevelSelectRoute

@Composable
fun ArrowwordApp() {
    val context = LocalContext.current
    val appGraph = remember(context) { AppGraph(context.applicationContext) }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LevelSelectRoute.route,
    ) {
        composable(LevelSelectRoute.route) {
            LevelSelectRoute(
                puzzleRepository = appGraph.puzzleRepository,
                progressRepository = appGraph.progressRepository,
                onNavigateToGame = { levelId ->
                    navController.navigate(GameRoute.createRoute(levelId))
                },
            )
        }

        composable(
            route = GameRoute.route,
            arguments = listOf(
                navArgument(GameRoute.ARG_LEVEL_ID) { type = NavType.StringType },
            ),
        ) {
            val levelId = requireNotNull(it.arguments?.getString(GameRoute.ARG_LEVEL_ID))
            GameRoute(
                levelId = levelId,
                puzzleRepository = appGraph.puzzleRepository,
                progressRepository = appGraph.progressRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLevel = { nextLevelId ->
                    navController.navigate(GameRoute.createRoute(nextLevelId)) {
                        popUpTo(GameRoute.createRoute(levelId)) { inclusive = true }
                    }
                },
            )
        }
    }
}
