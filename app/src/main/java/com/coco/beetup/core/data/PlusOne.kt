package com.coco.beetup.core.data

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PlusOne(onFinished: () -> Unit) {
  var visible by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    visible = false
    delay(500)
    onFinished()
  }

  val animatedOffsetY by
      animateDpAsState(
          targetValue = if (visible) 0.dp else (-24).dp,
          animationSpec =
              tween(
                  durationMillis = 500,
                  easing = FastOutLinearInEasing,
              ),
          label = "offsetY")
  val animatedAlpha by
      animateFloatAsState(
          targetValue = if (visible) 1f else 0f,
          animationSpec =
              tween(
                  durationMillis = 500,
                  easing = FastOutLinearInEasing,
              ),
          label = "alpha")

  Text(
      text = "+1",
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.offset(y = animatedOffsetY).alpha(animatedAlpha))
}
