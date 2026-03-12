package com.ugurbuga.arrowword.domain.repository

import com.ugurbuga.arrowword.domain.model.LevelProgress

interface ProgressRepository {
    suspend fun getProgress(levelId: String): LevelProgress?

    suspend fun saveProgress(progress: LevelProgress)

    suspend fun clearProgress(levelId: String)
}
