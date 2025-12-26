package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeightEntry(value: Int, onValueChange: (Int) -> Unit) {
  var selectedUnit by remember { mutableStateOf("kg") }

  Column(Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = if (value > 0) (if (selectedUnit == "g") value else value / 1000f).toString() else "",
        onValueChange = {
          onValueChange(
              when (selectedUnit) {
                "g" -> it.toIntOrNull()
                "kg" -> {
                  it.toFloatOrNull()?.let { parsed -> (parsed * 1000).toInt() }
                }
                else -> null
              } ?: 0)
        },
        label = { Text("Weight") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true)

    Row(Modifier.fillMaxWidth()) {
      ButtonGroup(
          overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
          },
          verticalAlignment = Alignment.Top,
          modifier = Modifier.fillMaxWidth(),
      ) {
        toggleableItem(
            checked = selectedUnit == "kg",
            onCheckedChange = { selectedUnit = "kg" },
            label = "kg",
            icon = { Icon(Icons.Default.MonitorWeight, null) })

        toggleableItem(
            checked = selectedUnit == "g",
            onCheckedChange = { selectedUnit = "g" },
            label = "grams",
        )
      }
    }
  }
}
