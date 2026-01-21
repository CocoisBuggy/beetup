package com.coco.beetup.core.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

enum class ScheduleKind {
  /** Some fixed number of days tp wait, and then prompt user to do the exercise */
  MONOTONIC,
  DAY_OF_WEEK,
  FOLLOWS_EXERCISE,
}

enum class ReminderStrength {
  IN_APP,
  NOTIFICATION,
  ANNOYING_NOTIFICATION,
}

/**
 * You can imagine this as rules that the notification engine should be following when it schedules
 * a notification.
 */
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
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "kind") val kind: ScheduleKind,
    @ColumnInfo(name = "reminder") val reminder: ReminderStrength? = null,
    @ColumnInfo(name = "follows_exercise") val followsExercise: Int? = null,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: DayOfWeek? = null,
    @ColumnInfo(name = "monotonic_days") val monotonicDays: Int? = null,
    @ColumnInfo(name = "message") val message: String? = null,
    @ColumnInfo(name = "enabled") val enabled: Boolean = true,
    @ColumnInfo(name = "show_after") val showAfter: LocalDate? = null,
    @ColumnInfo(name = "dismissed") val dismissed: Boolean = true,
) {
  init {
    when (kind) {
      ScheduleKind.MONOTONIC -> monotonicDays ?: throw IllegalArgumentException()
      ScheduleKind.DAY_OF_WEEK -> dayOfWeek ?: throw IllegalArgumentException()
      ScheduleKind.FOLLOWS_EXERCISE -> followsExercise ?: throw IllegalArgumentException()
    }

    if (monotonicDays != null && dayOfWeek != null) {
      throw IllegalArgumentException(
          "The dayOfWeek and monotonicDays values are mutually exclusive")
    }
  }
}

@Dao
interface BeetExerciseScheduleDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSchedule(schedule: BeetExerciseSchedule): Long

  @Update suspend fun updateSchedule(schedule: BeetExerciseSchedule)

  @Delete suspend fun deleteSchedule(schedule: BeetExerciseSchedule)

  @Query("SELECT * FROM BeetExerciseSchedule WHERE id = :id")
  fun getSchedule(id: Int): Flow<BeetExerciseSchedule>

  @Query("SELECT * FROM BeetExerciseSchedule ORDER BY id")
  fun getAllSchedules(): Flow<List<BeetExerciseSchedule>>

  @Query("SELECT * FROM BeetExerciseSchedule WHERE exercise_id = :exerciseId")
  fun getSchedulesForExercise(exerciseId: Int): Flow<List<BeetExerciseSchedule>>

  @Query("SELECT * FROM BeetExerciseSchedule WHERE exercise_id = :exerciseId LIMIT 1")
  fun getScheduleForExercise(exerciseId: Int): Flow<BeetExerciseSchedule?>

  @Query("SELECT * FROM BeetExerciseSchedule WHERE kind = :kind")
  fun getSchedulesByKind(kind: ScheduleKind): Flow<List<BeetExerciseSchedule>>

  @Query("DELETE FROM BeetExerciseSchedule WHERE exercise_id = :exerciseId")
  suspend fun deleteSchedulesForExercise(exerciseId: Int)

  @Query("UPDATE BeetExerciseSchedule set dismissed = true WHERE id = :scheduleId")
  suspend fun dismiss(scheduleId: Int)
}
