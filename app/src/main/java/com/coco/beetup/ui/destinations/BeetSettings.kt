package com.coco.beetup.ui.destinations

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.coco.beetup.service.ExerciseNotificationManager
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeetSettings(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
  var showClearDialog by remember { mutableStateOf(false) }
  var notificationsEnabled by remember {
    mutableStateOf(prefs.getBoolean("notifications_enabled", true))
  }
  var showTimePicker by remember { mutableStateOf(false) }
  var showPermissionRationaleDialog by remember { mutableStateOf(false) }
  val timePickerState = rememberTimePickerState(9, 0, true)

  val exportLauncher =
      rememberLauncherForActivityResult(
          ActivityResultContracts.CreateDocument("application/x-sqlite3")) { uri ->
            if (uri != null) {
              scope.launch {
                viewModel.checkpoint() // Flush WAL
                exportDatabase(context, uri)
              }
            }
          }

  val importLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
          scope.launch {
            viewModel.closeDatabase()

            if (importDatabase(context, uri)) {
              Toast.makeText(context, "Import successful. Restarting...", Toast.LENGTH_LONG).show()
              // Restart app
              val packageManager = context.packageManager
              val intent = packageManager.getLaunchIntentForPackage(context.packageName)
              val componentName = intent?.component
              val mainIntent = Intent.makeRestartActivityTask(componentName)
              context.startActivity(mainIntent)
              Runtime.getRuntime().exit(0)
            } else {
              Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            }
          }
        }
      }

  val notificationPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        Log.d("BeetSettings", "Permission result: $isGranted")
        prefs.edit { putBoolean("notifications_enabled", isGranted) }
        notificationsEnabled = isGranted

        if (isGranted) {
          Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
          // Clear the denied flag when permission is granted
          prefs.edit { remove("notification_permission_denied") }
        } else {
          // Mark that permission was denied
          prefs.edit { putBoolean("notification_permission_denied", true) }
          Toast.makeText(context, "Notifications require permission to work", Toast.LENGTH_LONG)
              .show()
        }
      }

  // Function to check if we should show permission rationale
  val shouldShowRequestPermissionRationale = {
    // This needs to be called from an Activity/Fragment context
    // For now, we'll assume if permission was denied and switch was toggled, it's permanent
    false
  }

  if (showPermissionRationaleDialog) {
    AlertDialog(
        onDismissRequest = { showPermissionRationaleDialog = false },
        title = { Text("Permission Required") },
        text = {
          Text(
              "You've permanently denied notification permission. To enable notifications, go to Settings > Apps > Beetup > Permissions and enable Notifications.")
        },
        confirmButton = {
          TextButton(
              onClick = {
                showPermissionRationaleDialog = false
                // Open app settings
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                      data = Uri.fromParts("package", context.packageName, null)
                    }
                context.startActivity(intent)
              }) {
                Text("Open Settings")
              }
        },
        dismissButton = {
          TextButton(
              onClick = {
                showPermissionRationaleDialog = false
                prefs.edit { putBoolean("notifications_enabled", false) }
                notificationsEnabled = false
              }) {
                Text("Cancel")
              }
        })
  }

  if (showClearDialog) {
    AlertDialog(
        onDismissRequest = { showClearDialog = false },
        title = { Text("Clear Data") },
        text = { Text("Are you sure you want to delete all data? This cannot be undone.") },
        confirmButton = {
          TextButton(
              onClick = {
                scope.launch {
                  viewModel.clearAllData(context)
                  showClearDialog = false
                  Toast.makeText(context, "Data cleared", Toast.LENGTH_SHORT).show()
                }
              }) {
                Text("Delete")
              }
        },
        dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } })
  }

  if (showTimePicker) {
    AlertDialog(
        onDismissRequest = { showTimePicker = false },
        title = { Text("Set Reminder Time") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
          TextButton(
              onClick = {
                showTimePicker = false
                Toast.makeText(context, "Reminder time updated", Toast.LENGTH_SHORT).show()
                // TODO: Update notification time, and reschedule existing notifications that are
                // now scheduled for the wrong time
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } })
  }

  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
  ) { innerPadding ->
    Column(Modifier.padding(innerPadding).padding(16.dp)) {
      Text("Settings", style = MaterialTheme.typography.displayLarge)
      Spacer(Modifier.height(24.dp))

      Text("Data Management", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))

      ButtonGroup(
          overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
          },
          verticalAlignment = Alignment.Top,
          modifier = Modifier.fillMaxWidth(),
      ) {
        clickableItem(
            onClick = { exportLauncher.launch("beet_backup.db") },
            label = "Export",
            icon = { Icon(Icons.Default.SaveAlt, "Export Icon") })

        clickableItem(
            onClick = { importLauncher.launch(arrayOf("*/*")) },
            label = "Import",
            icon = { Icon(Icons.Default.Download, "Import Icon") })
      }

      HorizontalDivider(Modifier.padding(top = 24.dp, bottom = 24.dp))

      Text("Notification Settings", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.height(8.dp))

      Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text("Enable Notifications")
              Switch(
                  checked = notificationsEnabled,
                  onCheckedChange = { newValue ->
                    if (newValue) {
                      // Check if we already have permission
                      val hasPermission =
                          ContextCompat.checkSelfPermission(
                              context, Manifest.permission.POST_NOTIFICATIONS) ==
                              PackageManager.PERMISSION_GRANTED

                      if (hasPermission) {
                        prefs.edit { putBoolean("notifications_enabled", true) }
                        notificationsEnabled = true
                      } else {
                        Log.d(
                            "BeetSettings",
                            "User is missing the POST_NOTIFICATIONS permission, we will ask for it")

                        // Check if user has previously denied this permission
                        val wasDenied = prefs.getBoolean("notification_permission_denied", false)

                        if (wasDenied) {
                          // User has denied before, show rationale dialog
                          showPermissionRationaleDialog = true
                        } else {
                          // First time asking, request permission
                          notificationPermissionLauncher.launch(
                              Manifest.permission.POST_NOTIFICATIONS)
                        }
                      }
                    } else {
                      // User disabled notifications
                      prefs.edit { putBoolean("notifications_enabled", false) }
                      notificationsEnabled = false
                      ExerciseNotificationManager.cancelAllNotifications(context)
                    }
                  })
            }

        if (notificationsEnabled) {
          Spacer(Modifier.height(8.dp))
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Reminder Time: ${String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)}")
                Button(onClick = { showTimePicker = true }) { Text("Change") }
              }
        }
      }

      HorizontalDivider(Modifier.padding(top = 24.dp, bottom = 24.dp))

      Button(
          onClick = { showClearDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DeleteSweep, "Clear All Data")
            Text("Clear Data")
          }
    }
  }
}

suspend fun exportDatabase(context: Context, uri: Uri) {
  withContext(Dispatchers.IO) {
    try {
      val dbFile = context.getDatabasePath("beet_database")
      if (dbFile.exists()) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
          FileInputStream(dbFile).use { input -> input.copyTo(output) }
        }
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
        }
      } else {
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "Database not found", Toast.LENGTH_SHORT).show()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}

suspend fun importDatabase(context: Context, uri: Uri): Boolean {
  return withContext(Dispatchers.IO) {
    try {
      val dbFile = context.getDatabasePath("beet_database")
      // Ensure db folder exists
      dbFile.parentFile?.mkdirs()

      val walFile = File(dbFile.path + "-wal")
      val shmFile = File(dbFile.path + "-shm")
      if (walFile.exists()) walFile.delete()
      if (shmFile.exists()) shmFile.delete()

      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(dbFile).use { output -> input.copyTo(output) }
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}
