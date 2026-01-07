package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class TimeInterval {
  Sec,
  Min,
  Hr
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HoldDurationEntry(value: Int, onValueChange: (Int) -> Unit) {
  var holdDurationSeconds by remember(value) { mutableIntStateOf(value) }
  var unit by remember { mutableStateOf(TimeInterval.Sec) }
  val displaySeconds by
      remember(unit, holdDurationSeconds) {
        derivedStateOf {
          when (unit) {
            TimeInterval.Sec -> holdDurationSeconds.toFloat()
            TimeInterval.Min -> holdDurationSeconds.toFloat() / 60f
            TimeInterval.Hr -> holdDurationSeconds.toFloat() / 60f / 60f
          }
        }
      }

  Column(modifier = Modifier.fillMaxWidth()) {
    Spacer(Modifier.size(16.dp))

    OutlinedTextField(
        value = if (displaySeconds == 0f) "" else displaySeconds.toString(),
        onValueChange = { newValue ->
          newValue.toFloatOrNull()?.let { displayUpdate ->
            holdDurationSeconds =
                when (unit) {
                  TimeInterval.Sec -> displayUpdate
                  TimeInterval.Min -> displayUpdate * 60
                  TimeInterval.Hr -> displayUpdate * (60 * 60)
                }.toInt()
            onValueChange(holdDurationSeconds)
          }
        },
        label = { Text("Hold Duration") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth())

    ButtonGroup(
        overflowIndicator = { menuState ->
          ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        verticalAlignment = Alignment.Top,
    ) {
      TimeInterval.entries.forEach { interval ->
        toggleableItem(
            checked = interval == unit,
            label = if (interval == unit) interval.name else interval.name,
            onCheckedChange = { unit = interval },
            weight = 1f,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun HoldDurationEntryPreview() {
  var duration by remember { mutableIntStateOf(60) }
  HoldDurationEntry(value = duration) { duration = it }
}
