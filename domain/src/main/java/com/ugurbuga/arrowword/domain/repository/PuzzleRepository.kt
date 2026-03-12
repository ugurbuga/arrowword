package com.ugurbuga.arrowword.domain.repository

import com.ugurbuga.arrowword.domain.model.Difficulty
import com.ugurbuga.arrowword.domain.model.LevelSummary
import com.ugurbuga.arrowword.domain.model.Puzzle

interface PuzzleRepository {
    suspend fun getLevelSummaries(difficulty: Difficulty): List<LevelSummary>

    suspend fun getPuzzle(levelId: String): Puzzle
}
