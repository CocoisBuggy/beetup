package com.coco.beetup.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class BeetMagnitude(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "magnitude_name") val name: String,
    @ColumnInfo(name = "magnitude_description") val description: String,
)

@Entity
data class BeetResistance(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "resistance_name") val name: String,
    @ColumnInfo(name = "resistance_description") val description: String,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BeetMagnitude::class,
            parentColumns = ["id"],
            childColumns = ["magnitude_kind"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["magnitude_kind"])]
)
data class BeetExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_name") val exerciseName: String,
    @ColumnInfo(name = "exercise_description") val exerciseDescription: String,
    @ColumnInfo(name = "magnitude_kind") val magnitudeKind: Int,
)

/**
 * The kind of resistances that are valid for some exercise can be specified and
 * updated. What this means is that there is a many-to-many relation between the
 * exercises and the various resistance kinds that can be composed for some
 * reified ACTIVITY that appears in the exercise log.
 */
@Entity(
    primaryKeys = ["exercise_id", "resistance_kind"],
    foreignKeys = [
        ForeignKey(
            entity = BeetExercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BeetResistance::class,
            parentColumns = ["id"],
            childColumns = ["resistance_kind"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ValidBeetResistances(
    @ColumnInfo(name = "exercise_id") val exerciseInt: Int,
    @ColumnInfo(name = "resistance_kind") val resistanceKind: Int,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BeetExercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)
data class BeetExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "log_date") val logDate: Date = Date(),
    @ColumnInfo(name = "log_day") val logDay: Int = (Date().time / 86_400_000L).toInt(),
    @ColumnInfo val exerciseId: Int,
    /** Day of all time (unix day) */
    @ColumnInfo(name = "magnitude", defaultValue = "1") val magnitude: Int = 0,
    @ColumnInfo(name = "difficulty") val difficulty: Difficulty? = null,
    @ColumnInfo(name = "comment") val comment: String? = null,
)

@Entity(
    primaryKeys = ["activity_id", "resistance_kind"],
    foreignKeys = [
        ForeignKey(
            entity = BeetExerciseLog::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class BeetActivityResistance(
    @ColumnInfo(name = "activity_id") val activityId: Int,
    @ColumnInfo(name = "resistance_kind") val resistanceKind: Int,
    @ColumnInfo(name = "resistance_value") val resistanceValue: Int,
)