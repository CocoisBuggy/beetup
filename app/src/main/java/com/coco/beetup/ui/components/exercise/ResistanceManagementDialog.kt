package com.coco.beetup.ui.components.exercise

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coco.beetup.core.data.BeetResistance
import com.coco.beetup.ui.viewmodel.BeetViewModel

@Composable
fun ResistanceManagementDialog(
    selectedExercise: Int?,
    showResistanceDialog: Boolean,
    viewModel: BeetViewModel,
    resistances: List<BeetResistance>,
    onDismiss: () -> Unit
) {
  selectedExercise?.let { selectedExercise ->
    if (showResistanceDialog) {
      val validResistances by
          viewModel.validResistancesFor(selectedExercise).collectAsState(initial = emptyList())

      AlertDialog(
          title = { Text("Manage Resistance Types") },
          onDismissRequest = onDismiss,
          text = {
            Column() {
              for (res in resistances) {
                ListItem(
                    colors =
                        androidx.compose.material3.ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text(res.name) },
                    leadingContent = {
                      Checkbox(
                          checked = validResistances.contains(res),
                          onCheckedChange = {
                            if (it) {

                              viewModel.insertResistanceReference(selectedExercise, res.id)
                            } else {
                              viewModel.removeResistanceReference(selectedExercise, res.id)
                            }
                          })
                    })
              }
            }
          },
          confirmButton = { Button(onClick = onDismiss) { Text("Done") } },
      )
    }
  }
}
