package com.coco.beetup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date


@Composable
fun ActivityEntry(
    viewModel: BeetViewModel,
    day: Int,
    item: BeetExercise,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.exerciseLogs(day, item.id).collectAsState(initial = emptyList())

    ListItem(
        headlineContent = { Text("${item.exerciseName}") },
        supportingContent = { Text("${logs.size} sets") },
        leadingContent = {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "Exercise Icon"
            )
        },
        trailingContent = {
            AnimatedVisibility(isSelected) {
                FilledTonalButton(
                    onClick = {
                        viewModel.insertActivity(logs.last().copy(id = 0, logDate = Date()))
                    }
                ) {
                    Text("+1")
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        modifier = modifier
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
    )
}
