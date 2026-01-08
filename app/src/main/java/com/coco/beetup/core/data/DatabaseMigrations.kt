package com.coco.beetup.core.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
  val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE BeetExerciseLog ADD COLUMN rest_seconds INTEGER")
        }
      }

  val MIGRATION_2_3 =
      object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS ExerciseNote (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                note_date INTEGER NOT NULL,
                note_day INTEGER NOT NULL,
                note_text TEXT NOT NULL
              )
            """
                  .trimIndent())
        }
      }

  val MIGRATION_3_4 =
      object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE BeetExerciseLog ADD COLUMN banner INTEGER NOT NULL DEFAULT 0")
        }
      }

  val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
