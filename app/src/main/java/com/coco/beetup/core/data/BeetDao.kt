package com.coco.beetup.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeetProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: BeetProfile)

    @Query("SELECT * FROM BeetProfile LIMIT 1")
    fun getProfile(): Flow<BeetProfile>
}

@Dao
interface BeetExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg exercises: BeetExercise)

    @Delete
    suspend fun delete(exercise: BeetExercise)

    @Delete
    suspend fun delete(exercises: List<BeetExercise>)

    @Query("SELECT * FROM BeetExercise WHERE id = :id")
    fun getExercise(id: Int): Flow<BeetExercise>

    @Query("SELECT * FROM BeetExercise")
    fun getAllExercises(): Flow<List<BeetExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg magnitudes: BeetMagnitude)

    @Query("SELECT * FROM BeetMagnitude WHERE id = :id")
    fun getMagnitude(id: Int): Flow<BeetMagnitude>

    @Query("SELECT * FROM BeetMagnitude")
    fun getAllMagnitudes(): Flow<List<BeetMagnitude>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg resistances: BeetResistance)

    @Query("SELECT * FROM BeetResistance WHERE id = :id")
    fun getResistance(id: Int): Flow<BeetResistance>

    @Query("SELECT * FROM BeetResistance")
    fun getAllResistances(): Flow<List<BeetResistance>>
}

@Dao
interface ExerciseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg logs: BeetExerciseLog)

    @Delete
    suspend fun delete(activities: List<BeetExerciseLog>)

    @Query("SELECT * FROM BeetExerciseLog WHERE id = :id")
    fun getLog(id: Int): Flow<BeetExerciseLog>

    @Query("SELECT * FROM BeetExerciseLog")
    fun getAllLogs(): Flow<List<BeetExerciseLog>>

    @Query("SELECT * FROM BeetExerciseLog where log_day = :day")
    fun getLogsForDay(day: Int): Flow<List<BeetExerciseLog>>

    @Query("SELECT * FROM BeetExerciseLog WHERE exerciseId = :exerciseId")
    fun getLogsForExercise(exerciseId: Int): Flow<List<BeetExerciseLog>>
}
