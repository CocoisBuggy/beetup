package com.coco.beetup.ui.components.grain

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

val FloatArray.left: Float
  get() = this[0]

val FloatArray.top: Float
  get() = this[1]

val FloatArray.width: Float
  get() = this[2] - this[0]

val FloatArray.height: Float
  get() = this[3] - this[1]

class MorphingShape(private val morph: Morph, private val progress: Float) : Shape {

  override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density
  ): Outline {
    val path = morph.toPath(progress = progress)
    val composePath = path.asComposePath()

    val bounds = morph.calculateBounds()
    val scaleX = size.width / bounds.width
    val scaleY = size.height / bounds.height

    val matrix = Matrix()
    matrix.translate(
        x = (size.width - bounds.width * scaleX) / 2f - bounds.left * scaleX,
        y = (size.height - bounds.height * scaleY) / 2f - bounds.top * scaleY)
    matrix.scale(scaleX, scaleY)
    composePath.transform(matrix)

    return Outline.Generic(composePath)
  }
}
