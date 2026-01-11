package com.coco.beetup.ui.components.schedule

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.coco.beetup.core.data.BeetExerciseSchedule
import com.coco.beetup.core.data.ReminderStrength
import com.coco.beetup.core.data.ScheduleKind
import com.coco.beetup.ui.viewmodel.BeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(
    viewModel: BeetViewModel,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    scheduleToEdit: BeetExerciseSchedule? = null
) {
  val exercises by viewModel.allExercises.collectAsState(initial = emptyList())

  var selectedExercise by
      remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.activityId ?: 0) }
  var scheduleKind by
      remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.kind ?: ScheduleKind.MONOTONIC) }
  var reminderStrength by
      remember(scheduleToEdit) {
        mutableStateOf(scheduleToEdit?.reminder ?: ReminderStrength.IN_APP)
      }
  var monotonicDays by
      remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.monotonicDays?.toString() ?: "1") }
  var dayOfWeek by
      remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.dayOfWeek?.toString() ?: "1") }
  var followsExercise by
      remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.followsExercise ?: 0) }
  var message by remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.message ?: "") }

  var expandedExercise by remember { mutableStateOf(false) }
  var expandedReminder by remember { mutableStateOf(false) }
  var expandedFollowsExercise by remember { mutableStateOf(false) }

  // Permission handling
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
  var showPermissionRationaleDialog by remember { mutableStateOf(false) }

  val notificationPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        prefs.edit { putBoolean("notifications_enabled", isGranted) }

        if (isGranted) {
          prefs.edit { remove("notification_permission_denied") }
          Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
          prefs.edit { putBoolean("notification_permission_denied", true) }
          Toast.makeText(context, "Notifications require permission to work", Toast.LENGTH_LONG)
              .show()
        }
      }

  // Check if schedule requires notification permission
  val requiresNotificationPermission = reminderStrength != ReminderStrength.IN_APP

  if (!showDialog) return

  // Permission rationale dialog
  if (showPermissionRationaleDialog) {
    AlertDialog(
        onDismissRequest = { showPermissionRationaleDialog = false },
        title = { Text("Permission Required") },
        text = {
          Text(
              "To create a schedule with notifications, you need to grant notification permission. Go to Settings > Apps > Beetup > Permissions and enable Notifications.")
        },
        confirmButton = {
          TextButton(
              onClick = {
                showPermissionRationaleDialog = false
                val intent =
                    android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                      data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                context.startActivity(intent)
              }) {
                Text("Open Settings")
              }
        },
        dismissButton = {
          TextButton(onClick = { showPermissionRationaleDialog = false }) { Text("Cancel") }
        })
  }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            text = if (scheduleToEdit != null) "Edit Schedule" else "Create Schedule",
            fontWeight = FontWeight.Medium)
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Exercise Selection
              ExposedDropdownMenuBox(
                  expanded = expandedExercise, onExpandedChange = { expandedExercise = it }) {
                    OutlinedTextField(
                        value =
                            exercises.find { it.id == selectedExercise }?.exerciseName
                                ?: "Select exercise",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExercise)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Exercise") })
                    ExposedDropdownMenu(
                        expanded = expandedExercise,
                        onDismissRequest = { expandedExercise = false }) {
                          exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.exerciseName) },
                                onClick = {
                                  selectedExercise = exercise.id
                                  expandedExercise = false
                                })
                          }
                        }
                  }

              // Schedule Kind Selection
              Text(
                  text = "Schedule Type",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Medium)

              ScheduleKind.entries.forEach { kind ->
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .selectable(
                                selected = scheduleKind == kind, onClick = { scheduleKind = kind })
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      RadioButton(
                          selected = scheduleKind == kind, onClick = { scheduleKind = kind })
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                          text =
                              when (kind) {
                                ScheduleKind.MONOTONIC -> "Every X days"
                                ScheduleKind.DAY_OF_WEEK -> "Day of week"
                                ScheduleKind.FOLLOWS_EXERCISE -> "After another exercise"
                              })
                    }
              }

              // Schedule-specific fields
              when (scheduleKind) {
                ScheduleKind.MONOTONIC -> {
                  OutlinedTextField(
                      value = monotonicDays,
                      onValueChange = { monotonicDays = it },
                      label = { Text("Days between exercises") },
                      modifier = Modifier.fillMaxWidth())
                }
                ScheduleKind.DAY_OF_WEEK -> {
                  OutlinedTextField(
                      value = dayOfWeek,
                      onValueChange = { dayOfWeek = it },
                      label = { Text("Day of week (1-7)") },
                      modifier = Modifier.fillMaxWidth())
                }
                ScheduleKind.FOLLOWS_EXERCISE -> {
                  ExposedDropdownMenuBox(
                      expanded = expandedFollowsExercise,
                      onExpandedChange = { expandedFollowsExercise = it }) {
                        OutlinedTextField(
                            value =
                                exercises.find { it.id == followsExercise }?.exerciseName
                                    ?: "Select exercise",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                              ExposedDropdownMenuDefaults.TrailingIcon(
                                  expanded = expandedFollowsExercise)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Follow this exercise") })
                        ExposedDropdownMenu(
                            expanded = expandedFollowsExercise,
                            onDismissRequest = { expandedFollowsExercise = false }) {
                              exercises
                                  .filter { it.id != selectedExercise }
                                  .forEach { exercise ->
                                    DropdownMenuItem(
                                        text = { Text(exercise.exerciseName) },
                                        onClick = {
                                          followsExercise = exercise.id
                                          expandedFollowsExercise = false
                                        })
                                  }
                            }
                      }
                }
              }

              // Reminder Strength
              ExposedDropdownMenuBox(
                  expanded = expandedReminder, onExpandedChange = { expandedReminder = it }) {
                    OutlinedTextField(
                        value =
                            when (reminderStrength) {
                              ReminderStrength.IN_APP -> "In-app reminder"
                              ReminderStrength.NOTIFICATION -> "Notification"
                              ReminderStrength.ANNOYING_NOTIFICATION -> "Annoying notification"
                            },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReminder)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Reminder Type") })
                    ExposedDropdownMenu(
                        expanded = expandedReminder,
                        onDismissRequest = { expandedReminder = false }) {
                          ReminderStrength.entries.forEach { strength ->
                            DropdownMenuItem(
                                text = {
                                  Text(
                                      when (strength) {
                                        ReminderStrength.IN_APP -> "In-app reminder"
                                        ReminderStrength.NOTIFICATION -> "Notification"
                                        ReminderStrength.ANNOYING_NOTIFICATION ->
                                            "Annoying notification"
                                      })
                                },
                                onClick = {
                                  reminderStrength = strength
                                  expandedReminder = false
                                })
                          }
                        }
                  }

              // Custom Message
              OutlinedTextField(
                  value = message,
                  onValueChange = { message = it },
                  label = { Text("Custom message (optional)") },
                  modifier = Modifier.fillMaxWidth(),
                  maxLines = 3)
            }
      },
      confirmButton = {
        Button(
            onClick = {
              try {
                // Check for notification permission if needed
                if (requiresNotificationPermission) {
                  val hasPermission =
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                      } else {
                        true // Android < 13 doesn't need this permission
                      }

                  if (!hasPermission) {
                    val wasDenied = prefs.getBoolean("notification_permission_denied", false)
                    if (wasDenied) {
                      showPermissionRationaleDialog = true
                    } else {
                      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    return@Button
                  }
                }

                val schedule =
                    BeetExerciseSchedule(
                        id = scheduleToEdit?.id ?: 0,
                        activityId = selectedExercise,
                        kind = scheduleKind,
                        reminder = reminderStrength,
                        followsExercise =
                            if (scheduleKind == ScheduleKind.FOLLOWS_EXERCISE) followsExercise
                            else null,
                        dayOfWeek =
                            if (scheduleKind == ScheduleKind.DAY_OF_WEEK)
                                dayOfWeek.toIntOrNull() ?: 1
                            else null,
                        monotonicDays =
                            if (scheduleKind == ScheduleKind.MONOTONIC)
                                monotonicDays.toIntOrNull() ?: 1
                            else null,
                        message = message.ifBlank { null })

                if (scheduleToEdit != null) {
                  viewModel.updateSchedule(schedule)
                } else {
                  viewModel.insertSchedule(schedule)
                }
                onDismiss()
              } catch (e: Exception) {
                // Handle validation errors
                Log.e("CreateScheduleDialog", "Validation error in schedule creation: $e")
              }
            }) {
              Text(if (scheduleToEdit != null) "Update" else "Create")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
