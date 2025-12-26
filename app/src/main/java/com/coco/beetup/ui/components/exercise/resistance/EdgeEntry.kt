package com.coco.beetup.ui.components.exercise.resistance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeEntry(value: Int, onValueChange: (Int) -> Unit) {
  var sliderValue by remember { mutableFloatStateOf(value.toFloat().coerceIn(1f, 50f)) }

  LaunchedEffect(value) { sliderValue = value.toFloat().coerceIn(1f, 50f) }

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
              value = value.toString(),
              onValueChange = { newValue ->
                val parsedValue = newValue.toFloatOrNull()
                if (parsedValue != null) {
                  val clampedValue = parsedValue.coerceIn(1f, 50f)
                  sliderValue = clampedValue
                  onValueChange(clampedValue.toInt())
                }
              },
              label = { Text("Edge (MM)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f))
          Spacer(modifier = Modifier.width(8.dp))
          Text("MM", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        RulerSlider(
            value = sliderValue,
            onValueChange = { newValue ->
              sliderValue = newValue
              onValueChange(newValue.toInt())
            },
            range = 1f..50f)
      }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun RulerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
  val textMeasurer = rememberTextMeasurer()
  val scheme = MaterialTheme.colorScheme

  Column(modifier = Modifier.fillMaxWidth()) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        steps = (range.endInclusive - range.start - 1).toInt(), // For discrete steps
        modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(8.dp))

    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
      val canvasWidth = size.width
      val startValue = range.start
      val endValue = range.endInclusive
      val valueRange = endValue - startValue

      val lineY = 0f
      val textYOffset = 15.dp.toPx()

      // Draw marks
      for (i in startValue.toInt()..endValue.toInt()) {
        val x = ((i - startValue) / valueRange) * canvasWidth
        val isMajorMark = (i % 5 == 0)

        val lineColor = if (isMajorMark) scheme.onSurfaceVariant else Color.Gray
        val lineHeight = if (isMajorMark) 15.dp.toPx() else 8.dp.toPx()
        val lineWidth = if (isMajorMark) 2.dp.toPx() else 1.dp.toPx()

        // Draw line
        drawLine(
            color = lineColor,
            start = Offset(x, lineY),
            end = Offset(x, lineY + lineHeight),
            strokeWidth = lineWidth)

        // Draw text for major marks
        if (isMajorMark) {
          val textLayoutResult =
              textMeasurer.measure(
                  text = AnnotatedString(i.toString()),
                  style = TextStyle(fontSize = 10.sp, color = scheme.onSurface))

          drawText(
              textLayoutResult = textLayoutResult,
              topLeft = Offset(x - textLayoutResult.size.width / 2, lineY + textYOffset))
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun EdgeEntryPreview() {
  MaterialTheme { EdgeEntry(value = 25, onValueChange = {}) }
}
