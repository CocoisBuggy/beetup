package com.coco.beetup.ui.destinations

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.R
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.components.CategorySelectionDialog
import com.coco.beetup.ui.components.DeletableExerciseEntry
import com.coco.beetup.ui.components.ExerciseLogDetailsDialog
import com.coco.beetup.ui.components.MorphFab
import com.coco.beetup.ui.components.ResistanceSelectionDialog
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome(
    nav: NavHostController,
    viewModel: BeetViewModel,
) {
  val day = (Date().time / 86_400_000L).toInt()
  val exerciseCategories by viewModel.allExercises.collectAsState(initial = emptyList())
  val todaysActivity by viewModel.activityGroupsForDay(day).collectAsState(initial = emptyList())

  var editMode by remember { mutableStateOf(false) }
  var selectedItems by remember { mutableStateOf<Set<ActivityKey>>(emptySet()) }
  var multiSelectionEnabled by remember { mutableStateOf(false) }
  var showCategoryDialog by remember { mutableStateOf(false) }
  var selectedExerciseForEntry by remember { mutableStateOf<BeetExercise?>(null) }
  var magnitudeForEntry by remember { mutableStateOf<Int?>(null) }
  val selectedActivityGroups by
      remember(todaysActivity, selectedItems) {
        derivedStateOf {
          todaysActivity.filter { it.key in selectedItems }.toSet()
        }
      }

  val onDeleteSelected = {
    val itemsToDelete = todaysActivity.filter { it.key in selectedItems }
    Log.i("BeetHome", "Deleting ${itemsToDelete.size} items")
    Log.d("BeetHome", "Deleting $itemsToDelete")

    viewModel.deleteActivity(itemsToDelete.flatMap { outer -> outer.logs.map { it.log } })
    selectedItems = emptySet()
    multiSelectionEnabled = false
  }

  if (showCategoryDialog) {
    CategorySelectionDialog(
        categories = exerciseCategories,
        onCategorySelected = { category ->
          selectedExerciseForEntry = category
          showCategoryDialog = false
        },
        onDismiss = { showCategoryDialog = false },
    )
  }

  selectedExerciseForEntry?.let { exercise ->
    val magnitude by viewModel.magnitudeFor(exercise.magnitudeKind).collectAsState(initial = null)
    val validResistances by
        viewModel.validResistancesFor(exercise.id).collectAsState(initial = emptyList())

    if (magnitudeForEntry == null) {
      magnitude?.let {
        ExerciseLogDetailsDialog(
            exercise = exercise,
            magnitudeName = it.name,
            magnitudeUnit = it.unit,
            onConfirm = { magnitude -> magnitudeForEntry = magnitude },
            onDismiss = { selectedExerciseForEntry = null },
        )
      }
    } else {
      ResistanceSelectionDialog(
          resistances = validResistances,
          onConfirm = { selectedResistances ->
            val newExerciseLog =
                BeetExerciseLog(
                    exerciseId = exercise.id,
                    magnitude = magnitudeForEntry!!,
                )
            val newResistances =
                selectedResistances.map {
                  BeetActivityResistance(
                      activityId = 0, // This will be overridden by the repository
                      resistanceKind = it.key,
                      resistanceValue = it.value)
                }
            viewModel.insertActivityAndResistances(newExerciseLog, newResistances)
            selectedExerciseForEntry = null
            magnitudeForEntry = null
          },
          onDismiss = { selectedExerciseForEntry = null },
      )
    }
  }

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
          TopAppBar(
              title = { Text(stringResource(id = R.string.todays_activity)) },
          )
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
    ) {
      items(todaysActivity) { item ->
        val isSelected = item.key in selectedItems

        DeletableExerciseEntry(
            viewModel = viewModel,
            item = item,
            isSelected = isSelected,
            onToggleSelection = {
              selectedItems =
                  if (multiSelectionEnabled) {
                    if (isSelected) {
                      selectedItems - item.key
                    } else {
                      selectedItems + item.key
                    }
                  } else {
                    if (item.key in selectedItems) emptySet() else setOf(item.key)
                  }
            },
            onToggleMultiSelection = { multiSelectionEnabled = !multiSelectionEnabled },
        )
      }
    }
  }
}
