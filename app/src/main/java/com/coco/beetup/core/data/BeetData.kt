package com.coco.beetup.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 6,
    entities =
        [
            BeetProfile::class,
            BeetExercise::class,
            BeetMagnitude::class,
            BeetResistance::class,
            BeetExerciseLog::class,
            BeetActivityResistance::class,
            ValidBeetResistances::class,
            ExerciseNote::class,
            BeetExerciseSchedule::class],
)
@TypeConverters(Converters::class)
abstract class BeetData : RoomDatabase() {
  abstract fun beetProfileDao(): BeetProfileDao

  abstract fun beetExerciseDao(): BeetExerciseDao

  abstract fun exerciseLogDao(): ExerciseLogDao

  abstract fun exerciseNoteDao(): ExerciseNoteDao

  abstract fun beetExerciseScheduleDao(): BeetExerciseScheduleDao
}
