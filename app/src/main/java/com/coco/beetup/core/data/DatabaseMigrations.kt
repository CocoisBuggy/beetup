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

  val MIGRATION_4_5 =
      object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
          // Create the new BeetExerciseSchedule table
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS `BeetExerciseSchedule` (" +
                  "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                  "`exercise_id` INTEGER NOT NULL, " +
                  "`kind` TEXT NOT NULL, " +
                  "`reminder` TEXT, " +
                  "`follows_exercise` INTEGER, " +
                  "`day_of_week` INTEGER, " +
                  "`monotonic_days` INTEGER, " +
                  "`message` TEXT, " +
                  "FOREIGN KEY(`exercise_id`) REFERENCES `BeetExercise`(`id`) ON DELETE CASCADE, " +
                  "FOREIGN KEY(`follows_exercise`) REFERENCES `BeetExercise`(`id`) ON DELETE CASCADE" +
                  ")")

          // Create index for exercise_id column for better query performance
          db.execSQL(
              "CREATE INDEX IF NOT EXISTS `index_BeetExerciseSchedule_exercise_id` ON `BeetExerciseSchedule` (`exercise_id`)")
        }
      }

    val MIGRATION_5_6 =
      object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE BeetExerciseSchedule ADD COLUMN enabled INTEGER NOT NULL DEFAULT 1")
          db.execSQL("ALTER TABLE BeetExerciseSchedule ADD COLUMN show_after INTEGER")
          db.execSQL("ALTER TABLE BeetExerciseSchedule ADD COLUMN dismissed INTEGER NOT NULL DEFAULT 1")
        }
      }

  val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
}
