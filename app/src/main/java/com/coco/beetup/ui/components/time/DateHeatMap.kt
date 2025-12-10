package com.coco.beetup.ui.components.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityOverview
import com.coco.beetup.ui.components.grain.PulsingSunnyShape
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * A composable that displays a heatmap for a given set of dates. The heatmap is organized by month
 * and day of the week.
 */
@Composable
fun DateHeatMap(
    activeDates: List<ActivityOverview>,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
  val today = LocalDate.now()
  val fourWeeksAgo = today.minusWeeks(4)
  val earliestActiveDate = activeDates.minOfOrNull { it.date }
  val maxActivity = activeDates.maxOfOrNull { it.count } ?: 0

  val startDate =
      if (earliestActiveDate != null && earliestActiveDate.isAfter(fourWeeksAgo)) {
        // If the user's first activity is within the last 4 weeks,
        // start the calendar from the beginning of that month
        // to avoid showing empty previous months.
        earliestActiveDate.withDayOfMonth(1)
      } else {
        // Otherwise, stick to the default 4-week view.
        fourWeeksAgo
      }
  // Remember the calculated data to avoid re-computing on every recomposition
  val heatmapData =
      remember(startDate, today, activeDates) { generateHeatmapData(startDate, today, activeDates) }

  Column(modifier = modifier) {
    // Render each month's section
    heatmapData.forEach { (yearMonth, days) ->
      MonthSection(
          yearMonth = yearMonth,
          days = days,
          activeColor = activeColor,
          maxActivity = maxActivity,
      )
    }
  }
}

/** A section of the heatmap representing a single month. */
@Composable
private fun MonthSection(
    yearMonth: YearMonth,
    days: List<ActivityOverview>,
    activeColor: Color,
    maxActivity: Int
) {
  val monthName = remember {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    yearMonth.format(formatter)
  }
  val dayOfWeekHeaders = remember {
    // Generates ["S", "M", "T", "W", "T", "F", "S"]
    (1..7).map {
      LocalDate.of(2024, 1, it).dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    }
  }

  Column(modifier = Modifier.padding(bottom = 16.dp)) {
    Text(
        text = monthName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 28.dp, bottom = 8.dp) // Align with cells
        )
    // Add headers for days of the week
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      dayOfWeekHeaders.forEach { header ->
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          Text(text = header, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
    // Grid for the days
    LazyGridFor(items = days, numCols = 7) { day ->
      HeatmapCell(day = day, activeColor = activeColor, maxActivity = maxActivity)
    }
  }
}

/** A single cell in the heatmap grid. */
@Composable
private fun HeatmapCell(day: ActivityOverview, activeColor: Color, maxActivity: Int) {
  val color =
      when (day.count) {
        -1 -> Color.Transparent
        0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> activeColor.copy(alpha = day.count.toFloat() / maxActivity)
      }

  if (day.date.isEqual(LocalDate.now())) {
    PulsingSunnyShape(
        day.count,
        Modifier.padding(2.dp).aspectRatio(1f),
    ) {
        Icon(Icons.Default.Today, contentDescription = "TodayIcon")
    }
  } else {
    Box(
        modifier =
            Modifier.padding(2.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(color))
  }
}

private fun generateHeatmapData(
    startDate: LocalDate,
    endDate: LocalDate,
    activeDates: List<ActivityOverview>,
): Map<YearMonth, List<ActivityOverview>> {
  val data = mutableMapOf<YearMonth, MutableList<ActivityOverview>>()
  var currentDate = startDate

  while (!currentDate.isAfter(endDate)) {
    val monthData = data.getOrPut(YearMonth.from(currentDate)) { mutableListOf() }
    val actual = activeDates.find { it.date == currentDate }
    monthData.add(actual ?: ActivityOverview(currentDate, 0))
    currentDate = currentDate.plusDays(1)
  }

  val alignedData = mutableMapOf<YearMonth, List<ActivityOverview>>()

  data.forEach { (yearMonth, days) ->
    val firstDayOfMonth = days.first().date
    val emptyDaysCount = (firstDayOfMonth.dayOfWeek.value % 7)
    val placeholders: List<ActivityOverview> =
        List(emptyDaysCount) { ActivityOverview(firstDayOfMonth, -1) }
    alignedData[yearMonth] = placeholders + days
  }

  return alignedData
}

@Composable
private fun <T> LazyGridFor(items: List<T>, numCols: Int, itemContent: @Composable (T) -> Unit) {
  val numRows = (items.size + numCols - 1) / numCols
  Column {
    for (rowIndex in 0 until numRows) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (colIndex in 0 until numCols) {
          val itemIndex = rowIndex * numCols + colIndex
          Box(modifier = Modifier.weight(1f)) {
            if (itemIndex < items.size) {
              itemContent(items[itemIndex])
            }
          }
        }
      }
    }
  }
}
