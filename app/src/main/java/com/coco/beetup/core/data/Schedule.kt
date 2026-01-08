package com.coco.beetup.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleKind {
    MONOTONIC,
    DAY_OF_WEEK,
    FOLLOWS_EXERCISE,
}

enum class ReminderStrength {
    IN_APP,
    NOTIFICATION,
    ANNOYING_NOTIFICATION,
}

@Entity(
    foreignKeys =
        [
            ForeignKey(
                entity = BeetExercise::class,
                parentColumns = ["id"],
                childColumns = ["exercise_id"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = BeetExercise::class,
                parentColumns = ["id"],
                childColumns = ["follows_exercise"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index(value = ["exercise_id"])],
)
data class BeetExerciseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_id") val activityId: Int,
    @ColumnInfo(name = "kind") val kind: ScheduleKind,
    @ColumnInfo(name = "reminder") val reminder: ReminderStrength? = null,
    @ColumnInfo(name = "follows_exercise") val followsExercise: Int? = null,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int? = null,
    @ColumnInfo(name = "monotonic_days") val monotonicDays: Int? = null,
) {
    init {
        when (kind) {
            ScheduleKind.MONOTONIC -> monotonicDays ?: throw IllegalArgumentException()
            ScheduleKind.DAY_OF_WEEK -> dayOfWeek ?: throw IllegalArgumentException()
            ScheduleKind.FOLLOWS_EXERCISE -> followsExercise ?: throw IllegalArgumentException()
        }
    }
}
