package com.coco.beetup.ui.destinations.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroupFlatRow
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetResistance

@Composable
fun StatsSummary(
    sortedData: List<ActivityGroupFlatRow>,
    resistanceGroups: Map<Int, List<BeetActivityResistance>>,
    resistances: List<BeetResistance>
) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              "Summary Statistics",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)

          Spacer(modifier = Modifier.height(12.dp))

          val avgMagnitude = sortedData.map { it.log.magnitude }.average()
          val maxMagnitude = sortedData.maxOfOrNull { it.log.magnitude } ?: 0
          val totalSessions = sortedData.size

          Text("Total Sessions: $totalSessions", style = MaterialTheme.typography.bodyMedium)
          Text(
              "Average Magnitude: ${String.format("%.1f", avgMagnitude)}",
              style = MaterialTheme.typography.bodyMedium)
          Text("Max Magnitude: $maxMagnitude", style = MaterialTheme.typography.bodyMedium)

          resistanceGroups.forEach { (resistanceKind, entries) ->
            val resistanceName = resistances.find { it.id == resistanceKind }?.name ?: "Unknown"
            val avgResistance = entries.map { it.resistanceValue }.average()
            val maxResistance = entries.maxOfOrNull { it.resistanceValue } ?: 0

            Text(
                "$resistanceName - Avg: ${
                        String.format(
                            "%.1f",
                            avgResistance
                        )
                    }, Max: $maxResistance",
                style = MaterialTheme.typography.bodySmall)
          }
        }
      }
}
