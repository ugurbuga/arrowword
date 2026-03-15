package com.ugurbuga.arrowword.ui.data

import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.GeneratedPuzzleRepository

class GeneratedPuzzleStore : GeneratedPuzzleRepository {
    private val map = LinkedHashMap<String, Puzzle>()

    override fun put(puzzle: Puzzle) {
        map[puzzle.id] = puzzle
    }

    override fun get(levelId: String): Puzzle? {
        return map[levelId]
    }
}
