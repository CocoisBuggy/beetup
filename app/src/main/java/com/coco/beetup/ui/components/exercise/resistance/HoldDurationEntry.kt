package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldDurationEntry(value: String, onValueChange: (String) -> Unit) {
  var holdDurationSeconds by remember(value) { mutableIntStateOf(value.toIntOrNull() ?: 60) }

  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        // Circular Dial
        Box(
            modifier =
                Modifier.size(200.dp).padding(16.dp).pointerInput(Unit) {
                  detectDragGestures { change, _ ->
                    val dialCenter = Offset(size.width / 2f, size.height / 2f)
                    val x = change.position.x - dialCenter.x
                    val y = change.position.y - dialCenter.y
                    var angle = -atan2(x, y) * (180f / PI).toFloat()
                    if (angle < 0) angle += 360f

                    // Map angle (0-360) to seconds (60-120)
                    // The dial starts at 270 degrees (top) for 60 seconds (1 minute)
                    // and sweeps clockwise to 270 + 360 = 630 degrees for 120 seconds (2 minutes).
                    // Let's adjust the angle calculation to fit this range for a more intuitive
                    // drag.

                    val normalizedAngle =
                        if (angle >= 270f) angle - 270f else angle + 90f // 0-360 for actual sweep
                    val newDuration =
                        (60 + (normalizedAngle / 360f * 60f)).toInt().coerceIn(60, 120)

                    holdDurationSeconds = newDuration
                    onValueChange(newDuration.toString())
                  }
                },
            contentAlignment = Alignment.Center) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                val radius = (size.minDimension / 2f) - (strokeWidth / 2f)
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Background arc
                drawArc(
                    color = Color.LightGray,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Progress arc
                val progress = (holdDurationSeconds - 60) / 60f
                val sweepAngle = progress * 360f

                drawArc(
                    color = Color.Blue,
                    startAngle = 270f, // Start from top
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Indicator
                val indicatorAngleRadians = (270f + sweepAngle) * PI.toFloat() / 180f
                val indicatorOffset =
                    Offset(
                        x = centerOffset.x + radius * cos(indicatorAngleRadians),
                        y = centerOffset.y + radius * sin(indicatorAngleRadians))
                drawCircle(color = Color.Blue, radius = strokeWidth / 2, center = indicatorOffset)
              }

              Text(text = "${holdDurationSeconds}s", fontSize = 32.sp, color = Color.Black)
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Text field to override value
        OutlinedTextField(
            value = holdDurationSeconds.toString(),
            onValueChange = { newValue ->
              if (newValue.isNotBlank()) {
                // Only allow numeric input
                if (newValue.all { it.isDigit() }) { // Changed from Regex to all { it.isDigit() }
                  val newDuration = newValue.toIntOrNull()?.coerceIn(60, 120)
                  if (newDuration != null) {
                    holdDurationSeconds = newDuration
                    onValueChange(newDuration.toString())
                  }
                }
              } else {
                // If the text field is cleared, reset to 60 seconds (1 minute)
                holdDurationSeconds = 60
                onValueChange("60")
              }
            },
            label = { Text("Hold Duration (seconds)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(150.dp))
      }
}

@Preview(showBackground = true)
@Composable
fun HoldDurationEntryPreview() {
  var duration by remember { mutableStateOf("60") }
  HoldDurationEntry(value = duration) { duration = it }
}
