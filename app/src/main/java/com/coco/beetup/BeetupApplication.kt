package com.coco.beetup

import android.app.Application
import androidx.room.Room
import com.coco.beetup.core.data.BeetData
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetMagnitude
import com.coco.beetup.core.data.BeetRepository
import com.coco.beetup.core.data.BeetResistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BeetupApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: BeetData by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            BeetData::class.java,
            "beet_database"
        )
            .build();

        applicationScope.launch {
            db.beetExerciseDao().insert(
                BeetMagnitude(1, "Reps", "Number of repetitions"),
                BeetMagnitude(2, "Distance", "Distance in meters"),
                BeetMagnitude(3, "Time", "Time in seconds")
            )
            db.beetExerciseDao().insert(
                BeetResistance(
                    1,
                    "Overhang",
                    "Some overhang expressed in degrees where 0 degrees is flat on the floor"
                ),
                BeetResistance(
                    2,
                    "Grams",
                    "Additional weight in the form of some number of grams"
                ),
                BeetResistance(3, "Elastic", "Elastic of some resistance factor")
            )
            db.beetExerciseDao().insert(
                BeetExercise(
                    1,
                    "Pull Up",
                    "Pull up exercise",
                    magnitudeKind = 1,
                    resistanceKind = 2
                ),
                BeetExercise(
                    1,
                    "Push Up",
                    "Push up exercise",
                    magnitudeKind = 1,
                    resistanceKind = 2
                ),
                BeetExercise(
                    1,
                    "Right hand max edge",
                    "Using an an edge and chalk, lift the weight",
                    magnitudeKind = 1,
                    resistanceKind = 2
                ),
                BeetExercise(
                    1,
                    "Left hand max edge",
                    "Using an an edge and chalk, lift the weight",
                    magnitudeKind = 1,
                    resistanceKind = 2
                ),
            )
        }

        return@lazy db
    }

    val repository: BeetRepository by lazy {
        BeetRepository(
            database.beetProfileDao(),
            database.beetExerciseDao(),
            database.exerciseLogDao()
        )
    }
}
