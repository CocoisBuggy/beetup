package com.coco.beetup.ui.components.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.BeetExpandedResistance
import com.coco.beetup.core.data.BeetResistance
import com.coco.beetup.ui.components.time.DurationPicker
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.launch

private fun mapRes(resistances: List<BeetExpandedResistance>): List<Pair<Int, Int>> {
  val list = mutableListOf<Pair<Int, Int>>()
  resistances.forEach { list.add(Pair(it.extra.id, it.entry.resistanceValue)) }
  return list
}

@Composable
fun EditDialog(
    viewModel: BeetViewModel,
    forItem: ActivityGroup,
    onCommit: () -> Unit,
    onDismiss: () -> Unit,
    allowedResistances: List<BeetResistance>
) {
  val scope = rememberCoroutineScope()
  val selectedResistances: SnapshotStateMap<Int, Int> = remember {
    mutableStateMapOf(*mapRes(forItem.logs.first().resistances).toTypedArray())
  }
  var restValue by remember { mutableIntStateOf(forItem.logs.first().log.rest ?: 0) }
  var restEntry by remember { mutableStateOf(forItem.logs.first().log.rest != null) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Edit Activity") },
      text = {
        Column {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text("Include Rest Time")
                Checkbox(checked = restEntry, onCheckedChange = { restEntry = it })
              }

          if (restEntry) {
            Text("Rest Duration", modifier = Modifier.padding(bottom = 8.dp))
            DurationPicker(
                seconds = restValue,
                onSecondsChanged = { restValue = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
          }

          Spacer(Modifier.size(12.dp))

          Text("Resistances", style = MaterialTheme.typography.titleMedium)

          ResistanceSelectionDialog(
              resistances = allowedResistances,
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
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
      confirmButton = {
        TextButton(
            onClick = {
              scope.launch {
                val newRest = if (restValue > 0 && restEntry) restValue else null
                for (log in forItem.logs) {
                  viewModel.updateLogAndResistances(
                      log.log.copy(rest = newRest), selectedResistances)
                }
                onCommit()
                onDismiss()
              }
            }) {
              Icon(Icons.Default.Save, "Save Icon")
              Text("Confirm")
            }
      })
}
