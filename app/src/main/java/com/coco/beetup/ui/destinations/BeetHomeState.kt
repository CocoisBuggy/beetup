package com.coco.beetup.ui.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.BeetExercise
import java.time.LocalDate

class BeetHomeState {
  var date by mutableStateOf(LocalDate.now())
  var editMode by mutableStateOf(false)
  var selectedItems by mutableStateOf<Set<ActivityKey>>(emptySet())
  var multiSelectionEnabled by mutableStateOf(false)
  var showCategoryDialog by mutableStateOf(false)
  var selectedExerciseForEntry by mutableStateOf<BeetExercise?>(null)
  var magnitudeForEntry by mutableStateOf<Int?>(null)

  fun clearSelection() {
    selectedItems = emptySet()
    multiSelectionEnabled = false
  }

  fun toggleSelection(key: ActivityKey) {
    selectedItems =
        if (multiSelectionEnabled) {
          if (key in selectedItems) {
            selectedItems - key
          } else {
            selectedItems + key
          }
        } else {
          if (key in selectedItems) emptySet() else setOf(key)
        }
  }

  fun toggleMultiSelection() {
    multiSelectionEnabled = !multiSelectionEnabled
  }

  fun startAddActivity() {
    selectedExerciseForEntry = null
    magnitudeForEntry = null
    showCategoryDialog = true
  }

  fun selectExercise(exercise: BeetExercise) {
    selectedExerciseForEntry = exercise
    showCategoryDialog = false
  }

  fun dismissExerciseDetails() {
    selectedExerciseForEntry = null
  }
}

@Composable
fun rememberBeetHomeState(): BeetHomeState {
  return remember { BeetHomeState() }
}
