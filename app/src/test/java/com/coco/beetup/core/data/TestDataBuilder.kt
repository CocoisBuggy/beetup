package com.coco.beetup.core.data

/**
 * Test data builders for creating test instances of data classes. This helps reduce boilerplate in
 * test files and makes tests more readable.
 */
object TestDataBuilder {

  // Exercise builders
  fun buildTestExercise(
      id: Int = 1,
      name: String = "Test Exercise",
      description: String = "Test Description",
      magnitudeKind: Int = 1
  ) =
      BeetExercise(
          id = id,
          exerciseName = name,
          exerciseDescription = description,
          magnitudeKind = magnitudeKind)

  fun buildPushUpExercise(id: Int = 1) =
      BeetExercise(
          id = id,
          exerciseName = "Push-ups",
          exerciseDescription = "Upper body exercise",
          magnitudeKind = 1 // Repetitions
          )

  fun buildBenchPressExercise(id: Int = 2) =
      BeetExercise(
          id = id,
          exerciseName = "Bench Press",
          exerciseDescription = "Upper body strength exercise",
          magnitudeKind = 1 // Repetitions
          )

  fun buildSquatExercise(id: Int = 3) =
      BeetExercise(
          id = id,
          exerciseName = "Squats",
          exerciseDescription = "Lower body exercise",
          magnitudeKind = 1 // Repetitions
          )

  // Magnitude builders
  fun buildTestMagnitude(
      id: Int = 1,
      name: String = "Repetitions",
      description: String = "Number of repetitions",
      unit: String = "reps"
  ) = BeetMagnitude(id = id, name = name, description = description, unit = unit)

  fun buildDurationMagnitude(id: Int = 2) =
      BeetMagnitude(id = id, name = "Duration", description = "Exercise duration", unit = "minutes")

  // Resistance builders
  fun buildTestResistance(
      id: Int = 1,
      name: String = "Test Resistance",
      description: String = "Test resistance",
      unit: String = "kg"
  ) = BeetResistance(id = id, name = name, description = description, unit = unit)

  fun buildBodyWeightResistance(id: Int = 1) =
      BeetResistance(
          id = id, name = "Body Weight", description = "Body weight resistance", unit = "kg")

  fun buildBarbellResistance(id: Int = 2) =
      BeetResistance(id = id, name = "Barbell", description = "Barbell resistance", unit = "kg")

  fun buildDumbbellResistance(id: Int = 3) =
      BeetResistance(id = id, name = "Dumbbell", description = "Dumbbell resistance", unit = "kg")

  // Exercise Log builders
  fun buildTestExerciseLog(
      id: Int = 1,
      exerciseId: Int = 1,
      magnitude: Int = 10,
      logDay: Int = 19500,
      comment: String? = null,
      rest: Int? = null,
      banner: Boolean = false
  ) =
      BeetExerciseLog(
          id = id,
          exerciseId = exerciseId,
          magnitude = magnitude,
          logDay = logDay,
          comment = comment,
          rest = rest,
          banner = banner)

  fun buildPushUpLog(id: Int = 1, repetitions: Int = 15, logDay: Int = 19500) =
      BeetExerciseLog(
          id = id,
          exerciseId = 1, // Assuming push-up exercise has ID 1
          magnitude = repetitions,
          logDay = logDay)

  fun buildBenchPressLog(id: Int = 2, repetitions: Int = 8, weight: Int = 60, logDay: Int = 19500) =
      BeetExerciseLog(
          id = id,
          exerciseId = 2, // Assuming bench press exercise has ID 2
          magnitude = repetitions,
          logDay = logDay)

  // Activity Resistance builders
  fun buildTestActivityResistance(
      activityId: Int = 1,
      resistanceKind: Int = 1,
      resistanceValue: Int = 50
  ) =
      BeetActivityResistance(
          activityId = activityId,
          resistanceKind = resistanceKind,
          resistanceValue = resistanceValue)

  fun buildBodyWeightResistanceEntry(activityId: Int = 1, weight: Int = 70) =
      BeetActivityResistance(
          activityId = activityId,
          resistanceKind = 1, // Assuming body weight resistance has ID 1
          resistanceValue = weight)

