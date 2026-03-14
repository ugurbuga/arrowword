package com.ugurbuga.arrowword.data.repository

import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository

class CompositePuzzleRepository(
    private val assetPuzzleRepository: PuzzleRepository,
    private val generatedPuzzleRepository: GeneratedPuzzleRepository,
) : PuzzleRepository {

    override suspend fun getPuzzle(levelId: String): Puzzle {
        val generated = generatedPuzzleRepository.get(levelId)
        if (generated != null) return generated

        return assetPuzzleRepository.getPuzzle(levelId)
    }
}
