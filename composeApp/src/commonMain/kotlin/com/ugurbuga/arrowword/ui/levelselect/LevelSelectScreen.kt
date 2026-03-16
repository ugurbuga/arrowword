package com.ugurbuga.arrowword.ui.levelselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.ugurbuga.arrowword.generated.resources.puzzle_size_large
import com.ugurbuga.arrowword.generated.resources.puzzle_size_medium
import com.ugurbuga.arrowword.generated.resources.puzzle_size_small
import com.ugurbuga.arrowword.generated.resources.puzzle_size_title
import com.ugurbuga.arrowword.generated.resources.puzzle_size_xl
import com.ugurbuga.arrowword.generated.resources.puzzle_size_xxl
import com.ugurbuga.arrowword.generated.resources.top_app_bar_title_home
import com.ugurbuga.arrowword.ui.theme.ArrowwordTheme
import androidx.compose.material3.Button
import com.ugurbuga.arrowword.domain.model.GeneratedPuzzleSize

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

                Text(
                    text = stringResource(Res.string.puzzle_size_title),
                    style = MaterialTheme.typography.titleSmall,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val allSizes = listOf(
                        GeneratedPuzzleSize.SMALL,
                        GeneratedPuzzleSize.MEDIUM,
                        GeneratedPuzzleSize.LARGE,
                        GeneratedPuzzleSize.XL,
                        GeneratedPuzzleSize.XXL,
                    )

                    allSizes.forEach { size ->
                        val labelRes = when (size) {
                            GeneratedPuzzleSize.SMALL -> Res.string.puzzle_size_small
                            GeneratedPuzzleSize.MEDIUM -> Res.string.puzzle_size_medium
                            GeneratedPuzzleSize.LARGE -> Res.string.puzzle_size_large
                            GeneratedPuzzleSize.XL -> Res.string.puzzle_size_xl
                            GeneratedPuzzleSize.XXL -> Res.string.puzzle_size_xxl
                        }

                        FilterChip(
                            selected = state.selectedSize == size,
                            onClick = { onAction(LevelSelectAction.SelectSize(size)) },
                            label = { Text(text = stringResource(labelRes)) },
                            enabled = !state.isLoading,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                enabled = !state.isLoading,
                onClick = { onAction(LevelSelectAction.Play) },
            ) {
                Text(text = stringResource(Res.string.play))
            }
        }
    }
}
