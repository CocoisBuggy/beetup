package com.coco.beetup.ui.destinations.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroupFlatRow
import com.coco.beetup.core.data.ActivityKey
import com.coco.beetup.core.data.BeetMagnitude
import com.coco.beetup.core.data.BeetResistance
import java.time.format.DateTimeFormatter

@Composable
fun TimelineView(
    historyData: List<ActivityGroupFlatRow>,
    magnitudes: List<BeetMagnitude>,
    resistances: List<BeetResistance>
) {
  val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    historyData
        .groupBy { it.log.logDate.toLocalDate() }
        .toSortedMap()
        .forEach { (date, entries) ->
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text(
                      date.format(dateFormatter),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold)

                  Spacer(modifier = Modifier.height(8.dp))

                  // Group entries by ActivityKey pattern (same exercise, magnitude, resistance
                  // combination)
                  entries
                      .groupBy { entry ->
                        ActivityKey(
                            exerciseId = entry.exercise.id,
                            magValue = entry.log.magnitude,
                            resistances =
                                entry.resistanceEntry.map {
                                  Pair(it.resistanceKind, it.resistanceValue)
                                })
                      }
                      .forEach { (activityKey, groupedEntries) ->
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                          // Exercise name and set count
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                  Text(
                                      groupedEntries.first().exercise.exerciseName,
                                      style = MaterialTheme.typography.bodyMedium,
                                      fontWeight = FontWeight.Medium)

                                  Text(
                                      "${groupedEntries.size} sets • Magnitude: ${groupedEntries.first().log.magnitude}",
                                      style = MaterialTheme.typography.bodySmall)

                                  if (groupedEntries.first().resistanceEntry.isNotEmpty()) {
                                    Text(
                                        "Resistance: ${groupedEntries.first().resistanceEntry.joinToString(", ") {
                                                        "${resistances.find { r -> r.id == it.resistanceKind }?.name ?: "Unknown"}: ${it.resistanceValue}"
                                                    }}",
                                        style = MaterialTheme.typography.bodySmall)
                                  }

                                  // Show comments if any
                                  groupedEntries
                                      .mapNotNull { it.log.comment }
                                      .takeIf { it.isNotEmpty() }
                                      ?.let { comments ->
                                        Text(
                                            "Notes: ${comments.joinToString("; ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                      }
                                }

                                if (groupedEntries.any { it.log.banner }) {
                                  Icon(
                                      Icons.Default.FitnessCenter,
                                      contentDescription = "Banner",
                                      tint = MaterialTheme.colorScheme.primary)
                                }
                              }
                        }
                      }
                }
              }
        }
  }
}
