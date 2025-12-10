package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.ActivityOverview
import com.coco.beetup.ui.components.time.DateHeatMap
import com.coco.beetup.ui.viewmodel.BeetViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityList(
    todaysActivity: List<ActivityGroup>,
    selectedItems: Set<ActivityKey>,
    activityDates: List<ActivityOverview>?,
    onToggleSelection: (ActivityKey) -> Unit,
    onToggleMultiSelection: () -> Unit,
    viewModel: BeetViewModel,
    modifier: Modifier,
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
  ) {
    item {
      if (activityDates == null) {
        LoadingIndicator()
      } else {
        DateHeatMap(activeDates = activityDates)
      }
    }

    items(todaysActivity) { item ->
      val isSelected = item.key in selectedItems

      DeletableExerciseEntry(
          viewModel = viewModel,
          item = item,
          isSelected = isSelected,
          onToggleSelection = { onToggleSelection(item.key) },
          onToggleMultiSelection = onToggleMultiSelection,
      )
    }
  }
}
