package com.coco.beetup.core.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class BeetRepository(
    private val beetProfileDao: BeetProfileDao,
    private val beetExerciseDao: BeetExerciseDao,
    private val exerciseLogDao: ExerciseLogDao
) {
    fun getProfile(): Flow<BeetProfile> = beetProfileDao.getProfile()

    fun getAllExercises(): Flow<List<BeetExercise>> = beetExerciseDao.getAllExercises()

    fun getAllLogs(): Flow<List<BeetExerciseLog>> = exerciseLogDao.getAllLogs()

    suspend fun insertExercise(exercise: BeetExercise) {
        beetExerciseDao.insert(exercise)
    }

    suspend fun deleteExercise(exercise: BeetExercise) {
        beetExerciseDao.delete(exercise)
    }

    suspend fun deleteExercises(exercises: Collection<BeetExercise>) {
        beetExerciseDao.delete(exercises.toList())
    }

    suspend fun insertLog(log: BeetExerciseLog) {
        exerciseLogDao.insert(log)
    }

    fun todaysLogs() = exerciseLogDao.getLogsForDay((Date().time / 86_400_000L).toInt())

    suspend fun deleteLogEntries(exercises: Collection<BeetExerciseLog>) {
        exerciseLogDao.delete(exercises.toList())
    }
}
