package com.ugurbuga.arrowword.domain.model

data class LevelProgress(
    val levelId: String,
    val entries: String,
    val isCompleted: Boolean,
)
