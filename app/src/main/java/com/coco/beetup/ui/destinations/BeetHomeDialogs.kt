package com.coco.beetup.ui.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.components.CategorySelectionDialog
import com.coco.beetup.ui.components.ExerciseLogDetailsDialog
import com.coco.beetup.ui.components.ResistanceSelectionDialog
import com.coco.beetup.ui.viewmodel.BeetViewModel

@Composable
fun BeetHomeDialogs(
    viewModel: BeetViewModel,
    showCategoryDialog: Boolean,
    onDismissCategoryDialog: () -> Unit,
    selectedExerciseForEntry: BeetExercise?,
    onExerciseSelected: (BeetExercise) -> Unit,
    magnitudeForEntry: Int?,
    onMagnitudeSet: (Int) -> Unit,
    onDismissExerciseDetails: () -> Unit,
    exerciseCategories: List<BeetExercise>
) {
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
            val newExerciseLog =
                BeetExerciseLog(
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
