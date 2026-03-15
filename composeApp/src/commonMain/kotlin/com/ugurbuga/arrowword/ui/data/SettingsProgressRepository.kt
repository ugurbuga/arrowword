package com.ugurbuga.arrowword.ui.data

import com.russhwolf.settings.Settings
import com.ugurbuga.arrowword.domain.model.LevelProgress
import com.ugurbuga.arrowword.domain.repository.ProgressRepository

class SettingsProgressRepository(
    private val settings: Settings,
) : ProgressRepository {

    override suspend fun getProgress(levelId: String): LevelProgress? {
        val value = settings.getStringOrNull(progressKey(levelId)) ?: return null
        val splitIndex = value.indexOf('|')
        if (splitIndex <= 0) return null

        val entries = value.substring(0, splitIndex)
        val completed = value.substring(splitIndex + 1).toBooleanStrictOrNull() ?: false

        return LevelProgress(
            levelId = levelId,
            entries = entries,
            isCompleted = completed,
        )
    }

    override suspend fun saveProgress(progress: LevelProgress) {
        settings.putString(progressKey(progress.levelId), serialize(progress))
    }

    override suspend fun clearProgress(levelId: String) {
        settings.remove(progressKey(levelId))
    }

    override suspend fun getGeneratedCompletedCount(): Int {
        return settings.getInt(generatedCompletedCountKey(), 0)
    }

    override suspend fun incrementGeneratedCompletedCount() {
        val cur = settings.getInt(generatedCompletedCountKey(), 0)
        settings.putInt(generatedCompletedCountKey(), cur + 1)
    }

    private fun serialize(progress: LevelProgress): String {
        return progress.entries + "|" + progress.isCompleted
    }

    private fun progressKey(levelId: String): String {
        return "progress_" + levelId
    }

    private fun generatedCompletedCountKey(): String {
        return "generated_completed_count"
    }
}
