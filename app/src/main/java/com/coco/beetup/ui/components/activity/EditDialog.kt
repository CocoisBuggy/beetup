package com.coco.beetup.ui.components.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.BeetExpandedResistance
import com.coco.beetup.core.data.BeetResistance
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.launch

private fun mapRes(resistances: List<BeetExpandedResistance>): List<Pair<Int, String>> {
  val list = mutableListOf<Pair<Int, String>>()
  resistances.forEach { list.add(Pair(it.extra.id, it.entry.resistanceValue.toString())) }
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
  val selectedResistances = remember {
    mutableStateMapOf(*mapRes(forItem.logs.first().resistances).toTypedArray())
  }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Edit Activity") },
      text = {
        Column {
          Resistances(
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
                for (log in forItem.logs) {
                  viewModel.updateLogAndResistances(log.log, selectedResistances)
                }
              }
            }) {
              Icon(Icons.Default.Save, "Save Icon")
              Text("Confirm")
            }
      })
}
