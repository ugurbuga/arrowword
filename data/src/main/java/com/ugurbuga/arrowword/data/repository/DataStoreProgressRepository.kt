package com.ugurbuga.arrowword.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ugurbuga.arrowword.domain.model.LevelProgress
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first

private val Context.progressDataStore by preferencesDataStore(name = "level_progress")

class DataStoreProgressRepository(
    private val context: Context,
) : ProgressRepository {

    override suspend fun getProgress(levelId: String): LevelProgress? {
        val preferences = context.progressDataStore.data.first()
        val value = preferences[progressKey(levelId)] ?: return null

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
        context.progressDataStore.edit { prefs ->
            prefs[progressKey(progress.levelId)] = serialize(progress)
        }
    }

    override suspend fun clearProgress(levelId: String) {
        context.progressDataStore.edit { prefs ->
            prefs.remove(progressKey(levelId))
        }
    }

    private fun serialize(progress: LevelProgress): String {
        return progress.entries + "|" + progress.isCompleted
    }

    private fun progressKey(levelId: String): Preferences.Key<String> {
        return stringPreferencesKey("progress_" + levelId)
    }
}
