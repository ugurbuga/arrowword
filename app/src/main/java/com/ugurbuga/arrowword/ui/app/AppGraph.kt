package com.ugurbuga.arrowword.ui.app

import android.content.Context
import com.ugurbuga.arrowword.data.repository.AssetPuzzleRepository
import com.ugurbuga.arrowword.data.repository.DataStoreProgressRepository
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository

class AppGraph(
    context: Context,
) {
    val puzzleRepository: PuzzleRepository = AssetPuzzleRepository(context = context)
    val progressRepository: ProgressRepository = DataStoreProgressRepository(context = context)
}
