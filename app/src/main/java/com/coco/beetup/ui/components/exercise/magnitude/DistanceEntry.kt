package com.coco.beetup.ui.components.exercise.magnitude

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.roundToInt

@Composable
fun DistanceEntry(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
  var isKm by remember { mutableStateOf(false) }
  var expanded by remember { mutableStateOf(false) }
  var text by remember { mutableStateOf("") }

  LaunchedEffect(value, isKm) {
    val currentParsed =
        if (isKm) {
          (text.toDoubleOrNull()?.times(1000))?.roundToInt() ?: 0
        } else {
          text.toIntOrNull() ?: 0
        }

    if (currentParsed != value) {
      text =
          if (value == 0) ""
          else {
            if (isKm) {
              (value / 1000.0).toString().removeSuffix(".0")
            } else {
              value.toString()
            }
          }
    }
  }

  OutlinedTextField(
      value = text,
      onValueChange = { newText ->
        text = newText
        if (newText.isEmpty()) {
          onValueChange(0)
        } else {
          if (isKm) {
            newText.toDoubleOrNull()?.let { onValueChange((it * 1000).roundToInt()) }
          } else {
            newText.toIntOrNull()?.let { onValueChange(it) }
          }
        }
      },
      label = { Text("Distance") },
      trailingIcon = {
        Box {
          TextButton(onClick = { expanded = true }) {
            Text(if (isKm) "km" else "m")
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Unit")
          }
          DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("m") },
                onClick = {
                  isKm = false
                  expanded = false
                })
            DropdownMenuItem(
                text = { Text("km") },
                onClick = {
                  isKm = true
                  expanded = false
                })
          }
        }
      },
      singleLine = true,
      keyboardOptions =
          KeyboardOptions(keyboardType = if (isKm) KeyboardType.Decimal else KeyboardType.Number),
      modifier = modifier)
}
