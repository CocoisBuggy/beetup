package com.coco.beetup.ui.components.activity

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
    item: ActivityGroup,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onToggleMultiSelection: () -> Unit,
    modifier: Modifier
) {
  ActivityEntry(
      viewModel = viewModel,
      activity = item,
      isSelected = isSelected,
      modifier =
          modifier.combinedClickable(
              onClick = { onToggleSelection() },
              onLongClick = {
                onToggleMultiSelection()
                onToggleSelection()
              },
          ))
}
