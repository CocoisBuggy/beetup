package com.coco.beetup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetResistance

@Composable
fun ResistanceSelectionDialog(
    resistances: List<BeetResistance>,
    onConfirm: (selectedResistances: Map<Int, Int>) -> Unit,
    onDismiss: () -> Unit,
) {
  val selectedResistances = remember { mutableStateMapOf<Int, String>() }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Add Resistance") },
      text = {
        if (resistances.isEmpty()) {
          Text("No recommended resistances for this exercise.")
        } else {
          LazyColumn {
            items(resistances) { resistance ->
              val isChecked = resistance.id in selectedResistances
              Column {
                ListItem(
                    headlineContent = { Text(resistance.name) },
                    leadingContent = { Checkbox(checked = isChecked, onCheckedChange = null) },
                    modifier =
                        Modifier.clickable {
                          if (isChecked) {
                            selectedResistances.remove(resistance.id)
                          } else {
                            selectedResistances[resistance.id] = ""
                          }
                        })
                if (isChecked) {
                  TextField(
                      value = selectedResistances[resistance.id] ?: "",
                      onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                          selectedResistances[resistance.id] = value
                        }
                      },
                      label = { Text("${resistance.name} Value (${resistance.unit}") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      modifier =
                          Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(
            onClick = {
              val confirmedResistances =
                  selectedResistances
                      .filter { it.value.isNotBlank() }
                      .mapValues { it.value.toInt() }
              onConfirm(confirmedResistances)
            },
            enabled =
                selectedResistances.values.all { it.isNotBlank() && it.toIntOrNull() != null }) {
              Text("Confirm")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
