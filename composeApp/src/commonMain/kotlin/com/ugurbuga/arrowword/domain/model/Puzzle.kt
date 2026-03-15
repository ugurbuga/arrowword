package com.ugurbuga.arrowword.domain.model

data class Puzzle(
    val id: String,
    val width: Int,
    val height: Int,
    val cells: List<Cell>,
)

sealed interface Cell {
    data object Block : Cell

    data class Letter(
        val solution: Char,
    ) : Cell

    data class Clue(
        val direction: Direction,
        val answerLength: Int,
        val text: String,
    ) : Cell
}
