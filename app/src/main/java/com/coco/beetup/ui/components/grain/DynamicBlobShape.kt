package com.coco.beetup.ui.components.grain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Helper for convex hull
private fun crossProduct(p1: Offset, p2: Offset, p3: Offset): Float {
  return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)
}

private data class BlobPoint(val offset: Offset, val ballId: Int)

class DynamicBlobShape(
    private val seed: Int = 0,
    private val numBalls: Int = 6,
    private val pointsPerBall: Int = 12,
    private val randomness: Float = 0.5f,
) : Shape {
  override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density
  ): Outline {
    val random = Random(seed.toLong())

    if (size.width <= 0 || size.height <= 0 || numBalls <= 0) {
      return Outline.Generic(Path())
    }

    // 1. Generate metaball centers in a grid
    val gridCols = max(1, floor(sqrt(numBalls.toFloat() * size.width / size.height)).toInt())
    val gridRows = max(1, ceil(numBalls.toFloat() / gridCols).toInt())
    val cellWidth = size.width / gridCols
    val cellHeight = size.height / gridRows
    val ballRadius =
        min(cellWidth, cellHeight) / 2f * (1f - randomness / 3) // Slightly smaller base radius

    val allPoints = mutableListOf<BlobPoint>()

    for (i in 0 until numBalls) {
      val col = i % gridCols
      val row = i / gridCols

      val centerX =
          cellWidth * col + cellWidth / 2f + (random.nextFloat() - 0.5f) * cellWidth * randomness
      val centerY =
          cellHeight * row + cellHeight / 2f + (random.nextFloat() - 0.5f) * cellHeight * randomness
      // Vary radius
      val radius = ballRadius * (0.8f + random.nextFloat() * 0.4f + randomness * 0.2f)

      // 2. Generate points on the circumference of each ball
      for (j in 0 until pointsPerBall) {
        val angle = 2 * PI * j / pointsPerBall
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        allPoints.add(BlobPoint(Offset(x, y), i))
      }
    }

    // 3. Compute the convex hull of all points
    if (allPoints.size <= 2) {
      return Outline.Generic(Path())
    }

    val sortedPoints = allPoints.sortedWith(compareBy({ it.offset.x }, { it.offset.y }))

    val upperHull = mutableListOf<BlobPoint>()
    for (p in sortedPoints) {
      while (upperHull.size >= 2 &&
          crossProduct(upperHull[upperHull.size - 2].offset, upperHull.last().offset, p.offset) <=
              0) {
        upperHull.removeAt(upperHull.size - 1)
      }
      upperHull.add(p)
    }

    val lowerHull = mutableListOf<BlobPoint>()
    for (p in sortedPoints.asReversed()) {
      while (lowerHull.size >= 2 &&
          crossProduct(lowerHull[lowerHull.size - 2].offset, lowerHull.last().offset, p.offset) <=
              0) {
        lowerHull.removeAt(lowerHull.size - 1)
      }
      lowerHull.add(p)
    }

    val hullPoints = (upperHull + lowerHull.drop(1).dropLast(1))

    // 4. Inject "neck" points between segments spanning different balls
    val smoothedPoints = mutableListOf<Offset>()
    for (i in hullPoints.indices) {
      val p1 = hullPoints[i]
      val p2 = hullPoints[(i + 1) % hullPoints.size]

      smoothedPoints.add(p1.offset)

      if (p1.ballId != p2.ballId) {
        // This segment bridges two different balls.
        // We add a control point pushed inwards to create a "neck" or curve.
        // Or pushed outwards if we want it "bubbly".
        // Metaballs usually have a smooth connection. Straight lines look rigid.
        // Let's push slightly INWARDS to simulate the neck, or simple curve.
        // To avoid "straight", we just need to break the collinearity.

        val midX = (p1.offset.x + p2.offset.x) / 2
        val midY = (p1.offset.y + p2.offset.y) / 2
        val dx = p2.offset.x - p1.offset.x
        val dy = p2.offset.y - p1.offset.y
        val dist = sqrt(dx * dx + dy * dy)

        // Normal vector (pointing out of the hull) is (-dy, dx)
        // We want to push IN, so we use (dy, -dx) or -Normal.
        // Let's try pushing INwards to make it look "gooey".
        // Factor can be adjusted.
        val neckFactor = 0.15f // 15% of distance
        val nx = dy / dist
        val ny = -dx / dist

        val neckX = midX + nx * (dist * neckFactor)
        val neckY = midY + ny * (dist * neckFactor)

        smoothedPoints.add(Offset(neckX, neckY))
      }
    }

    // 5. Smooth the path
    return Outline.Generic(
        Path().apply {
          if (smoothedPoints.isEmpty()) return@apply

          moveTo(smoothedPoints[0].x, smoothedPoints[0].y)

          for (i in smoothedPoints.indices) {
            val p0 = smoothedPoints[(i - 1 + smoothedPoints.size) % smoothedPoints.size]
            val p1 = smoothedPoints[i]
            val p2 = smoothedPoints[(i + 1) % smoothedPoints.size]
            val p3 = smoothedPoints[(i + 2) % smoothedPoints.size]

            val tension = 0.5f // Increased tension for smoother curves

            val cp1 =
                Offset(p1.x + (p2.x - p0.x) / 6f * tension, p1.y + (p2.y - p0.y) / 6f * tension)
            val cp2 =
                Offset(p2.x - (p3.x - p1.x) / 6f * tension, p2.y - (p3.y - p1.y) / 6f * tension)

            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
          }
          close()
        })
  }
}

@Preview
@Composable
fun DynamicBlobShapePreview() {
  Column(Modifier.padding(20.dp)) {
    Box(
        modifier =
            Modifier.size(200.dp)
                .background(
                    Color.Cyan,
                    shape = DynamicBlobShape(seed = 3, numBalls = 7, randomness = 0.3f)))
  }
}
