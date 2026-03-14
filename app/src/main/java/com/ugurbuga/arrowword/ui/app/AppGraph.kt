package com.ugurbuga.arrowword.ui.app

import android.content.Context
import com.ugurbuga.arrowword.data.repository.AssetPuzzleRepository
import com.ugurbuga.arrowword.data.repository.AssetWordRepository
import com.ugurbuga.arrowword.data.repository.CompositePuzzleRepository
import com.ugurbuga.arrowword.data.repository.DataStoreProgressRepository
import com.ugurbuga.arrowword.data.repository.GeneratedPuzzleStore
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import com.ugurbuga.arrowword.domain.repository.WordRepository

class AppGraph(
    context: Context,
) {
    private val generatedPuzzleStore: GeneratedPuzzleRepository = GeneratedPuzzleStore()

    val wordRepository: WordRepository = AssetWordRepository(context = context)

    val puzzleRepository: PuzzleRepository = CompositePuzzleRepository(
        assetPuzzleRepository = AssetPuzzleRepository(context = context),
        generatedPuzzleRepository = generatedPuzzleStore,
    )

    val generatedPuzzleRepository: GeneratedPuzzleRepository = generatedPuzzleStore
    val progressRepository: ProgressRepository = DataStoreProgressRepository(context = context)
}
