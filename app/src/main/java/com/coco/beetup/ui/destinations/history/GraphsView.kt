package com.coco.beetup.ui.destinations.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroupFlatRow
import com.coco.beetup.core.data.BeetMagnitude
import com.coco.beetup.core.data.BeetResistance
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GraphsView(
    historyData: List<ActivityGroupFlatRow>,
    magnitudes: List<BeetMagnitude>,
    resistances: List<BeetResistance>
) {
  val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }

  // Sort data by date for consistent chart rendering
  val sortedData = historyData.sortedBy { it.log.logDate }

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Magnitude Progress Chart
    MagnitudeChart(data = sortedData, dateFormatter = dateFormatter, magnitudes = magnitudes)
    // Resistance Progress Charts (if applicable)
    ResistanceCharts(data = sortedData, resistances = resistances, dateFormatter = dateFormatter)
    // Volume Chart (magnitude * total resistance)
    VolumeChart(data = sortedData, resistances = resistances, dateFormatter = dateFormatter)
  }
}

@Composable
private fun MagnitudeChart(
    data: List<ActivityGroupFlatRow>,
    dateFormatter: DateTimeFormatter,
    magnitudes: List<BeetMagnitude>
) {
  if (data.isEmpty()) return
  val modelProducer = remember { CartesianChartModelProducer() }

  // Group data by date and calculate min/max/avg
  val groupedData = data.groupBy { it.log.logDate }

  LaunchedEffect(data) {
    modelProducer.runTransaction {
      columnSeries {
        series(
            x = groupedData.keys.map { date -> date.toLocalDate().toEpochDay().toFloat() },
            y =
                groupedData.values.map { entries ->
                  val values = entries.map { it.log.magnitude }
                  if (values.size == 1) values.first() else values.average().toFloat()
                })
      }
    }
  }

  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              "Magnitude Progress",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)

          Spacer(modifier = Modifier.height(12.dp))
          CartesianChartHost(
              chart =
                  rememberCartesianChart(
                      rememberColumnCartesianLayer(),
                      startAxis = VerticalAxis.rememberStart(),
                      bottomAxis =
                          HorizontalAxis.rememberBottom(
                              valueFormatter = { _, x, _ ->
                                LocalDate.ofEpochDay(x.toLong()).format(dateFormatter)
                              })),
              modelProducer = modelProducer,
          )
        }
      }
}

@Composable
private fun ResistanceCharts(
    data: List<ActivityGroupFlatRow>,
    resistances: List<BeetResistance>,
    dateFormatter: DateTimeFormatter
) {
  // Group data by resistance type
  val resistanceData =
      data
          .groupBy { entry -> entry.resistanceEntry.map { it.resistanceKind }.distinct() }
          .filterKeys { it.isNotEmpty() }

  if (resistanceData.isEmpty()) return

  // Show chart for the most common resistance type
  val mostCommonResistanceKind =
      resistanceData.keys.flatten().groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

  mostCommonResistanceKind?.let { resistanceKind ->
    val resistanceName = resistances.find { it.id == resistanceKind }?.name ?: "Unknown"
    val filteredData =
        data.filter { entry -> entry.resistanceEntry.any { it.resistanceKind == resistanceKind } }

    ResistanceChart(
        data = filteredData,
        resistanceKind = resistanceKind,
        resistanceName = resistanceName,
        dateFormatter = dateFormatter)
  }
}

@Composable
private fun ResistanceChart(
    data: List<ActivityGroupFlatRow>,
    resistanceKind: Int,
    resistanceName: String,
    dateFormatter: DateTimeFormatter
) {
  if (data.isEmpty()) return
  val chartEntryModelProducer = remember { CartesianChartModelProducer() }

  // Group data by date and calculate total resistance per day
  val groupedData =
      data
          .groupBy { it.log.logDate }
          .mapValues { (_, entries) ->
            entries.flatMap { entry ->
              entry.resistanceEntry.filter { it.resistanceKind == resistanceKind }
            }
          }

  LaunchedEffect(data) {
    chartEntryModelProducer.runTransaction {
      columnSeries {
        series(
            x = groupedData.keys.map { date -> date.toLocalDate().toEpochDay().toFloat() },
            y =
                groupedData.values.map { resistanceEntries ->
                  resistanceEntries.sumOf { it.resistanceValue }.toFloat()
                })
      }
    }
  }

  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              "$resistanceName Progress",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)

          Spacer(modifier = Modifier.height(12.dp))
          CartesianChartHost(
              chart =
                  rememberCartesianChart(
                      rememberColumnCartesianLayer(),
                      startAxis = VerticalAxis.rememberStart(),
                      bottomAxis =
                          HorizontalAxis.rememberBottom(
                              valueFormatter = { _, x, _ ->
                                LocalDate.ofEpochDay(x.toLong()).format(dateFormatter)
                              })),
              modelProducer = chartEntryModelProducer,
          )
        }
      }
}

@Composable
private fun VolumeChart(
    data: List<ActivityGroupFlatRow>,
    resistances: List<BeetResistance>,
    dateFormatter: DateTimeFormatter
) {
  if (data.isEmpty()) return

  val chartEntryModelProducer = remember { CartesianChartModelProducer() }

  // Group data by date and calculate volume (magnitude * total resistance)
  val groupedData =
      data
          .groupBy { it.log.logDate }
          .mapValues { (_, entries) ->
            entries.map { entry ->
              val totalResistance = entry.resistanceEntry.sumOf { it.resistanceValue }
              entry.log.magnitude * totalResistance
            }
          }

  LaunchedEffect(data) {
    chartEntryModelProducer.runTransaction {
      columnSeries {
        series(
            x = groupedData.keys.map { date -> date.toLocalDate().toEpochDay() },
            y =
                groupedData.values.map { volumes ->
                  if (volumes.size == 1) volumes.first().toFloat() else volumes.average().toFloat()
                })
      }
    }
  }

  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              "Volume Trend (Magnitude × Total Resistance)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)

          Spacer(modifier = Modifier.height(12.dp))
          CartesianChartHost(
              chart =
                  rememberCartesianChart(
                      rememberColumnCartesianLayer(),
                      startAxis = VerticalAxis.rememberStart(),
                      bottomAxis =
                          HorizontalAxis.rememberBottom(
                              valueFormatter = { _, x, _ ->
                                LocalDate.ofEpochDay(x.toLong()).format(dateFormatter)
                              })),
              modelProducer = chartEntryModelProducer,
          )
        }
      }
}
