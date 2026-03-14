package com.ugurbuga.arrowword.domain.repository

import com.ugurbuga.arrowword.domain.model.WordEntry

interface WordRepository {
    suspend fun getWords(): List<WordEntry>
}
