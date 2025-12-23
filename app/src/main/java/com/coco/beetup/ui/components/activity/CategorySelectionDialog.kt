package com.coco.beetup.ui.components.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.coco.beetup.core.data.BeetExercise

@Composable
fun CategorySelectionDialog(
    categories: List<BeetExercise>,
    usageCounts: Map<Int, Int>,
    lastUsedDays: Map<Int, Int>,
    onCategorySelected: (BeetExercise) -> Unit,
    onDismiss: () -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }

  val sortedCategories =
      remember(categories, usageCounts) {
        categories.sortedByDescending { usageCounts[it.id] ?: 0 }
      }

  val filteredCategories =
      if (searchQuery.isBlank()) {
        sortedCategories
      } else {
        sortedCategories.filter { it.exerciseName.contains(searchQuery, ignoreCase = true) }
      }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Select Exercise") },
      text = {
        Column {
          OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              label = { Text("Search") },
              modifier = Modifier.fillMaxWidth(),
          )
          LazyColumn {
            items(filteredCategories) { category ->
              val isUnused = (usageCounts[category.id] ?: 0) == 0
              val textColor =
                  if (isUnused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                  else MaterialTheme.colorScheme.onSurface

              val lastUsed = lastUsedDays[category.id]
              val supportingText =
                  if (lastUsed == 0) "Used Today"
                  else if (lastUsed != null && lastUsed > 0) {
                    "$lastUsed days since last"
                  } else {
                    "Never used"
                  }

              ListItem(
                  headlineContent = { Text(text = category.exerciseName, color = textColor) },
                  supportingContent = {
                    Text(
                        text = supportingText,
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall)
                  },
                  modifier = Modifier.fillMaxWidth().clickable { onCategorySelected(category) },
                  colors =
                      androidx.compose.material3.ListItemDefaults.colors(
                          containerColor = androidx.compose.ui.graphics.Color.Transparent))
            }
          }
        }
      },
      confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
