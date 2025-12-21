package com.coco.beetup.ui.components.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
    date: LocalDate,
    todaysActivity: List<ActivityGroup>,
    selectedItems: Set<ActivityKey>,
    activityDates: List<ActivityOverview>?,
    exerciseDates: Map<Int, List<LocalDate>>?,
    onToggleSelection: (ActivityKey) -> Unit,
    onToggleMultiSelection: () -> Unit,
    viewModel: BeetViewModel,
    modifier: Modifier,
    onDateChange: (LocalDate) -> Unit
) {
  val nodePositionState = remember { NodePositionState() }
  var columnCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val scrollState = rememberScrollState()

  Box(modifier = modifier) {
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
              Column() {
                DateHeatMap(
                    selectedDates = setOf(date),
                    activeDates = activityDates,
                    onDatePositioned = { id, pos -> nodePositionState.onNodePositioned(id, pos) },
                    onDateSelected = {
                      if (selectedItems.isEmpty()) {
                        onDateChange(it)
                      }
                    },
                )

                Column {
                  ButtonGroup(
                      overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                      },
                      verticalAlignment = Alignment.Top,
                  ) {
                    clickableItem(
                        onClick = { onDateChange(date.minusDays(1)) },
                        label = "",
                        icon = { Icon(Icons.Default.SkipPrevious, "Previous Day") },
                        weight = 1f,
                        enabled = selectedItems.isEmpty(),
                    )

                    customItem(
                        buttonGroupContent = {
                          Button(
                              enabled = selectedItems.isEmpty() && date != LocalDate.now(),
                              modifier =
                                  Modifier.weight(if (date != LocalDate.now()) 1.5f else 0.5f),
                              onClick = { onDateChange(LocalDate.now()) },
                              shapes =
                                  ButtonShapes(
                                      shape = ButtonDefaults.squareShape,
                                      pressedShape = ButtonDefaults.pressedShape)) {
                                Icon(Icons.Default.Today, "Today")
                                if (date != LocalDate.now()) {
                                  Text("Today", style = MaterialTheme.typography.labelSmall)
                                }
                              }
                        },
                        menuContent = {},
                    )

                    clickableItem(
                        onClick = { onDateChange(date.plusDays(1)) },
                        label = "",
                        icon = { Icon(Icons.Default.SkipNext, "Next Day") },
                        weight = if (date < LocalDate.now()) 1f else 0.5f,
                        enabled = date < LocalDate.now() && selectedItems.isEmpty(),
                    )
                  }

                  Text("$date", style = MaterialTheme.typography.displayLarge)

                  Text(
                      if (todaysActivity.isEmpty()) "No activity for this day"
                      else "Activity for this day",
                      style = MaterialTheme.typography.titleMedium)
                }
              }
            }
          }

          Spacer(Modifier.size(8.dp))

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
                      if (coordinates.isAttached) {
                        nodePositionState.onNodePositioned(item.key, coordinates)
                      }
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
