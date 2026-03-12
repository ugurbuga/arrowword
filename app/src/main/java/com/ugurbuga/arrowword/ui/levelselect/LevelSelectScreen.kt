package com.ugurbuga.arrowword.ui.levelselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ugurbuga.arrowword.R
import com.ugurbuga.arrowword.domain.model.Difficulty
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LevelSelectScreen(
    state: LevelSelectUiState,
    onAction: (LevelSelectAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.top_app_bar_title_home)) },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.level_select_title),
                style = MaterialTheme.typography.headlineMedium,
            )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DifficultyButton(
                selected = state.selectedDifficulty == Difficulty.EASY,
                text = stringResource(R.string.difficulty_easy),
                onClick = { onAction(LevelSelectAction.SelectDifficulty(Difficulty.EASY)) },
            )
            DifficultyButton(
                selected = state.selectedDifficulty == Difficulty.MEDIUM,
                text = stringResource(R.string.difficulty_medium),
                onClick = { onAction(LevelSelectAction.SelectDifficulty(Difficulty.MEDIUM)) },
            )
            DifficultyButton(
                selected = state.selectedDifficulty == Difficulty.HARD,
                text = stringResource(R.string.difficulty_hard),
                onClick = { onAction(LevelSelectAction.SelectDifficulty(Difficulty.HARD)) },
            )
        }

        Text(
            text = stringResource(R.string.levels_section_title),
            style = MaterialTheme.typography.titleMedium,
        )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.levels) { level ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onAction(LevelSelectAction.SelectLevel(level.id)) },
                        enabled = level.isEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (level.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(text = stringResource(R.string.level_item_title, level.order))

                        if (level.isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.level_completed_tick))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyButton(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        ),
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun LevelSelectScreenPreview() {
    ArrowwordTheme {
        Surface {
            LevelSelectScreen(
                state = LevelSelectUiState(
                    selectedDifficulty = Difficulty.EASY,
                    levels = listOf(
                        LevelSelectItem(id = "easy-001", order = 1, isCompleted = true, isEnabled = true),
                        LevelSelectItem(id = "easy-002", order = 2, isCompleted = false, isEnabled = true),
                    ),
                ),
                onAction = {},
            )
        }
    }
}
