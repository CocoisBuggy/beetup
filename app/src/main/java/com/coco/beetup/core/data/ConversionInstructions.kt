package com.coco.beetup.core.data

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
    )
