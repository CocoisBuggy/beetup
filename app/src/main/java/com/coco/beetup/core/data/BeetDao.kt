package com.coco.beetup.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.Date
import kotlinx.coroutines.flow.Flow

data class ActivityOverviewPrimitive(val date: Date, val count: Int)

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
}

@Dao
interface ExerciseLogDao {
  @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertLog(log: BeetExerciseLog): Long

  @Delete suspend fun delete(activities: List<BeetExerciseLog>)

  @Query("SELECT * FROM BeetExerciseLog WHERE id = :id") fun getLog(id: Int): Flow<BeetExerciseLog>

  @Query("SELECT * FROM BeetExerciseLog") fun getAllLogs(): Flow<List<BeetExerciseLog>>

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

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertResistances(resistances: List<BeetActivityResistance>)

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

  @Query(
      """
          SELECT log_date as date, COUNT(*) as count 
          FROM BeetExerciseLog 
          WHERE log_day >= :since 
          GROUP BY log_day
          """)
  fun activityOverview(since: Int): Flow<List<ActivityOverviewPrimitive>>
}
