package com.ugurbuga.arrowword.data.repository

import android.content.Context
import com.ugurbuga.arrowword.domain.model.WordEntry
import com.ugurbuga.arrowword.domain.repository.WordRepository
import org.json.JSONArray

class AssetWordRepository(
    private val context: Context,
) : WordRepository {

    override suspend fun getWords(): List<WordEntry> {
        val json = readAssetAsString("words/dataset_tr.json")
        val array = JSONArray(json)
        val result = ArrayList<WordEntry>(array.length())

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += WordEntry(
                value = obj.getString("value"),
                text = obj.getString("text"),
            )
        }

        return result
    }

    private fun readAssetAsString(assetPath: String): String {
        return context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }
}
