package com.ugurbuga.arrowword.ui.data

import com.ugurbuga.arrowword.generated.resources.Res
import com.ugurbuga.arrowword.domain.model.WordEntry
import com.ugurbuga.arrowword.domain.repository.WordRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ResourceWordRepository(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : WordRepository {

    @Serializable
    private data class WordEntryDto(
        @SerialName("value") val value: String,
        @SerialName("text") val text: String,
    )

    override suspend fun getWords(): List<WordEntry> {
        val bytes = Res.readBytes("files/words/dataset_tr.json")
        val content = bytes.decodeToString()
        val dtos = json.decodeFromString<List<WordEntryDto>>(content)
        return dtos.map { WordEntry(value = it.value, text = it.text) }
    }
}
