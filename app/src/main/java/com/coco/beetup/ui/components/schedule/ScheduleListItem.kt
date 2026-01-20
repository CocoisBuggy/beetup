package com.coco.beetup.ui.components.schedule

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseSchedule
import com.coco.beetup.core.data.ReminderStrength
import com.coco.beetup.core.data.ScheduleKind
import com.coco.beetup.service.ExerciseNotificationManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ScheduleListItem(
    schedule: BeetExerciseSchedule,
    exercise: BeetExercise,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    context: Context? = null
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text =
                        when (schedule.kind) {
                          ScheduleKind.MONOTONIC -> "Every ${schedule.monotonicDays} days"
                          ScheduleKind.DAY_OF_WEEK -> "Day ${schedule.dayOfWeek}"
                          ScheduleKind.FOLLOWS_EXERCISE ->
                              "After exercise ID ${schedule.followsExercise}"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                schedule.reminder?.let { reminder ->
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text =
                          when (reminder) {
                            ReminderStrength.IN_APP -> "In-app reminder"
                            ReminderStrength.NOTIFICATION -> "Notification"
                            ReminderStrength.ANNOYING_NOTIFICATION -> "Annoying notification"
                          },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary)
                }

                schedule.message?.let { message ->
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text = "\"$message\"",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Show next notification time if notifications are enabled
                context?.let { ctx ->
                  val prefs = ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                  val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
                  var scheduledTime by remember { mutableStateOf<LocalDateTime?>(null) }

                  // Load actual scheduled time from WorkManager
                  LaunchedEffect(schedule.id) {
                    scheduledTime =
                        try {
                          com.coco.beetup.service.ExerciseNotificationManager
                              .getScheduledNotificationTime(ctx, schedule.id)
                        } catch (e: Exception) {
                          null
                        }
                  }

                  when {
                    schedule.reminder == ReminderStrength.IN_APP -> {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "In-app reminder only",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    !notificationsEnabled -> {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "Notifications disabled",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    scheduledTime != null -> {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "Next: ${formatNextNotificationTime(scheduledTime!!)}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.secondary)
                    }
                    else -> {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "No notification scheduled",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete schedule")
              }
            }
      }
}

fun formatNextNotificationTime(nextTime: LocalDateTime): String {
  val now = LocalDateTime.now()
  val daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), nextTime.toLocalDate())
  val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")

  return when {
    daysUntil == 0L -> "Today at ${nextTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
    daysUntil == 1L -> "Tomorrow at ${nextTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
    daysUntil <= 7L -> nextTime.format(formatter)
    else -> nextTime.format(DateTimeFormatter.ofPattern("MMM d"))
  }
}
