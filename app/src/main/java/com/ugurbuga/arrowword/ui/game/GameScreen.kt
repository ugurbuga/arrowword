package com.ugurbuga.arrowword.ui.game

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.KeyEvent
import androidx.compose.foundation.layout.Row
import com.ugurbuga.arrowword.R
import com.ugurbuga.arrowword.domain.model.Cell
import com.ugurbuga.arrowword.domain.model.Direction
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun GameScreen(
    state: GameUiState,
    onAction: (GameAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    var keyboardBuffer by remember { mutableStateOf("") }
    var isCongratsDialogOpen by remember { mutableStateOf(false) }
    var clueTooltipText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            isCongratsDialogOpen = true
        }
    }

    LaunchedEffect(state.selectedIndex) {
        if (state.selectedIndex != null) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    LaunchedEffect(clueTooltipText) {
        if (clueTooltipText != null) {
            delay(1500)
            clueTooltipText = null
        }
    }

    GameScreenDialogs(
        isCongratsDialogOpen = isCongratsDialogOpen,
        onDismissCongrats = { isCongratsDialogOpen = false },
        isLoading = state.isLoading,
        onNextLevel = {
            isCongratsDialogOpen = false
            onAction(GameAction.NextLevel)
        },
    )

    GameScaffold(
        levelId = state.levelId,
        onNavigateBack = onNavigateBack,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    )
                },
        ) {
            if (state.isLoading) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Text(text = stringResource(R.string.loading))
                    }
                }
            }

            if (clueTooltipText != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Text(
                        text = clueTooltipText ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextField(
                    value = keyboardBuffer,
                    onValueChange = { newValue ->
                        when {
                            newValue.isEmpty() && keyboardBuffer.isNotEmpty() -> {
                                onAction(GameAction.Backspace)
                            }

                            newValue.isNotEmpty() -> {
                                val c = newValue.last()
                                if (c.isLetter()) {
                                    onAction(GameAction.InputChar(c))
                                }
                            }
                        }
                        keyboardBuffer = ""
                    },
                    modifier = Modifier
                        .alpha(0f)
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            val nativeEvent = event.nativeKeyEvent
                            if (nativeEvent.action == KeyEvent.ACTION_DOWN && nativeEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                                onAction(GameAction.Backspace)
                                true
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                )

                state.puzzle?.let { puzzle ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            state.selectedClueText?.let { clueText ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                ) {
                                    Text(
                                        text = clueText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            for (row in 0 until puzzle.height) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    for (col in 0 until puzzle.width) {
                                        val index = row * puzzle.width + col
                                        val cell = puzzle.cells[index]
                                        val isSelected = state.selectedIndex == index
                                        val inActiveWord = state.activeWordIndices.contains(index)
                                        val isSolvedLetter = index in state.solvedLetterIndices

                                        PuzzleCell(
                                            cell = cell,
                                            entered = state.entries.getOrNull(index),
                                            selected = isSelected,
                                            inActiveWord = inActiveWord,
                                            solved = isSolvedLetter,
                                            onClick = {
                                                if (state.isLoading) return@PuzzleCell
                                                when (cell) {
                                                    is Cell.Letter -> {
                                                        onAction(GameAction.SelectCell(index))
                                                        focusRequester.requestFocus()
                                                        keyboardController?.show()
                                                    }

                                                    is Cell.Clue -> {
                                                        clueTooltipText = cell.text
                                                        focusManager.clearFocus(force = true)
                                                        keyboardController?.hide()
                                                    }

                                                    is Cell.Block -> {
                                                        focusManager.clearFocus(force = true)
                                                        keyboardController?.hide()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun GameScaffold(
    levelId: String,
    onNavigateBack: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.top_app_bar_level_title, levelId)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        content = content,
    )
}

@Composable
private fun GameScreenDialogs(
    isCongratsDialogOpen: Boolean,
    onDismissCongrats: () -> Unit,
    isLoading: Boolean,
    onNextLevel: () -> Unit,
) {
    if (isCongratsDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismissCongrats,
            title = { Text(text = stringResource(R.string.congrats_dialog_title)) },
            text = { Text(text = stringResource(R.string.congrats_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onNextLevel, enabled = !isLoading) {
                    Text(text = stringResource(R.string.next_level))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCongrats, enabled = !isLoading) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun PuzzleCell(
    cell: Cell,
    entered: Char?,
    selected: Boolean,
    inActiveWord: Boolean,
    solved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trLocale = Locale("tr", "TR")

    val bgColor = when (cell) {
        is Cell.Block -> Color.DarkGray
        is Cell.Clue -> MaterialTheme.colorScheme.tertiaryContainer
        is Cell.Letter -> when {
            solved -> Color(0xFF2E7D32)
            inActiveWord -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }
    }

    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.dp
    val overlayAlpha = if (selected) 0.10f else 0f
    val elevation = if (selected) 6.dp else 0.dp
    val arrowSize = 14.dp

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(6.dp), clip = false)
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
            .background(bgColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
    ) {
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = overlayAlpha),
                        shape = RoundedCornerShape(6.dp),
                    ),
            )
        }

        when (cell) {
            is Cell.Block -> Unit

            is Cell.Clue -> {
                val marker = when (cell.direction) {
                    Direction.RIGHT -> stringResource(R.string.clue_marker_right)
                    Direction.DOWN -> stringResource(R.string.clue_marker_down)
                }

                val clueStyle: androidx.compose.ui.text.TextStyle = when {
                    cell.text.length <= 18 -> MaterialTheme.typography.labelSmall
                    cell.text.length <= 32 -> MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                    else -> MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                }

                Text(
                    text = cell.text,
                    modifier = when (cell.direction) {
                        Direction.RIGHT -> Modifier.padding(end = arrowSize + 2.dp)
                        Direction.DOWN -> Modifier.padding(bottom = arrowSize + 2.dp)
                    },
                    style = clueStyle,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = marker,
                    modifier = when (cell.direction) {
                        Direction.RIGHT -> Modifier.align(Alignment.CenterEnd)
                        Direction.DOWN -> Modifier.align(Alignment.BottomCenter)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                )
            }

            is Cell.Letter -> {
                val shown = if (entered == null || entered == '\u0000') {
                    ""
                } else {
                    entered.toString().uppercase(trLocale)
                }
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = shown,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (solved) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    ArrowwordTheme {
        Surface {
            GameScreen(
                state = GameUiState(levelId = "easy-001"),
                onAction = {},
                onNavigateBack = {},
            )
        }
    }
}
