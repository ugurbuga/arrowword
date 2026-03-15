package com.ugurbuga.arrowword.domain.repository

import com.ugurbuga.arrowword.domain.model.Puzzle

interface GeneratedPuzzleRepository {
    fun put(puzzle: Puzzle)

    fun get(levelId: String): Puzzle?
}
