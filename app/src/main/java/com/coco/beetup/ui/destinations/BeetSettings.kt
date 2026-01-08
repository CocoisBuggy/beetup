package com.coco.beetup.ui.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
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
  var showClearDialog by remember { mutableStateOf(false) }
  var notificationsEnabled by remember { mutableStateOf(true) }
  var showTimePicker by remember { mutableStateOf(false) }
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
                // Save the time preference - this would need to be implemented
                Toast.makeText(context, "Reminder time updated", Toast.LENGTH_SHORT).show()
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
                  onCheckedChange = {
                    notificationsEnabled = it
                    if (!it) {
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
