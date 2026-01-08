package com.coco.beetup.ui.components.activity

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Badge
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coco.beetup.R
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ActivityGroupFlatRow
import com.coco.beetup.core.data.MagnitudeConversion
import com.coco.beetup.core.data.PlusOne
import com.coco.beetup.core.data.ResistanceConversion
import com.coco.beetup.ui.components.grain.ResistanceIcon
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.launch

private data class PlusOneAnimation(val id: String = UUID.randomUUID().toString())

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityListItem(
    viewModel: BeetViewModel,
    activity: ActivityGroup,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  var plusOneAnimations by remember { mutableStateOf<List<PlusOneAnimation>>(emptyList()) }
  var stats by remember { mutableStateOf<List<ActivityGroupFlatRow>?>(null) }
  val maxMagnitude by remember { derivedStateOf { stats?.maxByOrNull { it.log.magnitude } } }

  LaunchedEffect(activity) {
    activity.logs.firstOrNull()?.let { firstLog ->
      coroutineScope.launch {
        val date = firstLog.log.logDate.toLocalDate()

        stats =
            viewModel.getExerciseHistory(
                activity.exercise.id,
                date.minusDays(30).toEpochDay().toInt(),
                date.toEpochDay().toInt(),
            )
      }
    }
  }

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              activity.logs.firstOrNull()?.let { entry ->
                Text(
                    MagnitudeConversion[activity.magnitude.id]?.invoke(entry.log.magnitude)
                        ?: "${entry.log.magnitude} ${activity.magnitude.unit}")

                maxMagnitude?.let { maxMag ->
                  val max = maxMag.log.magnitude.toFloat()
                  val next = entry.log.magnitude.toFloat()

                  val percentage = (((next - max) / maxMag.log.magnitude.toFloat()) * 100).toInt()

                  if (percentage == 0) {
                    return@let
                  }

                  Badge(
                      contentColor =
                          if (percentage > 0) MaterialTheme.colorScheme.onPrimaryContainer
                          else MaterialTheme.colorScheme.onErrorContainer,
                      containerColor =
                          if (percentage > 0) MaterialTheme.colorScheme.primaryContainer
                          else MaterialTheme.colorScheme.errorContainer) {
                        Text("${if (percentage > 0) '+' else ""}${percentage}%")
                      }
                }
                Text("${activity.logs.size} sets")
              }

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

            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              activity.logs.first().let { entry ->
                entry.log.rest?.let { restSeconds ->
                  Badge(
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                      contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                        Icon(Icons.Default.Restore, "Rest Icon")
                        Text(formatDuration(Duration.ofSeconds(restSeconds.toLong())))
                      }
                }

                entry.resistances
                    .forEach { res ->
                      Badge(
                          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                          containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            ResistanceIcon[res.extra.id]?.let { icon ->
                              Icon(
                                  icon,
                                  contentDescription = "Icon for ${res.extra.name}",
                              )
                            }
                            Text(
                                ResistanceConversion[res.extra.id]?.invoke(res)
                                    ?: "${res.entry.resistanceValue}${res.extra.unit}")
                          }
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
                    shape = MaterialTheme.shapes.medium))
  }
}

@Composable
private fun formatDuration(duration: Duration): String {
  val hh = duration.toHours()
  val mm = duration.toMinutes() % 60
  val ss = duration.seconds % 60

  return if (hh > 0) {
    stringResource(R.string.duration_hms, hh, mm, ss)
  } else if (mm > 0) {
    stringResource(R.string.duration_ms, mm, ss)
  } else {
    stringResource(R.string.duration_s, ss)
  }
}
