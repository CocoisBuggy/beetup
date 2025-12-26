package com.coco.beetup.ui.components.exercise.magnitude

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coco.beetup.ui.components.time.DurationPicker

@Composable
fun DurationEntry(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
  DurationPicker(seconds = value, onSecondsChanged = onValueChange, modifier = modifier)
}
