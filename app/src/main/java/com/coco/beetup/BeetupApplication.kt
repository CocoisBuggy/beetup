package com.coco.beetup

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.coco.beetup.core.data.BeetData
import com.coco.beetup.core.data.BeetRepository
import java.io.BufferedReader
import java.io.InputStreamReader

class BeetupApplication : Application() {

  val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE BeetExerciseLog ADD COLUMN rest_seconds INTEGER")
      }
  }

  val database: BeetData by lazy {
    Room.databaseBuilder(
            applicationContext,
            BeetData::class.java,
            "beet_database",
        )
        .addMigrations(MIGRATION_1_2)
        .addCallback(
            object : RoomDatabase.Callback() {
              override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Read and execute SQL statements from the raw resource file
                val inputStream = resources.openRawResource(R.raw.defaults)
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                  lines.forEach { line ->
                    if (line.isNotBlank() && line.startsWith("--").not()) {
                      db.execSQL(line)
                    }
                  }
                }
              }
            })
        .build()
  }

  val repository: BeetRepository by lazy {
    BeetRepository(
        database,
        database.beetProfileDao(),
        database.beetExerciseDao(),
        database.exerciseLogDao(),
    )
  }
}
