package com.coco.beetup.ui.destinations

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import com.coco.beetup.ui.components.AppDrawer
import com.coco.beetup.ui.components.MorphFab
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome(
    nav: NavHostController,
    viewModel: BeetViewModel,
) {
  val day = (Date().time / 86_400_000L).toInt()
  val todaysActivity by viewModel.activityGroupsForDay(day).collectAsState(initial = emptyList())

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
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

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

  AppDrawer(drawerState = drawerState, scope = scope, navController = nav) {
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
                title = {},
                navigationIcon = {
                  IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.Menu, "Nav Menu")
                  }
                },
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
      ActivityList(
          modifier = Modifier.padding(innerPadding),
          todaysActivity = todaysActivity,
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
}
