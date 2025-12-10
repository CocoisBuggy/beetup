package com.coco.beetup.ui.components.grain

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes.Companion.Cookie4Sided
import androidx.compose.material3.MaterialShapes.Companion.Gem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph

enum class ShapeState {
  Sunny,
  VerySunny
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PulsingSunnyShape(
    triggerValue: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
  val shapeMorph = remember { Morph(Square, Cookie4Sided) }

  val targetState = if (triggerValue % 2 != 0) ShapeState.VerySunny else ShapeState.Sunny
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
            }
          }

  val animatedShape = remember(t) { MorphingShape(shapeMorph, t) }

  Surface(
      modifier = modifier.size(150.dp),
      color= MaterialTheme.colorScheme.primaryContainer,
      shape = animatedShape,
      shadowElevation = 8.dp) {
        Box(contentAlignment = Alignment.Center) { content() }
      }
}

@Preview
@Composable
fun PulsingShapeDemo() {
  var count by remember { mutableIntStateOf(0) }

  Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
    PulsingSunnyShape(triggerValue = count, modifier = Modifier.clickable { count++ }) {
      Text(
          text = "Taps: $count\nTap to Pulse!",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(16.dp),
      )
    }
  }
}
