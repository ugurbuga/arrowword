package com.ugurbuga.arrowword.ui.levelselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ugurbuga.arrowword.R
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme
import androidx.compose.material3.Button

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.completed_levels, state.completedCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                enabled = !state.isLoading,
                onClick = { onAction(LevelSelectAction.RandomPuzzle) },
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
                Text(text = stringResource(R.string.play))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LevelSelectScreenPreview() {
    ArrowwordTheme {
        Surface {
            LevelSelectScreen(
                state = LevelSelectUiState(
                    completedCount = 3,
                ),
                onAction = {},
            )
        }
    }
}
