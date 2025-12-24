package com.coco.beetup.ui.components.time

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun DurationPicker(seconds: Int, onSecondsChanged: (Int) -> Unit, modifier: Modifier = Modifier) {
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val remainingSeconds = seconds % 60

  var currentHours by remember { mutableIntStateOf(hours) }
  var currentMinutes by remember { mutableIntStateOf(minutes) }
  var currentSeconds by remember { mutableIntStateOf(remainingSeconds) }

  LaunchedEffect(hours, minutes, remainingSeconds) {
    currentHours = hours
    currentMinutes = minutes
    currentSeconds = remainingSeconds
  }

  Box(modifier = modifier.height(180.dp), contentAlignment = Alignment.Center) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().fadingEdge(MaterialTheme.colorScheme.surface)) {
          // Hours
          NumberPicker(
              value = currentHours,
              range = 0..23,
              onValueChange = { newHours ->
                currentHours = newHours
                onSecondsChanged(currentHours * 3600 + currentMinutes * 60 + currentSeconds)
              },
              label = "h")

          // Minutes
          NumberPicker(
              value = currentMinutes,
              range = 0..59,
              onValueChange = { newMinutes ->
                currentMinutes = newMinutes
                onSecondsChanged(currentHours * 3600 + newMinutes * 60 + currentSeconds)
              },
              label = "m")

          // Seconds
          NumberPicker(
              value = currentSeconds,
              range = 0..59,
              onValueChange = { newSeconds ->
                currentSeconds = newSeconds
                onSecondsChanged(currentHours * 3600 + currentMinutes * 60 + newSeconds)
              },
              label = "s")
        }

    // Selection Indicator (Overlay)
    // You might want to add a visual indicator (like two horizontal lines)
    // behind or in front of the selected row.
  }
}

@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium
) {
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
  val density = LocalDensity.current

  // Calculate item height based on text style or fix it
  val itemHeight = 60.dp
  val itemHeightPx = with(density) { itemHeight.toPx() }

  // Scroll to the initial value when it changes externally
  LaunchedEffect(value) {
    if (!listState.isScrollInProgress) {
      // Avoid scrolling if the user is currently interacting
      // Check if the current visible item is different
      if (listState.firstVisibleItemIndex != value) {
        listState.scrollToItem(value)
      }
    }
  }

  // Detect snap changes
  LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .map { index -> range.first + index }
        .distinctUntilChanged()
        .collect { index ->
          if (index in range && index != value) {
            onValueChange(index)
          }
        }
  }

  val itemCount = range.last - range.first + 1

  // We add padding items to allow the first and last items to be centered
  val visibleItemsCount = 3 // Number of items visible at once (approx)

  // This simple implementation relies on Snapping
  // Compose 1.3+ has rememberSnapFlingBehavior

  val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

  Row(verticalAlignment = Alignment.CenterVertically) {
    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.End,
        contentPadding = PaddingValues(vertical = itemHeight),
        modifier =
            Modifier.height(itemHeight * 3) // Show 3 items: previous, current, next
                .weight(1f, fill = false),
    ) {
      // Add spacers if we want to handle circular or just make sure 0 is selectable
      // However, for snapping to center, we usually need content padding.
      // But simpler is to use a fixed height container and snap logic.

      // Let's rely on contentPadding to center the first item?
      // Actually, simplest 'wheel' often just renders the list.
      // We need 'padding' so index 0 can be in the center.
      // height = 3 * itemHeight. Center is at itemHeight offset.

      items(count = itemCount) { index ->
        val number = range.first + index

        // Opacity/Scaling logic
        val opacity by remember {
          derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.find { it.index == index }

            if (itemInfo == null) 0.3f
            else {
              val viewportCenter = layoutInfo.viewportEndOffset / 2
              val itemCenter = itemInfo.offset + itemInfo.size / 2
              val distanceFromCenter = kotlin.math.abs(viewportCenter - itemCenter)

              val maxDistance = itemHeightPx * 1.5f // Fade out over 1.5 items distance
              val alpha = 1f - (distanceFromCenter / maxDistance).coerceIn(0f, 0.7f)
              alpha
            }
          }
        }

        Box(modifier = Modifier.height(itemHeight), contentAlignment = Alignment.Center) {
          Text(
              text = number.toString().padStart(2, '0'),
              style = textStyle,
              color = LocalContentColor.current.copy(alpha = opacity),
              maxLines = 1)
        }
      }
    }

    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = 4.dp, end = 8.dp))
  }
}

fun Modifier.fadingEdge(brush: Brush) =
    this.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
      drawContent()
      drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
fun Modifier.fadingEdge(color: Color) =
    this.fadingEdge(
        Brush.verticalGradient(
            0f to Color.Transparent, 0.2f to color, 0.8f to color, 1f to Color.Transparent))
