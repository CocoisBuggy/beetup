package com.coco.beetup.core.data

import androidx.room.Embedded
import androidx.room.Relation

data class BeetExpandedResistance(
    @Embedded val entry: BeetActivityResistance,
    @Relation(parentColumn = "resistance_kind", entityColumn = "id") val extra: BeetResistance
)

data class BeetExerciseLogWithResistances(
    @Embedded val log: BeetExerciseLog,
    @Relation(
        entity = BeetActivityResistance::class,
        parentColumn = "id",
        entityColumn = "resistance_kind",
    )
    val resistances: List<BeetExpandedResistance>
)

data class ActivityKey(
    val exerciseId: Int,
    val magValue: Int,
    val resistances: List<Pair<Int, Int>>
)

data class ActivityGroup(
    val exercise: BeetExercise,
    val magnitude: BeetMagnitude,
    val logs: List<BeetExerciseLogWithResistances>
) {
  val key: ActivityKey =
      ActivityKey(
          exerciseId = exercise.id,
          magValue = magnitude.id,
          resistances =
              logs.first().resistances.map { Pair(it.extra.id, it.entry.resistanceValue) })
}

data class ActivityGroupFlatRow(
    @Embedded val log: BeetExerciseLog,
    @Relation(parentColumn = "exerciseId", entityColumn = "id") val exercise: BeetExercise,
    @Relation(parentColumn = "id", entityColumn = "activity_id")
    val resistanceEntry: List<BeetActivityResistance>,
)
