package com.coco.beetup.ui.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.core.data.daysSince
import com.coco.beetup.ui.components.activity.CategorySelectionDialog
import com.coco.beetup.ui.components.activity.EditDialog
import com.coco.beetup.ui.components.activity.ExerciseLogDetailsDialog
import com.coco.beetup.ui.components.activity.ResistanceSelectionDialog
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

private fun LocalDate.isToday() = (this == LocalDate.now())

fun LocalDate.toLocalDateTime(): LocalDateTime {
  return this.atStartOfDay()
}

@Composable
fun BeetHomeDialogs(
    state: BeetHomeState,
    activityGroups: List<ActivityGroup>,
    viewModel: BeetViewModel,
    exerciseCategories: List<BeetExercise>,
) {

  state.selectedItems.firstOrNull()?.let { key ->
    if (state.editMode) {
      val activity = remember { activityGroups.find { it.key == key }!! }
      val allowedResistances by
          viewModel
              .validResistancesFor(activity.key.exerciseId)
              .collectAsState(initial = emptyList())

      EditDialog(
          viewModel = viewModel,
          onDismiss = { state.editMode = false },
          onCommit = { state.editMode = false },
          forItem = activity,
          allowedResistances = allowedResistances,
      )
    }
  }

  if (state.showCategoryDialog) {
    val exerciseUsageCounts by viewModel.exerciseUsageCounts.collectAsState(initial = emptyList())

    val countsMap =
        remember(exerciseUsageCounts) {
          exerciseUsageCounts.associate { it.exerciseId to it.count }
        }

    val lastUsedMap =
        remember(exerciseUsageCounts) {
          exerciseUsageCounts.associate {
            it.exerciseId to (it.lastDate?.daysSince()?.toInt() ?: -1)
          }
        }

    CategorySelectionDialog(
        categories = exerciseCategories,
        usageCounts = countsMap,
        lastUsedDays = lastUsedMap,
        onCategorySelected = state::selectExercise,
        onDismiss = { state.showCategoryDialog = false },
    )
  }

  state.selectedExerciseForEntry?.let { exercise ->
    val magnitude by viewModel.magnitudeFor(exercise.magnitudeKind).collectAsState(initial = null)
    val validResistances by
        viewModel.validResistancesFor(exercise.id).collectAsState(initial = emptyList())

    if (state.magnitudeForEntry == null) {
      magnitude?.let {
        ExerciseLogDetailsDialog(
            exercise = exercise,
            magnitudeName = it.name,
            magnitudeUnit = it.unit,
            onConfirm = { state.magnitudeForEntry = it },
            onDismiss = state::dismissExerciseDetails,
        )
      }
    } else {
        state.magnitudeForEntry?.let { magnitudeForEntry ->
            ResistanceSelectionDialog(
                resistances = validResistances,
                onConfirm = { selectedResistances ->
                    val logDate = if (state.date.isToday()) LocalDateTime.now() else state.date.toLocalDateTime()
                    val newExerciseLog =
                        BeetExerciseLog(
                            logDate = logDate,
                            logDay = state.date.toEpochDay().toInt(),
                            exerciseId = exercise.id,
                            magnitude = magnitudeForEntry,
                        )
                    val newResistances =
                        selectedResistances.map {
                            BeetActivityResistance(
                                activityId = 0, // This will be overridden by the repository
                                resistanceKind = it.key,
                                resistanceValue = it.value
                            )
                        }
                    viewModel.insertActivityAndResistances(newExerciseLog, newResistances)
                    state.dismissExerciseDetails()
                },
                onDismiss = state::dismissExerciseDetails,
            )
        }
    }
  }
}
