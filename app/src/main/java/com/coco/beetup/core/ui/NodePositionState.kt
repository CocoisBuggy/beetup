package com.coco.beetup.core.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.layout.LayoutCoordinates

data class NodePosition(val id: Any, val coordinates: LayoutCoordinates)

class NodePositionState {
  val nodePositions = mutableStateMapOf<Any, LayoutCoordinates>()

  fun onNodePositioned(id: Any, coordinates: LayoutCoordinates) {
    nodePositions[id] = coordinates
  }
}
