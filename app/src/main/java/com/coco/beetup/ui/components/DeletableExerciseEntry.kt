package com.coco.beetup.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coco.beetup.core.data.BeetExerciseLog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeletableExerciseEntry(
    item: BeetExerciseLog,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onToggleMultiSelection: () -> Unit,
) {
    ExerciseEntry(
        text = item,
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
