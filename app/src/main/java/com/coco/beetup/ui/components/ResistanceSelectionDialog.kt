package com.coco.beetup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetResistance

@Composable
fun ResistanceSelectionDialog(
    resistances: List<BeetResistance>,
    validResistances: List<BeetResistance>,
    onConfirm: (selectedResistances: Map<Int, Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedResistances = remember { mutableStateMapOf<Int, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Resistances") },
        text = {
            LazyColumn {
                items(resistances) { resistance ->
                    val isChecked = resistance.id in selectedResistances
                    val isValid = resistance in validResistances

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selectedResistances.remove(resistance.id)
                                    } else {
                                        selectedResistances[resistance.id] = ""
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Text(
                                text = resistance.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            if (isValid) {
                                Text(
                                    text = " (recommended)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        if (isChecked) {
                            TextField(
                                value = selectedResistances[resistance.id] ?: "",
                                onValueChange = { value ->
                                    if (value.all { it.isDigit() }) {
                                        selectedResistances[resistance.id] = value
                                    }
                                },
                                label = { Text("${resistance.name} Value") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val confirmedResistances = selectedResistances
                        .filter { it.value.isNotBlank() }
                        .mapValues { it.value.toInt() }
                    onConfirm(confirmedResistances)
                },
                enabled = selectedResistances.isNotEmpty() && selectedResistances.values.all { it.isNotBlank() && it.toIntOrNull() != null }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
