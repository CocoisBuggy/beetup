package com.coco.beetup.ui.components.grain

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes.Companion.Cookie4Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie6Sided
import androidx.compose.material3.MaterialShapes.Companion.Square
import androidx.compose.material3.MaterialShapes.Companion.Sunny
import androidx.compose.material3.MaterialShapes.Companion.VerySunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph

enum class ShapeState {
  Sunny,
  VerySunny,
  Unselected
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PulsingSunnyShape(
    selected: Boolean,
    triggerValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
  val unselectedShape = remember { Square }
  val shapeMorph = remember { Morph(Cookie6Sided, Cookie4Sided) }
  val morphToUnselected = remember { Morph(Cookie6Sided, unselectedShape) }

  val targetState =
      if (!selected) ShapeState.Unselected
      else if (triggerValue % 2 != 0) ShapeState.VerySunny else ShapeState.Sunny

  val transition = updateTransition(targetState = targetState, label = "OfficialMorphTransition")

  val t: Float by
      transition.animateFloat(
          label = "MorphProgress",
          transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessLow,
            )
          }) { state ->
            when (state) {
              ShapeState.Sunny -> 0f
              ShapeState.VerySunny -> 1f
              ShapeState.Unselected -> 1f
            }
          }

  val animatedShape =
      remember(t, targetState) {
        if (targetState == ShapeState.Unselected) {
          MorphingShape(morphToUnselected, t)
        } else {
          MorphingShape(shapeMorph, t)
        }
      }

  Surface(
      onClick = onClick,
      modifier = modifier.size(150.dp),
      color = color,
      shape = animatedShape,
      shadowElevation = 8.dp) {
        Box(contentAlignment = Alignment.Center) { content() }
      }
}

@Preview
@Composable
fun PulsingShapeDemo() {
  var count by remember { mutableIntStateOf(0) }
  var selected by remember { androidx.compose.runtime.mutableStateOf(true) }

  Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      PulsingSunnyShape(
          selected = selected,
          triggerValue = count,
          modifier = Modifier,
          color = MaterialTheme.colorScheme.primary,
          onClick = { count++ }) {
            Text(
                text = if (selected) "Taps: $count\nTap to Pulse!" else "Unselected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp),
            )
          }
      androidx.compose.material3.Button(onClick = { selected = !selected }) {
        Text("Toggle Selected")
      }
    }
  }
}
