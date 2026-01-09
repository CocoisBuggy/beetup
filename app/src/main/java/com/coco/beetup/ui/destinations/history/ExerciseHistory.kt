package com.coco.beetup.ui.destinations.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.beetup.core.data.ActivityGroupFlatRow
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.ui.viewmodel.BeetViewModel
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme

enum class ViewMode {
  TIMELINE,
  GRAPHS
}

enum class TimePeriod(val days: Int, val displayName: String) {
  ONE_MONTH(30, "1 Month"),
  THREE_MONTHS(90, "3 Months"),
  SIX_MONTHS(180, "6 Months")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExerciseHistory(
    nav: NavController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
) {
  var selectedExercise by remember { mutableStateOf<BeetExercise?>(null) }
  var historyData by remember { mutableStateOf<List<ActivityGroupFlatRow>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  var viewMode by remember { mutableStateOf(ViewMode.TIMELINE) }
  var timePeriod by remember { mutableStateOf(TimePeriod.ONE_MONTH) }

  val exercises by viewModel.allExercises.collectAsState(initial = null)
  val magnitudes by viewModel.allMagnitudes().collectAsState(initial = emptyList())
  val resistances by viewModel.allResistances.collectAsState(initial = emptyList())

  LaunchedEffect(selectedExercise, timePeriod) {
    selectedExercise?.let { exercise ->
      isLoading = true
      val currentDay = (System.currentTimeMillis() / 86_400_000L).toInt()
      val daysAgo = currentDay - timePeriod.days
      historyData = viewModel.getExerciseHistory(exercise.id, daysAgo, currentDay)
      isLoading = false
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Exercise History") },
            navigationIcon = {
              IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
              }
            })
      }) { innerPadding ->
        ProvideVicoTheme(rememberM3VicoTheme()) {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(innerPadding)
                      .padding(16.dp)
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (selectedExercise == null) {
                  ExerciseSelection(exercises) { selectedExercise = it }
                }

                selectedExercise?.let { selected ->
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                              selected.exerciseName,
                              style = MaterialTheme.typography.headlineSmall,
                              fontWeight = FontWeight.Bold)
                           Text(
                               "${historyData.size} records in last ${timePeriod.displayName}",
                               style = MaterialTheme.typography.bodySmall,
                               color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                         Button(onClick = { selectedExercise = null }) { Text("Change") }
                       }

                   // Time Period Selector
                   ButtonGroup(
                       overflowIndicator = { menuState ->
                         ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                       },
                   ) {
                     TimePeriod.entries.forEach { period ->
                       toggleableItem(
                           checked = timePeriod == period,
                           label = period.displayName,
                           onCheckedChange = { timePeriod = period },
                           weight = 1f,
                       )
                     }
                   }

                   // View Mode Selector
                  ButtonGroup(
                      overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                      },
                  ) {
                    ViewMode.entries.forEach { mode ->
                      toggleableItem(
                          checked = viewMode == mode,
                          label = mode.name,
                          onCheckedChange = { viewMode = mode },
                          icon = {
                            Icon(
                                when (mode) {
                                  ViewMode.TIMELINE -> Icons.Default.Timeline
                                  ViewMode.GRAPHS -> Icons.Default.BarChart
                                },
                                contentDescription = null,
                            )
                          },
                          weight = 1f,
                      )
                    }
                  }

                  if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center) {
                          CircularProgressIndicator()
                        }
                  } else if (historyData.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                      Text(
                          "No exercise history found for the last ${timePeriod.displayName}",
                          modifier = Modifier.padding(24.dp),
                          textAlign = TextAlign.Center,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  } else {
                    when (viewMode) {
                      ViewMode.TIMELINE -> TimelineView(historyData, magnitudes, resistances)
                      ViewMode.GRAPHS -> GraphsView(historyData, magnitudes, resistances)
                    }
                  }
                }
              }
        }
      }
}
