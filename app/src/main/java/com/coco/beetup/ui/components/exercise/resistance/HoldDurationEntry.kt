package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HoldDurationEntry(value: Int, onValueChange: (Int) -> Unit) {
  val maxSeconds = 120f
  var holdDurationSeconds by remember(value) { mutableIntStateOf(value) }
  val clampedHoldDuration by remember {
    derivedStateOf { holdDurationSeconds.coerceIn(0, maxSeconds.toInt()) }
  }

  val scheme = MaterialTheme.colorScheme
  val rotationalOffset = 270f
  val animatedClamped by
      animateFloatAsState(
          clampedHoldDuration.toFloat(),
          spring(Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

  Column(modifier = Modifier.fillMaxWidth()) {
    Spacer(Modifier.size(16.dp))
    OutlinedTextField(
        value = holdDurationSeconds.toString(),
        onValueChange = { newValue ->
          if (newValue.isNotBlank()) {
            if (newValue.all { it.isDigit() }) {
              newValue.toIntOrNull()?.let {
                holdDurationSeconds = it
                onValueChange(it)
              }
            }
          } else {
            holdDurationSeconds = 0
            onValueChange(holdDurationSeconds)
          }
        },
        label = { Text("Hold Duration (seconds)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.width(150.dp).fillMaxWidth())
  }
}

@Preview(showBackground = true)
@Composable
fun HoldDurationEntryPreview() {
  var duration by remember { mutableIntStateOf(60) }
  HoldDurationEntry(value = duration) { duration = it }
}
