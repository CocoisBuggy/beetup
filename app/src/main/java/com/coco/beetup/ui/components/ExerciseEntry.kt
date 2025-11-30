package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun ExerciseEntry(
    text: String,
    isSelected: Boolean, // <-- New parameter
    modifier: Modifier = Modifier // <-- New parameter
) {
    var sets by remember { mutableIntStateOf(2) }

    ListItem(
        headlineContent = { Text(text) },
        supportingContent = { Text("6 reps, $sets sets, 50kg") },
        leadingContent = {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "Exercise Icon"
            )
        },
        trailingContent = {
            Button(onClick = { sets++ }) {
                Text("+1 Set")
            }
        },
        colors = ListItemDefaults.colors(
            // 4. Change color based on selection state
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        modifier = modifier // Apply the combinedClickable modifier here
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
    )
}