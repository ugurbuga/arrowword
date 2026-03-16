package com.ugurbuga.arrowword.domain.usecase

import com.ugurbuga.arrowword.domain.model.Cell
import com.ugurbuga.arrowword.domain.model.Direction
import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.model.WordEntry
import kotlin.random.Random

class GenerateCrossPuzzleUseCase {

    fun generate(
        levelId: String,
        width: Int,
        height: Int,
        words: List<WordEntry>,
        random: Random = Random.Default,
    ): Puzzle {
        val maxLenRight = width - 1
        val maxLenDown = height - 1

        val candidatesH = words.filter { it.answerLength in 2..maxLenRight }
        val candidatesV = words.filter { it.answerLength in 2..maxLenDown }

        val candidatesHByLen: Map<Int, List<WordEntry>> = candidatesH.groupBy { it.answerLength }
        val candidatesVByLen: Map<Int, List<WordEntry>> = candidatesV.groupBy { it.answerLength }

        if (candidatesH.isEmpty()) error("No horizontal candidates")
        if (candidatesV.isEmpty()) error("No vertical candidates")

        data class Placement(
            val direction: Direction,
            val clueRow: Int,
            val clueCol: Int,
            val word: WordEntry,
        )

        fun idx(r: Int, c: Int) = r * width + c

        fun charAt(word: WordEntry, i: Int): Char = word.value[i]

        fun canPlace(
            placement: Placement,
            clueCells: MutableMap<Int, Cell.Clue>,
            letterCells: MutableMap<Int, Char>,
        ): Pair<Boolean, Int> {
            val clueIndex = idx(placement.clueRow, placement.clueCol)
            if (clueIndex in clueCells || clueIndex in letterCells) return false to 0

            fun hasLetter(r: Int, c: Int): Boolean {
                if (r !in 0 until height || c !in 0 until width) return false
                return idx(r, c) in letterCells
            }

            fun hasClue(r: Int, c: Int): Boolean {
                if (r !in 0 until height || c !in 0 until width) return false
                return idx(r, c) in clueCells
            }

            fun isOccupied(r: Int, c: Int): Boolean {
                return hasLetter(r, c) || hasClue(r, c)
            }

            val length = placement.word.answerLength

            val beforeRow = when (placement.direction) {
                Direction.RIGHT -> placement.clueRow
                Direction.DOWN -> placement.clueRow - 1
            }
            val beforeCol = when (placement.direction) {
                Direction.RIGHT -> placement.clueCol - 1
                Direction.DOWN -> placement.clueCol
            }
            if (beforeRow in 0 until height && beforeCol in 0 until width) {
                if (isOccupied(beforeRow, beforeCol)) return false to 0
            }

            val afterRow = when (placement.direction) {
                Direction.RIGHT -> placement.clueRow
                Direction.DOWN -> placement.clueRow + length + 1
            }
            val afterCol = when (placement.direction) {
                Direction.RIGHT -> placement.clueCol + length + 1
                Direction.DOWN -> placement.clueCol
            }
            if (afterRow in 0 until height && afterCol in 0 until width) {
                if (isOccupied(afterRow, afterCol)) return false to 0
            }

            var newLetters = 0

            for (i in 1..length) {
                val r = when (placement.direction) {
                    Direction.RIGHT -> placement.clueRow
                    Direction.DOWN -> placement.clueRow + i
                }
                val c = when (placement.direction) {
                    Direction.RIGHT -> placement.clueCol + i
                    Direction.DOWN -> placement.clueCol
                }

                if (r !in 0 until height || c !in 0 until width) return false to 0
                val targetIndex = idx(r, c)
                if (targetIndex in clueCells) return false to 0

                val newChar = charAt(placement.word, i - 1)
                val existing = letterCells[targetIndex]
                if (existing != null && existing != newChar) return false to 0

                val isCrossing = existing != null
                if (!isCrossing) {
                    when (placement.direction) {
                        Direction.RIGHT -> {
                            if (hasLetter(r - 1, c) || hasLetter(r + 1, c)) return false to 0
                            if (hasClue(r - 1, c) || hasClue(r + 1, c)) return false to 0
                        }

                        Direction.DOWN -> {
                            if (hasLetter(r, c - 1) || hasLetter(r, c + 1)) return false to 0
                            if (hasClue(r, c - 1) || hasClue(r, c + 1)) return false to 0
                        }
                    }
                }

                if (existing == null) newLetters++
            }

            return true to newLetters
        }

        fun place(
            placement: Placement,
            clueCells: MutableMap<Int, Cell.Clue>,
            letterCells: MutableMap<Int, Char>,
        ) {
            val clueIndex = idx(placement.clueRow, placement.clueCol)
            clueCells[clueIndex] = Cell.Clue(
                direction = placement.direction,
                answerLength = placement.word.answerLength,
                text = placement.word.text,
            )

            val length = placement.word.answerLength
            for (i in 1..length) {
                val r = when (placement.direction) {
                    Direction.RIGHT -> placement.clueRow
                    Direction.DOWN -> placement.clueRow + i
                }
                val c = when (placement.direction) {
                    Direction.RIGHT -> placement.clueCol + i
                    Direction.DOWN -> placement.clueCol
                }
                val targetIndex = idx(r, c)
                letterCells[targetIndex] = charAt(placement.word, i - 1)
            }
        }

        fun seedIntersecting(
            clueCells: MutableMap<Int, Cell.Clue>,
            letterCells: MutableMap<Int, Char>,
        ): Boolean {
            repeat(800) {
                val h = candidatesH.randomOrNull(random) ?: return false
                val v = candidatesV.randomOrNull(random) ?: return false

                val hWord = h.value
                val vWord = v.value

                val colLetter = random.nextInt(1, minOf(h.answerLength, width - 1) + 1)
                val rowLetter = random.nextInt(1, minOf(v.answerLength, height - 1) + 1)

                val hChar = hWord[colLetter - 1]
                val vChar = vWord[rowLetter - 1]
                if (hChar != vChar) return@repeat

                val row = rowLetter
                val col = colLetter

                val hClue = Placement(
                    direction = Direction.RIGHT,
                    clueRow = row,
                    clueCol = col - colLetter,
                    word = h,
                )

                val vClue = Placement(
                    direction = Direction.DOWN,
                    clueRow = row - rowLetter,
                    clueCol = col,
                    word = v,
                )

                val (hOk, _) = canPlace(hClue, clueCells, letterCells)
                if (!hOk) return@repeat
                place(hClue, clueCells, letterCells)

                val (vOk, _) = canPlace(vClue, clueCells, letterCells)
                if (!vOk) {
                    clueCells.clear()
                    letterCells.clear()
                    return@repeat
                }
                place(vClue, clueCells, letterCells)
                return true
            }
            return false
        }

        fun generateCrossingCandidates(
            r: Int,
            c: Int,
            existingChar: Char,
            direction: Direction,
        ): List<Placement> {
            val maxLen = when (direction) {
                Direction.RIGHT -> maxLenRight
                Direction.DOWN -> maxLenDown
            }
            val sourceByLen = when (direction) {
                Direction.RIGHT -> candidatesHByLen
                Direction.DOWN -> candidatesVByLen
            }

            val result = ArrayList<Placement>(64)
            if (sourceByLen.isEmpty()) return emptyList()

            repeat(80) {
                val len = random.nextInt(2, maxLen + 1)
                val lenPool = sourceByLen[len] ?: return@repeat
                val w = lenPool.randomOrNull(random) ?: return@repeat
                val value = w.value

                val indices = value.indices.filter { value[it] == existingChar }
                if (indices.isEmpty()) return@repeat

                val letterPos = indices[random.nextInt(indices.size)]
                val clueRow = when (direction) {
                    Direction.RIGHT -> r
                    Direction.DOWN -> r - letterPos - 1
                }
                val clueCol = when (direction) {
                    Direction.RIGHT -> c - letterPos - 1
                    Direction.DOWN -> c
                }

                val endRow = when (direction) {
                    Direction.RIGHT -> clueRow
                    Direction.DOWN -> clueRow + w.answerLength
                }
                val endCol = when (direction) {
                    Direction.RIGHT -> clueCol + w.answerLength
                    Direction.DOWN -> clueCol
                }

                if (clueRow !in 0 until height || clueCol !in 0 until width) return@repeat
                if (endRow !in 0 until height || endCol !in 0 until width) return@repeat

                result += Placement(
                    direction = direction,
                    clueRow = clueRow,
                    clueCol = clueCol,
                    word = w,
                )
            }

            return result
        }

        fun seedNonIntersecting(
            clueCells: MutableMap<Int, Cell.Clue>,
            letterCells: MutableMap<Int, Char>,
        ): Boolean {
            var best: Placement? = null
            var bestScore = 0

            repeat(600) {
                val dir = if (random.nextBoolean()) Direction.RIGHT else Direction.DOWN
                val w = when (dir) {
                    Direction.RIGHT -> candidatesH.randomOrNull(random)
                    Direction.DOWN -> candidatesV.randomOrNull(random)
                } ?: return@repeat

                val clueRow = random.nextInt(height)
                val clueCol = random.nextInt(width)

                val endRow = when (dir) {
                    Direction.RIGHT -> clueRow
                    Direction.DOWN -> clueRow + w.answerLength
                }
                val endCol = when (dir) {
                    Direction.RIGHT -> clueCol + w.answerLength
                    Direction.DOWN -> clueCol
                }
                if (endRow !in 0 until height || endCol !in 0 until width) return@repeat

                val p = Placement(
                    direction = dir,
                    clueRow = clueRow,
                    clueCol = clueCol,
                    word = w,
                )
                val (ok, newLetters) = canPlace(p, clueCells, letterCells)
                if (!ok) return@repeat
                if (newLetters > bestScore) {
                    bestScore = newLetters
                    best = p
                    if (bestScore >= w.answerLength) return@repeat
                }
            }

            val chosen = best ?: return false
            if (bestScore <= 0) return false
            place(chosen, clueCells, letterCells)
            return true
        }

        fun greedyGapFill(
            clueCells: MutableMap<Int, Cell.Clue>,
            letterCells: MutableMap<Int, Char>,
            maxPlacements: Int,
            sampleAttempts: Int,
            maxSampledEmptyCells: Int,
        ) {
            fun isOccupied(index: Int): Boolean = index in clueCells || index in letterCells

            fun maxAnswerLengthFrom(
                clueRow: Int,
                clueCol: Int,
                direction: Direction,
            ): Int {
                var len = 0
                while (true) {
                    val next = len + 1
                    val r = when (direction) {
                        Direction.RIGHT -> clueRow
                        Direction.DOWN -> clueRow + next
                    }
                    val c = when (direction) {
                        Direction.RIGHT -> clueCol + next
                        Direction.DOWN -> clueCol
                    }
                    if (r !in 0 until height || c !in 0 until width) return len
                    if (isOccupied(idx(r, c))) return len
                    len = next
                }
            }

            fun chooseWordFor(dir: Direction, maxLen: Int): WordEntry? {
                if (maxLen < 2) return null
                val sourceByLen = when (dir) {
                    Direction.RIGHT -> candidatesHByLen
                    Direction.DOWN -> candidatesVByLen
                }

                for (len in maxLen downTo 2) {
                    val pool = sourceByLen[len] ?: continue
                    if (pool.isNotEmpty()) return pool.randomOrNull(random)
                }
                return null
            }

            repeat(maxPlacements) {
                var best: Placement? = null
                var bestNewLetters = 0

                val emptyIndices = ArrayList<Int>(maxSampledEmptyCells)
                repeat(sampleAttempts) {
                    if (emptyIndices.size >= maxSampledEmptyCells) return@repeat
                    val candidate = random.nextInt(width * height)
                    if (isOccupied(candidate)) return@repeat
                    emptyIndices.add(candidate)
                }

                for (emptyIndex in emptyIndices) {
                    val r = emptyIndex / width
                    val c = emptyIndex % width

                    for (dir in listOf(Direction.RIGHT, Direction.DOWN)) {
                        val maxLen = maxAnswerLengthFrom(r, c, dir)
                        val w = chooseWordFor(dir, maxLen) ?: continue
                        val p = Placement(
                            direction = dir,
                            clueRow = r,
                            clueCol = c,
                            word = w,
                        )
                        val (ok, newLetters) = canPlace(p, clueCells, letterCells)
                        if (!ok) continue
                        if (newLetters > bestNewLetters) {
                            bestNewLetters = newLetters
                            best = p
                            if (bestNewLetters >= w.answerLength) break
                        }
                    }
                }

                val chosen = best ?: return
                if (bestNewLetters <= 0) return
                place(chosen, clueCells, letterCells)
            }
        }

        val bestCells = MutableList<Cell>(width * height) { Cell.Block }
        var bestBlocks = width * height
        var bestFill = 0

        val area = width * height

        val attemptCount = when {
            area <= 70 -> 45
            area <= 120 -> 32
            else -> 24
        }

        val growIterations = when {
            area <= 70 -> 220
            area <= 120 -> 180
            else -> 140
        }

        val gapFillPlacements = when {
            area <= 70 -> 220
            area <= 120 -> 160
            else -> 110
        }

        val gapFillSampleAttempts = when {
            area <= 70 -> 520
            area <= 120 -> 360
            else -> 260
        }

        val gapFillMaxSampledEmptyCells = when {
            area <= 70 -> 240
            area <= 120 -> 180
            else -> 140
        }

        val targetWords = when {
            width * height <= 40 -> 6
            width * height <= 70 -> 10
            else -> 18
        }

        repeat(attemptCount) {
            val clueCells = HashMap<Int, Cell.Clue>()
            val letterCells = HashMap<Int, Char>()

            if (!seedIntersecting(clueCells, letterCells)) return@repeat

            var placedWords = clueCells.size
            var noProgress = 0

            repeat(growIterations) {
                if (placedWords >= targetWords) return@repeat

                var progressed = false

                if (letterCells.isNotEmpty()) {
                    val letterIndex = letterCells.keys.elementAt(random.nextInt(letterCells.size))
                    val r = letterIndex / width
                    val c = letterIndex % width
                    val ch = letterCells[letterIndex] ?: return@repeat

                    val dir = if (random.nextBoolean()) Direction.RIGHT else Direction.DOWN
                    val candidates = generateCrossingCandidates(r, c, ch, dir)

                    var best: Placement? = null
                    var bestScore = 0

                    for (p in candidates) {
                        val (ok, newLetters) = canPlace(p, clueCells, letterCells)
                        if (!ok) continue
                        if (newLetters > bestScore) {
                            bestScore = newLetters
                            best = p
                            if (bestScore >= p.word.answerLength) break
                        }
                    }

                    if (best != null && bestScore > 0) {
                        place(best, clueCells, letterCells)
                        placedWords++
                        progressed = true
                    }
                }

                if (!progressed && (letterCells.isEmpty() || noProgress >= 12)) {
                    if (seedNonIntersecting(clueCells, letterCells)) {
                        placedWords++
                        progressed = true
                    }
                }

                if (progressed) {
                    noProgress = 0
                } else {
                    noProgress++
                    if (noProgress >= 30) return@repeat
                }
            }

            greedyGapFill(
                clueCells = clueCells,
                letterCells = letterCells,
                maxPlacements = gapFillPlacements,
                sampleAttempts = gapFillSampleAttempts,
                maxSampledEmptyCells = gapFillMaxSampledEmptyCells,
            )

            val fill = clueCells.size + letterCells.size
            val blocks = width * height - fill
            if (blocks < bestBlocks || (blocks == bestBlocks && fill > bestFill)) {
                bestBlocks = blocks
                bestFill = fill
                for (i in bestCells.indices) bestCells[i] = Cell.Block
                for ((i, clue) in clueCells) bestCells[i] = clue
                for ((i, ch) in letterCells) bestCells[i] = Cell.Letter(solution = ch)

                if (bestBlocks == 0) {
                    return Puzzle(
                        id = levelId,
                        width = width,
                        height = height,
                        cells = bestCells,
                    )
                }
            }
        }

        if (bestFill == 0) error("Unable to generate puzzle")

        return Puzzle(
            id = levelId,
            width = width,
            height = height,
            cells = bestCells,
        )
    }
}

private fun <T> List<T>.randomOrNull(random: Random): T? {
    if (isEmpty()) return null
    return this[random.nextInt(size)]
}
