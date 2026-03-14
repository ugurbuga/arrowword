package com.ugurbuga.arrowword.domain.repository
 
import com.ugurbuga.arrowword.domain.model.Puzzle
 
interface PuzzleRepository {
    suspend fun getPuzzle(levelId: String): Puzzle
}
