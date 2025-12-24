package com.coco.beetup.ui.components.grain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

val ResistanceIcon =
    mapOf<Int, ImageVector>(
        // Overhang
        Pair(1, Icons.Default.Rotate90DegreesCcw),
        // Grams
        Pair(2, Icons.Default.Scale),
        // Edge
        Pair(4, Icons.AutoMirrored.Default.CompareArrows),
        // Duration
        Pair(5, Icons.Default.Timer)
    )
