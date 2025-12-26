package com.coco.beetup.ui.components.activity

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
import com.coco.beetup.ui.components.exercise.resistance.EdgeEntry
import com.coco.beetup.ui.components.exercise.resistance.HoldDurationEntry
import com.coco.beetup.ui.components.exercise.resistance.WeightEntry
import kotlin.collections.set

val ResistanceEntryMap =
    mapOf<Int, @Composable (value: Int, onValueChange: (Int) -> Unit) -> Unit>(
        (2 to ::WeightEntry), (4 to ::EdgeEntry), (5 to ::HoldDurationEntry))

@Composable
fun ResistanceSelectionDialog(
    resistances: List<BeetResistance>,
    selectedResistances: Map<Int, Int>,
    onSelectionChange: (Int, Int?) -> Unit
) {
  LazyColumn(Modifier.fillMaxWidth()) {
    items(resistances) { resistance ->
      val isChecked = resistance.id in selectedResistances
      Column(Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(resistance.name) },
            leadingContent = { Checkbox(checked = isChecked, onCheckedChange = null) },
            modifier =
                Modifier.fillMaxWidth().clickable {
                  if (isChecked) {
                    onSelectionChange(resistance.id, null)
                  } else {
                    onSelectionChange(resistance.id, 0)
                  }
                })
        if (isChecked) {
          val resEntry = ResistanceEntryMap[resistance.id]
          if (resEntry != null) {
            resEntry(
                selectedResistances[resistance.id] ?: 0, { onSelectionChange(resistance.id, it) })
          } else {
            TextField(
                value = selectedResistances[resistance.id]?.toString() ?: "",
                onValueChange = { onSelectionChange(resistance.id, it.toIntOrNull()) },
                label = { Text("${resistance.name} Value (${resistance.unit})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp))
          }
        }
      }
    }
  }
}

@Composable
fun ResistanceSelectionDialog(
    resistances: List<BeetResistance>,
    onConfirm: (selectedResistances: Map<Int, Int>) -> Unit,
    onDismiss: () -> Unit,
) {
  val selectedResistances = remember { mutableStateMapOf<Int, Int>() }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Add Resistance") },
      text = {
        if (resistances.isEmpty()) {
          Text("No recommended resistances for this exercise.")
        } else {
          ResistanceSelectionDialog(
              resistances = resistances,
              selectedResistances = selectedResistances,
              onSelectionChange = { id, value ->
                if (value == null) {
                  selectedResistances.remove(id)
                } else {
                  selectedResistances[id] = value
                }
              })
        }
      },
      confirmButton = {
        TextButton(
            onClick = {
              val confirmedResistances = selectedResistances.mapValues { it.value }
              onConfirm(confirmedResistances)
            },
        ) {
          Text("Confirm")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
