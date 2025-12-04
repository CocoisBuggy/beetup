package com.coco.beetup.core.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BeetExerciseLogWithResistances(
    @Embedded val log: BeetExerciseLog,
    @Relation(
        entity = BeetResistance::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = BeetActivityResistance::class,
                parentColumn = "activity_id",
                entityColumn = "resistance_kind"))
    val resistances: List<BeetResistance>
)

data class ActivityGroup(
    @Embedded val exercise: BeetExercise,
    @Relation(parentColumn = "magnitude_kind", entityColumn = "id") val magnitude: BeetMagnitude,
    @Relation(entity = BeetExerciseLog::class, parentColumn = "id", entityColumn = "exerciseId")
    val logs: List<BeetExerciseLogWithResistances>
)
