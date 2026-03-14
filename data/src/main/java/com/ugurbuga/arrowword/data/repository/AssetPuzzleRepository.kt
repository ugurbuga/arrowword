package com.ugurbuga.arrowword.data.repository

import android.content.Context
import com.ugurbuga.arrowword.domain.model.Cell
import com.ugurbuga.arrowword.domain.model.Direction
import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import org.json.JSONArray
import org.json.JSONObject

class AssetPuzzleRepository(
    private val context: Context,
) : PuzzleRepository {

    override suspend fun getPuzzle(levelId: String): Puzzle {
        val indexJson = readAssetAsString("puzzles/index.json")
        val root = JSONObject(indexJson)
        val levels = root.getJSONArray("levels")

        val puzzleFile = findPuzzleFile(levels, levelId)
        val puzzleJson = readAssetAsString("puzzles/$puzzleFile")
        val puzzle = JSONObject(puzzleJson)

        val width = puzzle.getInt("width")
        val height = puzzle.getInt("height")
        val cellsJson = puzzle.getJSONArray("cells")

        val cells = parseCells(cellsJson)

        return Puzzle(
            id = levelId,
            width = width,
            height = height,
            cells = cells,
        )
    }

    private fun findPuzzleFile(levels: JSONArray, levelId: String): String {
        for (i in 0 until levels.length()) {
            val level = levels.getJSONObject(i)
            if (level.getString("id") == levelId) {
                return level.getString("puzzleFile")
            }
        }
        error("Puzzle file not found for levelId=$levelId")
    }

    private fun parseCells(cellsJson: JSONArray): List<Cell> {
        val result = ArrayList<Cell>(cellsJson.length())

        for (i in 0 until cellsJson.length()) {
            val cellObj = cellsJson.getJSONObject(i)
            result += parseCell(cellObj)
        }

        return result
    }

    private fun parseCell(cellObj: JSONObject): Cell {
        return when (val type = cellObj.getString("type")) {
            "block" -> Cell.Block
            "letter" -> Cell.Letter(solution = cellObj.getString("solution").first())
            "clue" -> Cell.Clue(
                direction = Direction.valueOf(cellObj.getString("direction")),
                answerLength = cellObj.getInt("answerLength"),
                text = cellObj.getString("text"),
            )

            else -> error("Unknown cell type=$type")
        }
    }

    private fun readAssetAsString(assetPath: String): String {
        return context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }
}
