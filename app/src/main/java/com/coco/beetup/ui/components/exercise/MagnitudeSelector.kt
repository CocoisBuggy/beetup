package com.coco.beetup.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetMagnitude
import kotlin.collections.forEach

@Composable
fun MagnitudeSelector(
    magnitudes: List<BeetMagnitude>,
    selectedMagnitude: BeetMagnitude?,
    onMagnitudeSelected: (BeetMagnitude) -> Unit
) {
  Column {
    Text("Select Magnitude Kind:", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      magnitudes.forEach { mag ->
        FilterChip(
            selected = selectedMagnitude == mag,
            onClick = { onMagnitudeSelected(mag) },
            label = { Text(mag.name) })
      }
    }
  }
}
