package com.ugurbuga.arrowword.ui.data

import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository

class GeneratedOnlyPuzzleRepository(
    private val generatedPuzzleRepository: GeneratedPuzzleRepository,
) : PuzzleRepository {
    override suspend fun getPuzzle(levelId: String): Puzzle {
        return requireNotNull(generatedPuzzleRepository.get(levelId))
    }
}
