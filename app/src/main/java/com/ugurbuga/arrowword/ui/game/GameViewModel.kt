package com.ugurbuga.arrowword.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ugurbuga.arrowword.domain.model.Cell
import com.ugurbuga.arrowword.domain.model.Difficulty
import com.ugurbuga.arrowword.domain.model.Direction
import com.ugurbuga.arrowword.domain.model.LevelProgress
import com.ugurbuga.arrowword.domain.model.Puzzle
import com.ugurbuga.arrowword.domain.repository.PuzzleRepository
import com.ugurbuga.arrowword.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val levelId: String,
    private val puzzleRepository: PuzzleRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _viewEvents = MutableSharedFlow<GameViewEvent>()
    val viewEvents = _viewEvents.asSharedFlow()

    init {
        loadPuzzle()
    }

    fun onAction(action: GameAction) {
        when (action) {
            is GameAction.SelectCell -> onSelectCell(action.index)
            is GameAction.InputChar -> onInputChar(action.char)
            GameAction.Backspace -> onBackspace()
            GameAction.NextLevel -> onNextLevel()
        }
    }

    private fun normalizePuzzle(puzzle: Puzzle): Puzzle {
        val width = puzzle.width
        val height = puzzle.height

        fun stepForward(index: Int, direction: Direction): Int? {
            val row = index / width
            val col = index % width
            return when (direction) {
                Direction.RIGHT -> if (col + 1 < width) index + 1 else null
                Direction.DOWN -> if (row + 1 < height) index + width else null
            }
        }

        val allowedLetterIndices = HashSet<Int>()
        for (i in puzzle.cells.indices) {
            val cell = puzzle.cells[i]
            if (cell is Cell.Clue) {
                var cur = i
                repeat(cell.answerLength) {
                    cur = stepForward(cur, cell.direction) ?: return@repeat
                    allowedLetterIndices += cur
                }
            }
        }

        val normalizedCells = puzzle.cells.mapIndexed { idx, cell ->
            when (cell) {
                is Cell.Letter -> if (idx in allowedLetterIndices) cell else Cell.Block
                else -> cell
            }
        }

        return puzzle.copy(cells = normalizedCells)
    }

    private fun loadPuzzle() {
        viewModelScope.launch {
            val rawPuzzle = puzzleRepository.getPuzzle(levelId)
            val puzzle = normalizePuzzle(rawPuzzle)
            val initialEntries = CharArray(puzzle.cells.size) { '\u0000' }

            val progress = progressRepository.getProgress(levelId)
            val restoredEntries = progress?.entries?.let { deserializeEntries(it, puzzle.cells.size) }
            val entries = restoredEntries ?: initialEntries

            val completed = progress?.isCompleted ?: isSolved(puzzle, entries)

            val solvedInfo = computeSolvedInfo(puzzle = puzzle, entries = entries)

            _uiState.value = _uiState.value.copy(
                levelId = levelId,
                puzzle = puzzle,
                entries = entries,
                isCompleted = completed,
                solvedClueIndices = solvedInfo.solvedClueIndices,
                solvedLetterIndices = solvedInfo.solvedLetterIndices,
                selectedIndex = firstLetterIndex(puzzle) ?: 0,
                selectedDirection = Direction.RIGHT,
            ).let { state ->
                state.copy(activeWordIndices = computeActiveWordIndices(state))
            }

            saveProgressIfReady()
        }
    }

    private fun onSelectCell(index: Int) {
        val puzzle = _uiState.value.puzzle ?: return
        if (index !in puzzle.cells.indices) return

        if (puzzle.cells[index] !is Cell.Letter) return

        val currentState = _uiState.value

        val rightClue = findCoveringClue(
            puzzle = puzzle,
            selectedIndex = index,
            direction = Direction.RIGHT,
        )
        val downClue = findCoveringClue(
            puzzle = puzzle,
            selectedIndex = index,
            direction = Direction.DOWN,
        )

        val availableDirections = buildList {
            if (rightClue != null) add(Direction.RIGHT)
            if (downClue != null) add(Direction.DOWN)
        }

        if (availableDirections.isEmpty()) return

        fun nextAvailableDirection(current: Direction): Direction {
            if (availableDirections.isEmpty()) return current
            val idx = availableDirections.indexOf(current)
            return if (idx < 0) availableDirections.first() else availableDirections[(idx + 1) % availableDirections.size]
        }

        fun isFirstLetterOf(clue: IndexedClue): Boolean {
            val indices = computeAnswerIndices(
                puzzle = puzzle,
                clueIndex = clue.index,
                direction = clue.cell.direction,
                answerLength = clue.cell.answerLength,
            )
            return indices.firstOrNull() == index
        }

        val preferredDirection = when {
            availableDirections.size == 1 -> availableDirections.first()

            currentState.selectedIndex == index -> {
                nextAvailableDirection(currentState.selectedDirection)
            }

            availableDirections.size >= 2 -> {
                val firstRight = rightClue?.let { isFirstLetterOf(it) } ?: false
                val firstDown = downClue?.let { isFirstLetterOf(it) } ?: false

                when {
                    firstDown && !firstRight -> Direction.DOWN
                    firstRight && !firstDown -> Direction.RIGHT
                    currentState.selectedDirection in availableDirections -> currentState.selectedDirection
                    else -> availableDirections.first()
                }
            }

            else -> currentState.selectedDirection
        }

        _uiState.value = currentState.copy(
            selectedIndex = index,
            selectedDirection = preferredDirection,
        ).let { it.copy(activeWordIndices = computeActiveWordIndices(it)) }
    }

    private fun onInputChar(char: Char) {
        val puzzle = _uiState.value.puzzle ?: return
        val index = _uiState.value.selectedIndex ?: return
        if (index !in puzzle.cells.indices) return
        if (puzzle.cells[index] !is Cell.Letter) return

        val newEntries = _uiState.value.entries.copyOf()
        newEntries[index] = char.uppercaseChar()

        val solved = isSolved(puzzle, newEntries)

        val solvedInfo = computeSolvedInfo(puzzle = puzzle, entries = newEntries)

        val nextIndex = findNextIndex(
            state = _uiState.value,
            fromIndex = index,
        )

        val nextSelected = nextIndex ?: index

        _uiState.value = _uiState.value.copy(
            entries = newEntries,
            isCompleted = solved,
            solvedClueIndices = solvedInfo.solvedClueIndices,
            solvedLetterIndices = solvedInfo.solvedLetterIndices,
            selectedIndex = nextSelected,
        ).let { it.copy(activeWordIndices = computeActiveWordIndices(it)) }

        saveProgressIfReady()
    }

    private fun onNextLevel() {
        viewModelScope.launch {
            val nextId = findNextLevelId(levelId) ?: return@launch
            _viewEvents.emit(GameViewEvent.NavigateToLevel(levelId = nextId))
        }
    }

    private suspend fun findNextLevelId(currentLevelId: String): String? {
        val difficulty = when {
            currentLevelId.startsWith("easy-") -> Difficulty.EASY
            currentLevelId.startsWith("medium-") -> Difficulty.MEDIUM
            currentLevelId.startsWith("hard-") -> Difficulty.HARD
            else -> return null
        }

        val levels = puzzleRepository.getLevelSummaries(difficulty)
        val current = levels.firstOrNull { it.id == currentLevelId } ?: return null
        val next = levels.firstOrNull { it.order == current.order + 1 }
        return next?.id
    }

    private fun onBackspace() {
        val puzzle = _uiState.value.puzzle ?: return
        val index = _uiState.value.selectedIndex ?: return
        if (index !in puzzle.cells.indices) return
        if (puzzle.cells[index] !is Cell.Letter) return

        val current = _uiState.value.entries
        val newEntries = current.copyOf()
        if (newEntries[index] != '\u0000') {
            newEntries[index] = '\u0000'
            val solvedInfo = computeSolvedInfo(puzzle = puzzle, entries = newEntries)
            _uiState.value = _uiState.value.copy(
                entries = newEntries,
                isCompleted = false,
                solvedClueIndices = solvedInfo.solvedClueIndices,
                solvedLetterIndices = solvedInfo.solvedLetterIndices,
            )
        } else {
            val previousIndex = findPreviousIndex(
                state = _uiState.value,
                fromIndex = index,
            )

            if (previousIndex != null) {
                newEntries[previousIndex] = '\u0000'
                val solvedInfo = computeSolvedInfo(puzzle = puzzle, entries = newEntries)
                _uiState.value = _uiState.value.copy(
                    entries = newEntries,
                    selectedIndex = previousIndex,
                    isCompleted = false,
                    solvedClueIndices = solvedInfo.solvedClueIndices,
                    solvedLetterIndices = solvedInfo.solvedLetterIndices,
                ).let { it.copy(activeWordIndices = computeActiveWordIndices(it)) }
            }
        }

        saveProgressIfReady()
    }

    private fun saveProgressIfReady() {
        val puzzle = _uiState.value.puzzle ?: return
        val entries = _uiState.value.entries
        if (entries.size != puzzle.cells.size) return

        viewModelScope.launch {
            progressRepository.saveProgress(
                LevelProgress(
                    levelId = levelId,
                    entries = serializeEntries(entries),
                    isCompleted = _uiState.value.isCompleted,
                )
            )
        }
    }

    private fun computeActiveWordIndices(state: GameUiState): List<Int> {
        val puzzle = state.puzzle ?: return emptyList()
        val selectedIndex = state.selectedIndex ?: return emptyList()
        if (selectedIndex !in puzzle.cells.indices) return emptyList()
        if (puzzle.cells[selectedIndex] !is Cell.Letter) return emptyList()

        val clue = findCoveringClue(
            puzzle = puzzle,
            selectedIndex = selectedIndex,
            direction = state.selectedDirection,
        ) ?: return emptyList()

        return computeAnswerIndices(
            puzzle = puzzle,
            clueIndex = clue.index,
            direction = clue.cell.direction,
            answerLength = clue.cell.answerLength,
        )
    }

    private fun findNextIndex(state: GameUiState, fromIndex: Int): Int? {
        val puzzle = state.puzzle ?: return null
        val active = state.activeWordIndices
        val pos = active.indexOf(fromIndex)
        if (pos < 0) return null
        return active.getOrNull(pos + 1)
            ?.takeIf { it in puzzle.cells.indices && puzzle.cells[it] is Cell.Letter }
    }

    private fun findPreviousIndex(state: GameUiState, fromIndex: Int): Int? {
        val puzzle = state.puzzle ?: return null
        val active = state.activeWordIndices
        val pos = active.indexOf(fromIndex)
        if (pos < 0) return null
        return active.getOrNull(pos - 1)
            ?.takeIf { it in puzzle.cells.indices && puzzle.cells[it] is Cell.Letter }
    }

    private data class IndexedClue(
        val index: Int,
        val cell: Cell.Clue,
    )

    private fun findCoveringClue(
        puzzle: Puzzle,
        selectedIndex: Int,
        direction: Direction,
    ): IndexedClue? {
        var best: IndexedClue? = null
        var bestDistance: Int? = null

        for (i in puzzle.cells.indices) {
            val cell = puzzle.cells[i]
            if (cell is Cell.Clue && cell.direction == direction) {
                val indices = computeAnswerIndices(
                    puzzle = puzzle,
                    clueIndex = i,
                    direction = cell.direction,
                    answerLength = cell.answerLength,
                )

                if (selectedIndex in indices) {
                    val distance = kotlin.math.abs(selectedIndex - i)
                    if (best == null || bestDistance == null || distance < bestDistance) {
                        best = IndexedClue(i, cell)
                        bestDistance = distance
                    }
                }
            }
        }

        return best
    }

    private fun computeAnswerIndices(
        puzzle: Puzzle,
        clueIndex: Int,
        direction: Direction,
        answerLength: Int,
    ): List<Int> {
        val indices = ArrayList<Int>(answerLength)
        var cur = clueIndex
        repeat(answerLength) {
            cur = stepForward(cur, puzzle.width, puzzle.height, direction) ?: return@repeat
            if (cur !in puzzle.cells.indices) return@repeat
            if (puzzle.cells[cur] !is Cell.Letter) return@repeat
            indices += cur
        }
        return indices
    }

    private fun stepForward(index: Int, width: Int, height: Int, direction: Direction): Int? {
        val row = index / width
        val col = index % width
        return when (direction) {
            Direction.RIGHT -> if (col + 1 < width) index + 1 else null
            Direction.DOWN -> if (row + 1 < height) index + width else null
        }
    }

    private fun stepBackward(index: Int, width: Int, height: Int, direction: Direction): Int? {
        val row = index / width
        val col = index % width
        return when (direction) {
            Direction.RIGHT -> if (col - 1 >= 0) index - 1 else null
            Direction.DOWN -> if (row - 1 >= 0) index - width else null
        }
    }

    private fun firstLetterIndex(puzzle: Puzzle): Int? {
        return puzzle.cells.indexOfFirst { it is Cell.Letter }.takeIf { it >= 0 }
    }

    private fun toggleDirection(direction: Direction): Direction {
        return when (direction) {
            Direction.RIGHT -> Direction.DOWN
            Direction.DOWN -> Direction.RIGHT
        }
    }

    private fun isSolved(puzzle: Puzzle, entries: CharArray): Boolean {
        if (entries.size != puzzle.cells.size) return false

        for (i in puzzle.cells.indices) {
            val cell = puzzle.cells[i]
            if (cell is Cell.Letter) {
                val entered = entries[i]
                if (entered == '\u0000') return false
                if (entered.uppercaseChar() != cell.solution.uppercaseChar()) return false
            }
        }
        return true
    }

    private data class SolvedInfo(
        val solvedClueIndices: Set<Int>,
        val solvedLetterIndices: Set<Int>,
    )

    private fun computeSolvedInfo(puzzle: Puzzle, entries: CharArray): SolvedInfo {
        val solvedClues = HashSet<Int>()
        val solvedLetters = HashSet<Int>()

        for (i in puzzle.cells.indices) {
            val cell = puzzle.cells[i]
            if (cell is Cell.Clue) {
                val indices = computeAnswerIndices(
                    puzzle = puzzle,
                    clueIndex = i,
                    direction = cell.direction,
                    answerLength = cell.answerLength,
                )

                if (indices.isEmpty()) continue

                val isCorrect = indices.all { idx ->
                    val solution = (puzzle.cells[idx] as? Cell.Letter)?.solution ?: return@all false
                    val entered = entries.getOrNull(idx) ?: return@all false
                    entered != '\u0000' && entered.uppercaseChar() == solution.uppercaseChar()
                }

                if (isCorrect) {
                    solvedClues += i
                    solvedLetters += indices
                }
            }
        }

        return SolvedInfo(
            solvedClueIndices = solvedClues,
            solvedLetterIndices = solvedLetters,
        )
    }

    private fun serializeEntries(entries: CharArray): String {
        val sb = StringBuilder(entries.size)
        for (c in entries) {
            sb.append(if (c == '\u0000') '.' else c)
        }
        return sb.toString()
    }

    private fun deserializeEntries(serialized: String, expectedSize: Int): CharArray {
        val result = CharArray(expectedSize) { '\u0000' }
        for (i in 0 until minOf(serialized.length, expectedSize)) {
            val c = serialized[i]
            result[i] = if (c == '.') '\u0000' else c
        }
        return result
    }

    companion object {
        fun factory(
            levelId: String,
            puzzleRepository: PuzzleRepository,
            progressRepository: ProgressRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return GameViewModel(
                            levelId = levelId,
                            puzzleRepository = puzzleRepository,
                            progressRepository = progressRepository,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

data class GameUiState(
    val levelId: String = "",
    val puzzle: Puzzle? = null,
    val selectedIndex: Int? = null,
    val selectedDirection: Direction = Direction.RIGHT,
    val activeWordIndices: List<Int> = emptyList(),
    val entries: CharArray = CharArray(0),
    val isCompleted: Boolean = false,
    val solvedClueIndices: Set<Int> = emptySet(),
    val solvedLetterIndices: Set<Int> = emptySet(),
)

sealed interface GameAction {
    data class SelectCell(val index: Int) : GameAction
    data class InputChar(val char: Char) : GameAction
    data object Backspace : GameAction
    data object NextLevel : GameAction
}

sealed interface GameViewEvent {
    data class NavigateToLevel(val levelId: String) : GameViewEvent
}
