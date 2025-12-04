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

data class ActivityGroup(
    @Embedded val exercise: BeetExercise,
    @Relation(parentColumn = "magnitude_kind", entityColumn = "id") val magnitude: BeetMagnitude,
    @Relation(entity = BeetExerciseLog::class, parentColumn = "id", entityColumn = "exerciseId")
    val logs: List<BeetExerciseLogWithResistances>
)
