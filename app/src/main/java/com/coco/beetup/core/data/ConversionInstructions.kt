package com.coco.beetup.core.data

import kotlin.time.Duration.Companion.seconds

private fun formatGrams(resistance: BeetExpandedResistance): String {
  val grams = resistance.entry.resistanceValue
  return if (grams >= 1000) {
    val kilograms = grams / 1000.0
    // Format to 1 decimal place if not a whole number, otherwise show as integer
    if (kilograms % 1 == 0.0) {
      "${kilograms.toInt()}kg"
    } else {
      "%.1fkg".format(kilograms)
    }
  } else {
    "${grams}g"
  }
}

val ResistanceConversion =
    mapOf<Int, (BeetExpandedResistance) -> String>(
        Pair(2, { formatGrams(it) }),
        Pair(5, { it.entry.resistanceValue.toLong().seconds.toString() }),
    )

private fun formatDistance(meters: Int): String {
  return if (meters >= 1000) {
    val kilometers = meters / 1000.0
    return if (kilometers % 1 == 0.0) {
      "${kilometers.toInt()}km"
    } else {
      "%.1fkm".format(kilometers)
    }
  } else {
    "($meters)m"
  }
}

val MagnitudeConversion =
    mapOf<Int, (Int) -> String>(
        // Distance
        Pair(2, { formatDistance(it) }),
        // Duration
        Pair(3, { it.toLong().seconds.toString() }),
    )
