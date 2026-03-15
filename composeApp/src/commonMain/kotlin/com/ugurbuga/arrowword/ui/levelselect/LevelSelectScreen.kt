package com.ugurbuga.arrowword.ui.levelselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.ugurbuga.arrowword.generated.resources.Res
import com.ugurbuga.arrowword.generated.resources.completed_levels
import com.ugurbuga.arrowword.generated.resources.play
import com.ugurbuga.arrowword.generated.resources.preparing_puzzle
import com.ugurbuga.arrowword.generated.resources.top_app_bar_title_home
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme
import androidx.compose.material3.Button

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LevelSelectScreen(
    state: LevelSelectUiState,
    onAction: (LevelSelectAction) -> Unit,
) {
    if (state.isLoading) {
        AlertDialog(
            onDismissRequest = {},
            text = { Text(text = stringResource(Res.string.preparing_puzzle)) },
            confirmButton = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.top_app_bar_title_home)) },
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
                    text = stringResource(Res.string.completed_levels, state.completedCount),
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
                Text(text = stringResource(Res.string.play))
            }
        }
    }
}
