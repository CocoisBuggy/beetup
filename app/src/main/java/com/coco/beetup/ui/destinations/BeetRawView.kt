package com.coco.beetup.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.core.data.BeetMagnitude
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeetRawView(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val allLogs by viewModel.allLogs.collectAsState(initial = null)
  val allExercises by viewModel.allExercises.collectAsState(initial = emptyList())
  val allMagnitudes by viewModel.allMagnitudes().collectAsState(initial = emptyList())

  val exercisesById = remember(allExercises) { allExercises.associateBy { it.id } }
  val magnitudesById = remember(allMagnitudes) { allMagnitudes.associateBy { it.id } }

  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
      content = { innerPadding ->
        if (allLogs == null) {
          LoadingIndicator()
        }
        allLogs?.let { logs ->
          LazyColumn(Modifier.padding(innerPadding)) {
            items(
                count = logs.size,
                key = { logs[it].id },
                itemContent = { index ->
                  val log = logs[index]
                  val exercise = exercisesById[log.exerciseId]
                  val magnitude = exercise?.let { magnitudesById[it.magnitudeKind] }
                  LogCard(log, exercise, magnitude)
                },
            )
          }
        }
      },
  )
}

@Composable
fun LogCard(log: BeetExerciseLog, exercise: BeetExercise?, magnitude: BeetMagnitude?) {
  val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }

  Card(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row {
            Text(
                text = "Log #${log.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(
                text = "Ex ID: ${log.exerciseId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = exercise?.exerciseName ?: "Unknown Exercise",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)
          if (exercise != null) {
            Text(
                text = exercise.exerciseDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
              LabelValue("Date", log.logDate.format(dateFormatter))
              LabelValue("Unix Day", log.logDay.toString())
            }
            Column(modifier = Modifier.weight(1f)) {
              val magText =
                  if (magnitude != null) {
                    "${log.magnitude} ${magnitude.unit}"
                  } else {
                    log.magnitude.toString()
                  }
              LabelValue("Magnitude", magText)
              LabelValue("Difficulty", log.difficulty?.name ?: "N/A")
            }
          }

          if (!log.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            LabelValue("Comment", log.comment)
          }
        }
      }
}

@Composable
fun LabelValue(label: String, value: String) {
  Column(modifier = Modifier.padding(bottom = 6.dp)) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary)
    Text(text = value, style = MaterialTheme.typography.bodyMedium)
  }
}
