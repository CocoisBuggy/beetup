package com.coco.beetup.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
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
        ForeignKey(
            entity = BeetResistance::class,
            parentColumns = ["id"],
            childColumns = ["resistance_kind"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BeetExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_name") val exerciseName: String,
    @ColumnInfo(name = "exercise_description") val exerciseDescription: String,
    @ColumnInfo(name = "magnitude_kind") val magnitudeKind: Int,
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
    ]
)
data class BeetExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val exerciseId: Int,
    @ColumnInfo(name = "log_date") val logDate: Date,
    /** Day of all time (unix day) */
    @ColumnInfo(name = "log_day") val logDay: Int,
    @ColumnInfo(name = "magnitude", defaultValue = "1") val magnitude: Int,
    @ColumnInfo(name = "resistance") val resistance: Int?,
    @ColumnInfo(name = "difficulty") val difficulty: Difficulty?,
    @ColumnInfo(name = "comment") val comment: String?,
)
