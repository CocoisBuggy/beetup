package com.coco.beetup.ui.components.activity

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.PlusOne
import com.coco.beetup.core.data.ResistanceConversion
import com.coco.beetup.ui.components.grain.ResistanceIcon
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.delay

private data class PlusOneAnimation(val id: String = UUID.randomUUID().toString())

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityEntry(
    viewModel: BeetViewModel,
    activity: ActivityGroup,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
  var plusOneAnimations by remember { mutableStateOf<List<PlusOneAnimation>>(emptyList()) }

  if (activity.logs.isEmpty()) {
    ListItem(
        leadingContent = { LoadingIndicator() },
        headlineContent = { Text(activity.exercise.exerciseName) },
    )
  } else {

    ListItem(
        headlineContent = { Text(activity.exercise.exerciseName) },
        supportingContent = {
          Column {
            val text = mutableListOf<String>()

            activity.logs.firstOrNull()?.let {
              text.add("${it.log.magnitude} ${activity.magnitude.unit}")
            }

            text.add("${activity.logs.size} sets")
            Row {
              Text(text.joinToString(", "))
              Spacer(Modifier.width(4.dp))

              if (plusOneAnimations.isNotEmpty()) {
                Box(modifier = Modifier.width(32.dp)) {
                  plusOneAnimations.forEach { animation ->
                    key(animation.id) {
                      PlusOne(
                          onFinished = {
                            plusOneAnimations =
                                plusOneAnimations.filterNot { it.id == animation.id }
                          })
                    }
                  }
                }
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              activity.logs.first().resistances.forEach { res ->
                AssistChip(
                    {},
                    leadingIcon = {
                      ResistanceIcon[res.extra.id]?.let { icon ->
                        Icon(
                            icon,
                            contentDescription = "Icon for ${res.extra.name}",
                        )
                      }
                    },
                    label = {
                      Text(
                          ResistanceConversion[res.extra.id]?.invoke(res)
                              ?: "${res.entry.resistanceValue}${res.extra.unit}")
                    },
                    enabled = false)
              }

              if (activity.logs.first().resistances.isEmpty()) {
                AssistChip({}, label = { Text("No Resistance") }, enabled = false)
              }
            }

            if (isSelected) {
              var currentNow by remember { mutableStateOf(LocalDateTime.now()) }
              LaunchedEffect(Unit) {
                while (true) {
                  delay(1000)
                  currentNow = LocalDateTime.now()
                }
              }

              val sortedLogs = remember(activity.logs) { activity.logs.sortedBy { it.log.logDate } }
              val hasTimeData =
                  remember(sortedLogs) {
                    sortedLogs.isNotEmpty() &&
                        sortedLogs.any { it.log.logDate.toLocalTime() != LocalTime.MIN }
                  }

              if (hasTimeData) {
                val intervals =
                    remember(sortedLogs) {
                      sortedLogs.zipWithNext { a, b ->
                        Duration.between(a.log.logDate, b.log.logDate)
                      }
                    }

                if (intervals.isNotEmpty()) {
                  Spacer(Modifier.height(8.dp))
                  Text(
                      text =
                          "Rest: " +
                              intervals.joinToString(", ") {
                                val mm = it.toMinutes()
                                val ss = it.minusMinutes(mm).seconds
                                if (mm > 0) "${mm}m${ss}s" else "${ss}s"
                              },
                      style = MaterialTheme.typography.bodySmall)

                  if (intervals.size > 1) {
                    val maxDuration = intervals.maxOf { it.seconds }
                    val minDuration = intervals.minOf { it.seconds }
                    val range = (maxDuration - minDuration).coerceAtLeast(1)
                    val color = MaterialTheme.colorScheme.primary

                    Canvas(modifier = Modifier.height(30.dp).fillMaxWidth().padding(top = 4.dp)) {
                      val points =
                          intervals.mapIndexed { index, duration ->
                            val x = size.width * index / (intervals.size - 1).coerceAtLeast(1)
                            val normalized = (duration.seconds - minDuration).toFloat() / range
                            val y = size.height - (normalized * size.height)
                            Offset(x, y)
                          }

                      points.zipWithNext { a, b ->
                        drawLine(color = color, start = a, end = b, strokeWidth = 2.dp.toPx())
                      }

                      points.forEach { drawCircle(color, radius = 2.dp.toPx(), center = it) }
                    }
                  }
                }

                val lastLog = sortedLogs.last()
                if (lastLog.log.logDate.toLocalDate().isEqual(currentNow.toLocalDate())) {
                  if (intervals.isEmpty()) Spacer(Modifier.height(4.dp))
                  val since = Duration.between(lastLog.log.logDate, currentNow)
                  val mm = since.toMinutes()
                  val ss = since.minusMinutes(mm).seconds
                  Text(
                      "Since last: ${if (mm > 0) "${mm}m${ss}s" else "${ss}s"} ago",
                      style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        },
        leadingContent = {
          Icon(Icons.Default.FitnessCenter, contentDescription = "Exercise Icon")
        },
        trailingContent = {
          FilledTonalButton(
              onClick = {
                activity.logs.lastOrNull()?.let { lastLog ->
                  val nextDate =
                      if (lastLog.log.logDate.toLocalDate() == LocalDate.now()) LocalDateTime.now()
                      else lastLog.log.logDate.toLocalDate().atStartOfDay()

                  if (nextDate == null) return@let

                  plusOneAnimations += PlusOneAnimation()
                  viewModel.insertActivityAndResistances(
                      activity.logs.last().log.copy(id = 0, logDate = nextDate),
                      activity.logs.last().resistances.map { it.entry })
                }
              }) {
                Text("+1")
              }
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier =
            modifier
                .padding(vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.medium)
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow)))
  }
}
