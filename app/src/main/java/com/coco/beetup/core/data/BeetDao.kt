package com.coco.beetup.core.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.Date
import kotlinx.coroutines.flow.Flow

data class ActivityOverviewPrimitive(val date: Date, val count: Int)

data class ExerciseOccurredOnDatePrimitive(val date: Date, val exercise: Int)

data class ExerciseUsagePrimitive(val exerciseId: Int, val count: Int, val lastDate: Date?)

@Dao
interface BeetProfileDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(profile: BeetProfile)

  @Query("SELECT * FROM BeetProfile LIMIT 1") fun getProfile(): Flow<BeetProfile>
}

@Dao
interface BeetExerciseDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg exercises: BeetExercise)

  @Delete suspend fun delete(exercise: BeetExercise)

  @Delete suspend fun delete(exercises: List<BeetExercise>)

  @Query("SELECT * FROM BeetExercise WHERE id = :id") fun getExercise(id: Int): Flow<BeetExercise>

  @Query("SELECT * FROM BeetExercise") fun getAllExercises(): Flow<List<BeetExercise>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg magnitudes: BeetMagnitude)

  @Query("SELECT * FROM BeetMagnitude WHERE id = :id")
  fun getMagnitude(id: Int): Flow<BeetMagnitude>

  @Query("SELECT * FROM BeetMagnitude") fun getAllMagnitudes(): Flow<List<BeetMagnitude>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg resistances: BeetResistance)

  @Query("SELECT * FROM BeetResistance WHERE id = :id")
  fun getResistance(id: Int): Flow<BeetResistance>

  @Query("SELECT * FROM BeetResistance") fun getAllResistances(): Flow<List<BeetResistance>>

  @Query(
      "SELECT * FROM BeetExercise join BeetExerciseLog " +
          "on BeetExercise.id = BeetExerciseLog.exerciseId " +
          "where log_day = :day group by exerciseId",
  )
  fun getActiveExercisesForDay(day: Int): Flow<List<BeetExercise>>

  @Query(
      "SELECT * FROM ValidBeetResistances " +
          " JOIN BeetResistance ON ValidBeetResistances.resistance_kind = BeetResistance.id" +
          " WHERE exercise_id = :exercise")
  fun validResistancesFor(exercise: Int): Flow<List<BeetResistance>>

  @Query(
      """
        delete from ValidBeetResistances where exercise_id = :exerciseId and resistance_kind = :resistanceId
      """)
  suspend fun removeResistanceReference(exerciseId: Int, resistanceId: Int)

  @Query(
      """
          insert into ValidBeetResistances (exercise_id, resistance_kind)
          values (:exerciseId, :resistanceId)
      """)
  suspend fun insertResistanceReference(exerciseId: Int, resistanceId: Int)
}

@Dao
interface ExerciseLogDao {
  @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertLog(log: BeetExerciseLog): Long

  @Delete suspend fun delete(activities: List<BeetExerciseLog>)

  @Update suspend fun update(activity: BeetExerciseLog)

  @Query("SELECT * FROM BeetExerciseLog WHERE id = :id") fun getLog(id: Int): Flow<BeetExerciseLog>

  @Query("SELECT * FROM BeetExerciseLog order by log_date desc")
  fun getAllLogs(): Flow<List<BeetExerciseLog>>

  @Query("SELECT * FROM BeetExerciseLog where log_day = :day")
  fun getLogsForDay(day: Int): Flow<List<BeetExerciseLog>>

  @Query("SELECT * FROM BeetExerciseLog WHERE exerciseId = :exerciseId")
  fun getLogsForExercise(exerciseId: Int): Flow<List<BeetExerciseLog>>

  @Query("SELECT * FROM BeetExerciseLog WHERE exerciseId = :exercise and log_day = :day")
  fun exerciseLogsFor(
      day: Int,
      exercise: Int,
  ): Flow<List<BeetExerciseLogWithResistances>>

  @Query(
      """
        SELECT *
        FROM BeetExerciseLog L
        INNER JOIN BeetExercise E ON E.id = L.exerciseId
        LEFT JOIN BeetActivityResistance R ON R.activity_id = L.id
        WHERE L.log_day = :day
        ORDER BY E.id, L.magnitude, R.resistance_kind
    """)
  fun getAllFlatActivityDataForDay(day: Int): Flow<List<ActivityGroupFlatRow>>

  @Query(
      """
        SELECT *
        FROM BeetExerciseLog L
        INNER JOIN BeetExercise E ON E.id = L.exerciseId
        LEFT JOIN BeetActivityResistance R ON R.activity_id = L.id
        WHERE L.exerciseId = :exerciseId AND L.log_day >= :since AND L.log_day < :before
        ORDER BY E.id, L.magnitude, R.resistance_kind
    """)
  suspend fun getFlatActivityDataForExerciseSince(
      exerciseId: Int,
      since: Int,
      before: Int
  ): List<ActivityGroupFlatRow>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertResistances(resistances: List<BeetActivityResistance>)

  @Query("DELETE FROM BeetActivityResistance where activity_id = :logId")
  suspend fun deleteLogsFor(logId: Int)

  @Transaction
  suspend fun insertActivityAndResistances(
      log: BeetExerciseLog,
      resistances: List<BeetActivityResistance>
  ) {
    val logId = insertLog(log)
    if (resistances.isNotEmpty()) {
      insertResistances(resistances.map { it.copy(activityId = logId.toInt()) })
    }
  }

  @Transaction
  suspend fun updateLogAndResistances(
      log: BeetExerciseLog,
      selectedResistances: SnapshotStateMap<Int, Int>
  ) {
    update(log)
    deleteLogsFor(log.id)
    insertResistances(
        selectedResistances.map { (id, value) ->
          BeetActivityResistance(activityId = log.id, resistanceKind = id, resistanceValue = value)
        })
  }

  @Query(
      """
          SELECT log_date as date, COUNT(*) as count 
          FROM BeetExerciseLog 
          WHERE log_day >= :since 
          GROUP BY log_day
          """)
  fun activityOverview(since: Int): Flow<List<ActivityOverviewPrimitive>>

  @Query(
      """
          SELECT log_date as date, exerciseId as exercise
          FROM BeetExerciseLog 
          WHERE log_day >= :since 
          GROUP BY log_day, exerciseId
          """)
  fun exerciseDateOverview(since: Int): Flow<List<ExerciseOccurredOnDatePrimitive>>

  @Query(
      "SELECT exerciseId, COUNT(*) as count, MAX(log_date) as lastDate FROM BeetExerciseLog GROUP BY exerciseId")
  fun getExerciseUsageCounts(): Flow<List<ExerciseUsagePrimitive>>

  @Query("SELECT DISTINCT log_day FROM BeetExerciseLog WHERE banner = 1")
  fun getBannerDates(): Flow<List<Int>>
}

@Dao
interface ExerciseNoteDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNote(note: ExerciseNote): Long

  @Update suspend fun updateNote(note: ExerciseNote)

  @Delete suspend fun deleteNote(note: ExerciseNote)

  @Query("SELECT * FROM ExerciseNote WHERE id = :id") fun getNote(id: Int): Flow<ExerciseNote>

  @Query("SELECT * FROM ExerciseNote WHERE note_day = :day LIMIT 1")
  fun getNoteForDay(day: Int): Flow<ExerciseNote?>

  @Query("SELECT * FROM ExerciseNote ORDER BY note_date DESC")
  fun getAllNotes(): Flow<List<ExerciseNote>>

  @Query("DELETE FROM ExerciseNote WHERE note_day = :day") suspend fun deleteNoteForDay(day: Int)
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
}
