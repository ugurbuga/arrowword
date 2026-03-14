package com.ugurbuga.arrowword.domain.model

enum class GeneratedPuzzleSize(
    val width: Int,
    val height: Int,
) {
    SMALL(width = 5, height = 7),
    MEDIUM(width = 7, height = 9),
    LARGE(width = 9, height = 11),
    XL(width = 11, height = 13),
    XXL(width = 13, height = 15),
}
