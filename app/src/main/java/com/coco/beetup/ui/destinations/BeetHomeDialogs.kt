package com.coco.beetup.ui.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.components.activity.CategorySelectionDialog
import com.coco.beetup.ui.components.activity.EditDialog
import com.coco.beetup.ui.components.activity.ExerciseLogDetailsDialog
import com.coco.beetup.ui.components.activity.ResistanceSelectionDialog
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

private fun LocalDate.isToday() = (this == LocalDate.now())

fun LocalDate.toDate(): Date {
  return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

@Composable
fun BeetHomeDialogs(
    date: LocalDate,
    activityGroups: List<ActivityGroup>,
    selectedItems: Set<ActivityKey>,
    viewModel: BeetViewModel,
    showCategoryDialog: Boolean,
    onDismissCategoryDialog: () -> Unit,
    selectedExerciseForEntry: BeetExercise?,
    onExerciseSelected: (BeetExercise) -> Unit,
    magnitudeForEntry: Int?,
    onMagnitudeSet: (Int) -> Unit,
    onDismissExerciseDetails: () -> Unit,
    exerciseCategories: List<BeetExercise>,
    editMode: Boolean,
    onEditCommit: () -> Unit
) {

  selectedItems.firstOrNull()?.let { key ->
    if (editMode) {
      val activity = remember { activityGroups.find { it.key == key }!! }
      val allowedResistances by
          viewModel
              .validResistancesFor(activity.key.exerciseId)
              .collectAsState(initial = emptyList())

      EditDialog(
          viewModel = viewModel,
          onDismiss = onEditCommit,
          onCommit = onEditCommit,
          forItem = activity,
          allowedResistances = allowedResistances,
      )
    }
  }

  if (showCategoryDialog) {
    CategorySelectionDialog(
        categories = exerciseCategories,
        onCategorySelected = onExerciseSelected,
        onDismiss = onDismissCategoryDialog,
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
            onConfirm = onMagnitudeSet,
            onDismiss = onDismissExerciseDetails,
        )
      }
    } else {
      ResistanceSelectionDialog(
          resistances = validResistances,
          onConfirm = { selectedResistances ->
            val logDate = if (date.isToday()) Date() else date.toDate()
            val newExerciseLog =
                BeetExerciseLog(
                    logDate = logDate,
                    logDay = date.toEpochDay().toInt(),
                    exerciseId = exercise.id,
                    magnitude = magnitudeForEntry,
                )
            val newResistances =
                selectedResistances.map {
                  BeetActivityResistance(
                      activityId = 0, // This will be overridden by the repository
                      resistanceKind = it.key,
                      resistanceValue = it.value)
                }
            viewModel.insertActivityAndResistances(newExerciseLog, newResistances)
            onDismissExerciseDetails()
          },
          onDismiss = onDismissExerciseDetails,
      )
    }
  }
}
