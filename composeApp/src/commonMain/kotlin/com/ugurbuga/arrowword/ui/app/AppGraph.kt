package com.ugurbuga.arrowword.ui.app

import com.russhwolf.settings.Settings
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import com.ugurbuga.arrowword.domain.repository.WordRepository
import com.ugurbuga.arrowword.ui.data.GeneratedPuzzleStore
import com.ugurbuga.arrowword.ui.data.GeneratedOnlyPuzzleRepository
import com.ugurbuga.arrowword.ui.data.SettingsProgressRepository
import com.ugurbuga.arrowword.ui.data.ResourceWordRepository

class ArrowwordDependencies(
    settings: Settings = Settings(),
) {
    val wordRepository: WordRepository = ResourceWordRepository()

    val generatedPuzzleRepository: GeneratedPuzzleRepository = GeneratedPuzzleStore()

    val puzzleRepository: PuzzleRepository = GeneratedOnlyPuzzleRepository(
        generatedPuzzleRepository = generatedPuzzleRepository,
    )

    val progressRepository: ProgressRepository = SettingsProgressRepository(
        settings = settings,
    )
}
