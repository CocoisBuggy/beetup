package com.coco.beetup.ui.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.core.data.unixDay
import com.coco.beetup.ui.components.MorphFab
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.components.activity.ActivityList
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val exerciseCategories by viewModel.allExercises.collectAsState(initial = emptyList())
  val state = rememberBeetHomeState()

  val activityGroups by
      viewModel.activityGroupsForDay(state.date.unixDay()).collectAsState(initial = emptyList())
  val activityOverview by viewModel.activityOverview().collectAsState(initial = null)
  val exerciseDateOverview by viewModel.exerciseDateOverview().collectAsState(initial = null)
  val bannerDates by viewModel.getBannerDates().collectAsState(initial = emptyList())

  val selectedActivityGroups by
      remember(activityGroups, state.selectedItems) {
        derivedStateOf { activityGroups.filter { it.key in state.selectedItems }.toSet() }
      }

  val onDeleteSelected = {
    val itemsToDelete = activityGroups.filter { it.key in state.selectedItems }
    viewModel.deleteActivity(itemsToDelete.flatMap { outer -> outer.logs.map { it.log } })
    state.clearSelection()
  }

  BeetHomeDialogs(
      state = state,
      activityGroups = activityGroups,
      viewModel = viewModel,
      exerciseCategories = exerciseCategories,
  )

  Scaffold(
      topBar = {
        if (state.multiSelectionEnabled) {
          SelectionAppBar(
              selectedItemCount = state.selectedItems.size,
              onClearSelection = state::clearSelection,
              onDeleteSelected = onDeleteSelected,
          )
        } else {
          BeetTopBar(scope, drawerState)
        }
      },
      floatingActionButton = {
        if (!state.multiSelectionEnabled) {
          MorphFab(
              selectedItems = selectedActivityGroups,
              editMode = state.editMode,
              multiSelectionEnabled = state.multiSelectionEnabled,
              onAddActivityClick = state::startAddActivity,
              onEditModeToggle = { state.editMode = !state.editMode },
              onDeleteSelected = onDeleteSelected,
              onBifurcate = {
                state.selectedExerciseForEntry =
                    exerciseCategories.find { it.id == state.selectedItems.first().exerciseId }
              },
              onMinus = {
                selectedActivityGroups.forEach {
                  it.logs.lastOrNull()?.let { last -> viewModel.deleteActivity(listOf(last.log)) }
                }
              },
              onBannerToggle = {
                selectedActivityGroups.firstOrNull()?.logs?.firstOrNull()?.let { firstLog ->
                  val updatedLog = firstLog.log.copy(banner = !firstLog.log.banner)
                  viewModel.updateLogEntry(updatedLog)
                }
              },
          )
        }
      },
  ) { innerPadding ->
    ActivityList(
        date = state.date,
        modifier = Modifier.padding(innerPadding).padding(6.dp),
        todaysActivity = activityGroups,
        activityDates = activityOverview,
        exerciseDates = exerciseDateOverview,
        bannerDates = bannerDates.map { java.time.LocalDate.ofEpochDay(it.toLong()) }.toSet(),
        selectedItems = state.selectedItems,
        onToggleSelection = state::toggleSelection,
        onToggleMultiSelection = state::toggleMultiSelection,
        viewModel = viewModel,
        onDateChange = { state.date = it })
  }
}
