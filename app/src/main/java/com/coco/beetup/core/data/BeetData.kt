package com.coco.beetup.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities =
        [
            BeetProfile::class,
            BeetExercise::class,
            BeetMagnitude::class,
            BeetResistance::class,
            BeetExerciseLog::class,
            BeetActivityResistance::class,
            ValidBeetResistances::class,
        ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class BeetData : RoomDatabase() {
  abstract fun beetProfileDao(): BeetProfileDao

  abstract fun beetExerciseDao(): BeetExerciseDao

  abstract fun exerciseLogDao(): ExerciseLogDao
}