  fun buildBarbellResistanceEntry(activityId: Int = 1, weight: Int = 60) =
      BeetActivityResistance(
          activityId = activityId,
          resistanceKind = 2, // Assuming barbell resistance has ID 2
          resistanceValue = weight)

  // Activity Group Flat Row builders
  fun buildActivityGroupFlatRow(
      log: BeetExerciseLog = buildTestExerciseLog(),
      exercise: BeetExercise = buildTestExercise(),
      resistances: List<BeetActivityResistance> = emptyList()
  ) = ActivityGroupFlatRow(log = log, exercise = exercise, resistanceEntry = resistances)

  // Exercise Note builders
  fun buildTestExerciseNote(id: Int = 1, noteDay: Int = 19500, noteText: String = "Test note") =
      ExerciseNote(id = id, noteDay = noteDay, noteText = noteText)

  // Exercise Schedule builders
  fun buildTestExerciseSchedule(
      id: Int = 1,
      activityId: Int = 1,
      kind: ScheduleKind = ScheduleKind.DAY_OF_WEEK,
      reminder: com.coco.beetup.core.data.ReminderStrength? = null,
      followsExercise: Int? = null,
      dayOfWeek: Int? = 1,
      monotonicDays: Int? = null,
      message: String? = null
  ) =
      BeetExerciseSchedule(
          id = id,
          exerciseId = activityId,
          kind = kind,
          reminder = reminder,
          followsExercise = followsExercise,
          dayOfWeek = dayOfWeek,
          monotonicDays = monotonicDays,
          message = message)

  // Valid Resistances builders
  fun buildValidResistances(exerciseId: Int = 1, resistanceKind: Int = 1) =
      ValidBeetResistances(exerciseId = exerciseId, resistanceKind = resistanceKind)

  // Complete exercise session builders (log + resistances)
  fun buildCompleteExerciseSession(
      exercise: BeetExercise = buildTestExercise(),
      log: BeetExerciseLog = buildTestExerciseLog(),
      resistances: List<BeetActivityResistance> = emptyList()
  ): ActivityGroupFlatRow {
    return ActivityGroupFlatRow(
        log = log.copy(exerciseId = exercise.id),
        exercise = exercise,
        resistanceEntry = resistances.map { it.copy(activityId = log.id) })
  }

  fun buildPushUpSession(
      repetitions: Int = 15,
      bodyWeight: Int = 70,
      logDay: Int = 19500
  ): ActivityGroupFlatRow {
    val exercise = buildPushUpExercise()
    val log = buildPushUpLog(repetitions = repetitions, logDay = logDay)
    val resistance = buildBodyWeightResistanceEntry(activityId = log.id, weight = bodyWeight)

    return buildCompleteExerciseSession(exercise, log, listOf(resistance))
  }

  fun buildBenchPressSession(
      repetitions: Int = 8,
      barbellWeight: Int = 60,
      logDay: Int = 19500
  ): ActivityGroupFlatRow {
    val exercise = buildBenchPressExercise()
    val log = buildBenchPressLog(repetitions = repetitions, weight = barbellWeight, logDay = logDay)
    val resistance = buildBarbellResistanceEntry(activityId = log.id, weight = barbellWeight)

    return buildCompleteExerciseSession(exercise, log, listOf(resistance))
  }

  // Multiple sessions for testing grouping
  fun buildMultiplePushUpSessions(
      sessions: List<Pair<Int, Int>> // List of (repetitions, bodyWeight) pairs
  ): List<ActivityGroupFlatRow> {
    return sessions.mapIndexed { index, (reps, weight) ->
      buildPushUpSession(repetitions = reps, bodyWeight = weight, logDay = 19500 + index)
    }
  }

  fun buildSameDayPushUpSessions(
      sessions: List<Pair<Int, Int>> // List of (repetitions, bodyWeight) pairs for same day
  ): List<ActivityGroupFlatRow> {
    val exercise = buildPushUpExercise()
    return sessions.mapIndexed { index, (reps, weight) ->
      val log = buildPushUpLog(id = index + 1, repetitions = reps, logDay = 19500)
      val resistance = buildBodyWeightResistanceEntry(activityId = log.id, weight = weight)
      buildCompleteExerciseSession(exercise, log, listOf(resistance))
    }
  }
}
