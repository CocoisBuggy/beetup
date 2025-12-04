package com.coco.beetup.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.ui.viewmodel.BeetViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeletableExerciseEntry(
    viewModel: BeetViewModel,
    day: Int,
    item: ActivityGroup,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onToggleMultiSelection: () -> Unit,
) {
  ActivityEntry(
      viewModel = viewModel,
      activity = item,
      day = day,
      isSelected = isSelected,
      modifier =
          Modifier.combinedClickable(
              onClick = { onToggleSelection() },
              onLongClick = { onToggleMultiSelection() },
          ),
  )
}
