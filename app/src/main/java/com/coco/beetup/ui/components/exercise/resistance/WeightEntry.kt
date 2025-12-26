package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun WeightEntry(value: String, onValueChange: (String) -> Unit) {
  var selectedUnit by remember { mutableStateOf("kg") }

  Column {
    OutlinedTextField(
        value = value,
        onValueChange = {
          if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
            onValueChange(it)
          }
        },
        label = { Text("Weight") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true)
    Row {
      Text(
          text = "kg",
          color =
              if (selectedUnit == "kg") MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.clickable { selectedUnit = "kg" })
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = "g",
          color =
              if (selectedUnit == "g") MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.clickable { selectedUnit = "g" })
    }
  }
}
