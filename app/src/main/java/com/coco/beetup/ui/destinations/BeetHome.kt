package com.coco.beetup.ui.destinations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.R
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.components.CategorySelectionDialog
import com.coco.beetup.ui.components.DeletableExerciseEntry
import com.coco.beetup.ui.components.ExerciseLogDetailsDialog
import com.coco.beetup.ui.components.FloatingActivityToolbar
import com.coco.beetup.ui.components.ResistanceSelectionDialog
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.components.WelcomeCard
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
  val todaysExercise by viewModel.activityGroupsForDay(day).collectAsState(initial = emptyList())
  val allResistances by viewModel.allResistances.collectAsState(initial = emptyList())

  var editMode by remember { mutableStateOf(false) }
  var selectedItems by remember { mutableStateOf<Set<ActivityGroup>>(emptySet()) }
  var multiSelectionEnabled by remember { mutableStateOf(false) }
  var showCategoryDialog by remember { mutableStateOf(false) }
  var selectedExerciseForEntry by remember { mutableStateOf<BeetExercise?>(null) }
  var magnitudeForEntry by remember { mutableStateOf<Int?>(null) }

  val onDeleteSelected = {
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
        if (!multiSelectionEnabled && selectedItems.isNotEmpty()) {
          AnimatedVisibility(
              visible = true,
              enter = slideInHorizontally { it } + fadeIn(),
              exit = slideOutHorizontally { it } + fadeOut(),
          ) {
            FloatingActivityToolbar({ editMode = !editMode }, editMode)
          }
        } else {
          FloatingActionButton(onClick = { showCategoryDialog = true }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.add_exercise_entry),
            )
          }
        }
      },
  ) { innerPadding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
    ) {
      item { WelcomeCard(nav) }

      items(todaysExercise, key = { it.exercise.id }) { item ->
        val isSelected = item in selectedItems

        DeletableExerciseEntry(
            viewModel = viewModel,
            day = day,
            item = item,
            isSelected = isSelected,
            onToggleSelection = {
              selectedItems =
                  if (multiSelectionEnabled) {
                    if (isSelected) {
                      selectedItems - item
                    } else {
                      selectedItems + item
                    }
                  } else {
                    if (item in selectedItems) emptySet() else setOf(item)
                  }
            },
            onToggleMultiSelection = { multiSelectionEnabled = !multiSelectionEnabled },
        )
      }
    }
  }
}
