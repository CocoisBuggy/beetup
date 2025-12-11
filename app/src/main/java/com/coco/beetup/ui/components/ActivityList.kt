package com.coco.beetup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.ActivityOverview
import com.coco.beetup.core.ui.NodePositionState
import com.coco.beetup.ui.components.grain.AnimatedNodeLinks
import com.coco.beetup.ui.components.time.DateHeatMap
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityList(
    todaysActivity: List<ActivityGroup>,
    selectedItems: Set<ActivityKey>,
    activityDates: List<ActivityOverview>?,
    exerciseDates: Map<Int, List<LocalDate>>?,
    onToggleSelection: (ActivityKey) -> Unit,
    onToggleMultiSelection: () -> Unit,
    viewModel: BeetViewModel,
    modifier: Modifier,
) {
  val nodePositionState = remember { NodePositionState() }
  var columnCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

  Box(modifier = modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier.fillMaxSize().verticalScroll(scrollState).onGloballyPositioned {
              columnCoords = it
            }) {
          if (activityDates == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                  LoadingIndicator()
                }
          } else {
            AnimatedVisibility(true) {
              DateHeatMap(
                  activeDates = activityDates,
                  onDatePositioned = { id, pos -> nodePositionState.onNodePositioned(id, pos) },
              )
            }
          }

          Row() { Text("Activity:") }

          todaysActivity.forEach { item ->
            val isSelected = item.key in selectedItems

            DeletableExerciseEntry(
                viewModel = viewModel,
                item = item,
                isSelected = isSelected,
                onToggleSelection = { onToggleSelection(item.key) },
                onToggleMultiSelection = onToggleMultiSelection,
                modifier =
                    Modifier.onGloballyPositioned { coordinates ->
                      nodePositionState.onNodePositioned(item.key, coordinates)
                    })
          }
        }

    columnCoords?.let { columnCoords ->
      AnimatedNodeLinks(
          nodes = nodePositionState,
          columnCoords = columnCoords,
          exerciseDates = exerciseDates ?: emptyMap(),
          selectedItems = selectedItems,
          scrollState = scrollState,
      )
    }
  }
}
