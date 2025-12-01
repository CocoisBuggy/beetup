package com.coco.beetup.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeletableExerciseEntry(
    itemText: String,
    isSelected: Boolean,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    onToggleMultiSelection: () -> Unit,
    multiSelectionEnabled: Boolean,
) {
    ExerciseEntry(
        text = itemText,
        isSelected = isSelected,
        modifier = Modifier.combinedClickable(
            onClick = {
                onToggleSelection()
            },
            onLongClick = {
                onToggleMultiSelection()
            }
        )
    )
}
