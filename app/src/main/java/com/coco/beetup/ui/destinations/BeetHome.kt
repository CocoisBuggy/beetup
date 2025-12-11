package com.coco.beetup.ui.destinations

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.ui.components.ActivityList
import com.coco.beetup.ui.components.MorphFab
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val day = (Date().time / 86_400_000L).toInt()
  val todaysActivity by viewModel.activityGroupsForDay(day).collectAsState(initial = emptyList())
  val activityOverview by viewModel.activityOverview().collectAsState(initial = null)
  val exerciseDateOverview by viewModel.exerciseDateOverview().collectAsState(initial = null)

  var editMode by remember { mutableStateOf(false) }
  var selectedItems by remember { mutableStateOf<Set<ActivityKey>>(emptySet()) }
  var multiSelectionEnabled by remember { mutableStateOf(false) }
  var showCategoryDialog by remember { mutableStateOf(false) }
  var selectedExerciseForEntry by remember { mutableStateOf<BeetExercise?>(null) }
  var magnitudeForEntry by remember { mutableStateOf<Int?>(null) }
  val selectedActivityGroups by
      remember(todaysActivity, selectedItems) {
        derivedStateOf { todaysActivity.filter { it.key in selectedItems }.toSet() }
      }

  val onDeleteSelected = {
    val itemsToDelete = todaysActivity.filter { it.key in selectedItems }
    Log.i("BeetHome", "Deleting ${"$"}{itemsToDelete.size} items")
    Log.d("BeetHome", "Deleting ${"$"}itemsToDelete")

    viewModel.deleteActivity(itemsToDelete.flatMap { outer -> outer.logs.map { it.log } })
    selectedItems = emptySet()
    multiSelectionEnabled = false
  }

  BeetHomeDialogs(
      viewModel = viewModel,
      showCategoryDialog = showCategoryDialog,
      onDismissCategoryDialog = { showCategoryDialog = false },
      selectedExerciseForEntry = selectedExerciseForEntry,
      onExerciseSelected = { exercise ->
        selectedExerciseForEntry = exercise
        showCategoryDialog = false
      },
      magnitudeForEntry = magnitudeForEntry,
      onMagnitudeSet = { magnitude -> magnitudeForEntry = magnitude },
      onDismissExerciseDetails = { selectedExerciseForEntry = null },
  )

  Scaffold(
      topBar = {
        if (multiSelectionEnabled) {
          SelectionAppBar(
              selectedItemCount = selectedItems.size,
              onClearSelection = {
                selectedItems = emptySet()
                multiSelectionEnabled = false
              },
              onDeleteSelected = onDeleteSelected,
          )
        } else {
          BeetTopBar(scope, drawerState)
        }
      },
      floatingActionButton = {
        if (!multiSelectionEnabled) {
          MorphFab(
              selectedItems = selectedActivityGroups,
              editMode = editMode,
              multiSelectionEnabled = multiSelectionEnabled,
              onAddActivityClick = { showCategoryDialog = true },
              onEditModeToggle = { editMode = !editMode },
              onDeleteSelected = onDeleteSelected,
              onBifurcate = {},
              onMinus = {
                selectedActivityGroups.forEach {
                  it.logs.lastOrNull()?.let { last -> viewModel.deleteActivity(listOf(last.log)) }
                }
              },
          )
        }
      },
  ) { innerPadding ->
    ActivityList(
        modifier = Modifier.padding(innerPadding),
        todaysActivity = todaysActivity,
        activityDates = activityOverview,
        exerciseDates = exerciseDateOverview,
        selectedItems = selectedItems,
        onToggleSelection = {
          selectedItems =
              if (multiSelectionEnabled) {
                if (it in selectedItems) {
                  selectedItems - it
                } else {
                  selectedItems + it
                }
              } else {
                if (it in selectedItems) emptySet() else setOf(it)
              }
        },
        onToggleMultiSelection = { multiSelectionEnabled = !multiSelectionEnabled },
        viewModel = viewModel)
  }
}
