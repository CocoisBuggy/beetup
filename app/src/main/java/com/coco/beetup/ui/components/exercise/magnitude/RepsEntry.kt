package com.coco.beetup.ui.components.exercise.magnitude

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun RepsEntry(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
  OutlinedTextField(
      value = if (value == 0) "" else value.toString(),
      onValueChange = {
        if (it.isEmpty()) {
          onValueChange(0)
        } else {
          it.toIntOrNull()?.let { num -> onValueChange(num) }
        }
      },
      label = { Text("Repetitions") },
      suffix = { Text("reps") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = modifier)
}
