package com.ugurbuga.arrowword.domain.model

data class WordEntry(
    val value: String,
    val text: String,
) {
    val answerLength: Int
        get() = value.length
}
