package com.coco.beetup.ui.components.grain

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.ui.NodePositionState
import java.time.LocalDate
import kotlin.collections.forEach
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedNodeLinks(
    nodes: NodePositionState,
    columnCoords: LayoutCoordinates,
    exerciseDates: Map<Int, List<LocalDate>>,
    selectedItems: Set<ActivityKey>,
    scrollState: ScrollState
) {
  val randomPairs = remember {
    (0..20).map { Pair(Random.nextInt(-200, 200), Random.nextInt(-300, 300)) }
  }
  val color = MaterialTheme.colorScheme.tertiary
  val lineProgress: Float by
      animateFloatAsState(
          targetValue = if (selectedItems.isEmpty()) 0f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioLowBouncy,
                  stiffness = Spring.StiffnessLow,
              ),
          label = "lineProgressAnimation",
      )

  val circleBump: Float by
      animateFloatAsState(
          targetValue = if (selectedItems.isEmpty()) 0f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow,
              ),
          label = "circleBumpAnimation",
      )

  Canvas(modifier = Modifier.fillMaxSize()) {
    val parentCoords = columnCoords ?: return@Canvas

    selectedItems.forEach { activityKey ->
      val curveOffsetDistance = 100f
      val scrollPixels = scrollState.value.toFloat()
      val scrollOffset = Offset(0f, -scrollPixels)
      val startLayoutCoords = nodes.nodePositions[activityKey] ?: return@forEach
      val startOffset = startLayoutCoords.positionInParent()
      val finalStartOffset = startOffset + scrollOffset
      val startPoint =
          Offset(
              x = finalStartOffset.x + (startLayoutCoords.size.width / 2f), y = finalStartOffset.y)

      drawCircle(
          radius = 8.dp.toPx() * circleBump,
          center = startPoint,
          color = color,
      )

      exerciseDates[activityKey.exerciseId]?.forEachIndexed { idx, date ->
        val endLayoutCoords = nodes.nodePositions[date] ?: return@forEach
        val endOffset = parentCoords.localPositionOf(endLayoutCoords, Offset.Zero)

        val finalEndOffset = endOffset + scrollOffset

        val endPoint =
            Offset(
                x = finalEndOffset.x + (endLayoutCoords.size.width / 2f),
                y = finalEndOffset.y + endLayoutCoords.size.height)

        val angle = atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x)
        val perpendicularAngle = angle - (Math.PI / 2).toFloat()

        val point1OnLine = startPoint + (endPoint - startPoint) / 3f
        val point2OnLine = startPoint + (endPoint - startPoint) * (2f / 3f)

        val cosPerpendicular = cos(perpendicularAngle) * curveOffsetDistance
        val sinPerpendicular = sin(perpendicularAngle) * curveOffsetDistance

        val xOffset = if (endPoint.x <= startPoint.x) cosPerpendicular else -cosPerpendicular

        val controlPoint1 =
            Offset(
                x = point1OnLine.x + xOffset + randomPairs[idx].first,
                y = point1OnLine.y + sinPerpendicular)

        val controlPoint2 =
            Offset(
                x = point2OnLine.x + xOffset + randomPairs[idx].second,
                y = point2OnLine.y + sinPerpendicular)

        val path =
            Path().apply {
              moveTo(startPoint.x, startPoint.y)
              cubicTo(
                  controlPoint1.x,
                  controlPoint1.y,
                  controlPoint2.x,
                  controlPoint2.y,
                  endPoint.x,
                  endPoint.y)
            }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, forceClosed = false)

        val totalLength = pathMeasure.length
        val animatedLength = totalLength * lineProgress
        val partialPath = Path()

        pathMeasure.getSegment(
            0f,
            animatedLength,
            partialPath,
            startWithMoveTo = true,
        )

        drawPath(
            path = partialPath,
            color = color,
            style =
                Stroke(
                    width = 3.dp.toPx(),
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals = floatArrayOf(8.dp.toPx(), 4.dp.toPx()), phase = 0f)),
        )
      }
    }
  }
}
