package com.coco.beetup.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date

@Composable
fun ActivityEntry(
    viewModel: BeetViewModel,
    activity: ActivityGroup,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
  ListItem(
      headlineContent = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text("${activity.exercise.exerciseName}")
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            activity.logs.firstOrNull()?.resistances?.forEach { res ->
              AssistChip(
                  {},
                  label = { Text("${res.entry.resistanceValue}${res.extra.unit}") },
                  enabled = false)
            }
          }
        }
      },
      supportingContent = {
        Column {
          val text = mutableListOf<String>()

          activity.logs.firstOrNull()?.let {
            text.add("${it.log.magnitude} ${activity.magnitude.unit}")
          }

          text.add("${activity.logs.size} sets")

          Text(text.joinToString(", "))
        }
      },
      leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = "Exercise Icon") },
      trailingContent = {
        FilledTonalButton(
            onClick = {
              viewModel.insertActivityAndResistances(
                  activity.logs.last().log.copy(id = 0, logDate = Date()),
                  activity.logs.last().resistances.map { it.entry })
            }) {
              Text("+1")
            }
      },
      colors =
          ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
      modifier =
          modifier
              .padding(vertical = 4.dp)
              .clip(MaterialTheme.shapes.medium)
              .border(
                  width = 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = MaterialTheme.shapes.medium))
}
