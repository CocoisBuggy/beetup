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

@Composable
fun AnimatedNodeLinks(
    nodes: NodePositionState,
    columnCoords: LayoutCoordinates,
    exerciseDates: Map<Int, List<LocalDate>>,
    selectedItems: Set<ActivityKey>,
    scrollState: ScrollState
) {
  val color = MaterialTheme.colorScheme.secondary

  val expressiveSpring =
      spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)

  val lineProgress: Float by
      animateFloatAsState(
          targetValue = if (selectedItems.isEmpty()) 0f else 1f,
          animationSpec = expressiveSpring,
          label = "lineProgressAnimation",
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
          radius = 8.dp.toPx(),
          center = startPoint,
          color = color,
      )

      exerciseDates[activityKey.exerciseId]?.forEach { date ->
        val endLayoutCoords = nodes.nodePositions[date] ?: return@forEach
        val endOffset = parentCoords.localPositionOf(endLayoutCoords, Offset.Zero)

        val finalEndOffset = endOffset + scrollOffset

        val endPoint =
            Offset(
                x = finalEndOffset.x + (endLayoutCoords.size.width / 2f),
                y = finalEndOffset.y + endLayoutCoords.size.height)

        val midpoint =
            Offset(x = (startPoint.x + endPoint.x) / 2f, y = (startPoint.y + endPoint.y) / 2f)

        val angle = atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x)
        val perpendicularAngle = angle - (Math.PI / 2).toFloat()
        val controlPoint =
            if (endPoint.x <= startPoint.x)
                Offset(
                    x = midpoint.x + cos(perpendicularAngle) * curveOffsetDistance,
                    y = midpoint.y + sin(perpendicularAngle) * curveOffsetDistance)
            else {
              Offset(
                  x = midpoint.x - cos(perpendicularAngle) * curveOffsetDistance,
                  y = midpoint.y + sin(perpendicularAngle) * curveOffsetDistance)
            }

        val path =
            Path().apply {
              moveTo(startPoint.x, startPoint.y)
              quadraticTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
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
