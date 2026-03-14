package com.ugurbuga.arrowword.data.repository

import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository

class GeneratedPuzzleStore : GeneratedPuzzleRepository {
    private val puzzles = LinkedHashMap<String, Puzzle>()

    override fun put(puzzle: Puzzle) {
        puzzles[puzzle.id] = puzzle
    }

    override fun get(levelId: String): Puzzle? {
        return puzzles[levelId]
    }
}
