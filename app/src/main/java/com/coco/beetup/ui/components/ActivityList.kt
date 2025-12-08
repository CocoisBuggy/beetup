package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.ui.components.time.DateHeatMap
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.ZoneId

@Composable
fun ActivityList(
    todaysActivity: List<ActivityGroup>,
    selectedItems: Set<ActivityKey>,
    onToggleSelection: (ActivityKey) -> Unit,
    onToggleMultiSelection: () -> Unit,
    viewModel: BeetViewModel,
    modifier: Modifier
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
  ) {
    item {
      DateHeatMap(
          todaysActivity
              .flatMap { r ->
                r.logs.map {
                  it.log.logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                }
              }
              .toSet())
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
