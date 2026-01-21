package com.coco.beetup.ui.destinations

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes.Companion.Sunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.core.data.BeetExerciseSchedule
import com.coco.beetup.ui.components.ExerciseHistory
import com.coco.beetup.ui.components.Settings
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.components.schedule.CreateScheduleDialog
import com.coco.beetup.ui.components.schedule.ScheduleListItem
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeetScheduleManager(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
  var searchQuery by remember { mutableStateOf("") }

  val schedules by viewModel.allSchedules.collectAsState(emptyList())
  val exercises by viewModel.allExercises.collectAsState(initial = null)
  var showScheduleDialog by remember { mutableStateOf(false) }
  var scheduleToEdit by remember { mutableStateOf<BeetExerciseSchedule?>(null) }
  val notificationsEnabled = remember { prefs.getBoolean("notifications_enabled", true) }

  val scrollState = rememberScrollState()

  CreateScheduleDialog(
      viewModel,
      showScheduleDialog,
      onDismiss = {
        showScheduleDialog = false
        scheduleToEdit = null
      },
      scheduleToEdit = scheduleToEdit)

  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
      floatingActionButton = {
        FloatingActionButton(onClick = { showScheduleDialog = true }, shape = Sunny.toShape()) {
          Icon(Icons.Default.Add, "Add Icon")
        }
      },
  ) { innerPadding ->
    Column(
        Modifier.fillMaxSize().padding(innerPadding).padding(12.dp).verticalScroll(scrollState),
    ) {
      Text("Schedule Manager", style = MaterialTheme.typography.headlineMedium)

      if (!notificationsEnabled) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notifications are disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    text =
                        "Exercise reminders won't work while notifications are disabled. Enable them in Settings to receive reminders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(top = 4.dp))
                TextButton(onClick = { nav.navigate(Settings) }) { Text("Go to Settings") }
              }
            }
      }

      Button(onClick = { nav.navigate(ExerciseHistory) }, modifier = Modifier.fillMaxWidth()) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp))
        Text("View Exercise History")
      }

      OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.padding(top = 24.dp, bottom = 24.dp).fillMaxWidth(),
          leadingIcon = { Icon(Icons.Default.Search, null) },
          placeholder = { Text("Search Schedules") },
          singleLine = true,
      )

      if (exercises == null) {
        LoadingIndicator()
      }

      exercises?.let { exercises ->
        val exerciseMap = exercises.associateBy { it.id }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          for (schedule in schedules) {
            val exercise = exerciseMap[schedule.exerciseId]
            if (exercise == null) continue

            if (exercise.exerciseName.contains(searchQuery, ignoreCase = true).not()) continue

            ScheduleListItem(
                schedule = schedule,
                exercise = exercise,
                onClick = {
                  scheduleToEdit = schedule
                  showScheduleDialog = true
                },
                onDelete = { viewModel.deleteSchedule(schedule) },
                context = LocalContext.current)
          }
        }
      }
    }
  }
}
