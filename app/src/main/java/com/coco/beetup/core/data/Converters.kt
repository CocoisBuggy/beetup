package com.coco.beetup.core.data

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class Converters {
  @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

  @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time?.toLong()

  @TypeConverter
  fun fromTimestampToLocalDateTime(value: Long?): LocalDateTime? {
    return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
  }

  @TypeConverter
  fun localDateTimeToTimestamp(date: LocalDateTime?): Long? {
    return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
  }

  @TypeConverter
  fun fromScheduleKind(value: String?): ScheduleKind? {
    return value?.let { ScheduleKind.valueOf(it) }
  }

  @TypeConverter
  fun scheduleKindToString(value: ScheduleKind?): String? {
    return value?.name
  }

  @TypeConverter
  fun fromReminderStrength(value: String?): ReminderStrength? {
    return value?.let { ReminderStrength.valueOf(it) }
  }

  @TypeConverter
  fun reminderStrengthToString(value: ReminderStrength?): String? {
    return value?.name
  }

  @TypeConverter fun fromDayOfWeek(value: DayOfWeek): Int = value.value

  @TypeConverter fun toDayOfWeek(value: Int): DayOfWeek = DayOfWeek.of(value)

  @TypeConverter
  fun fromLocalDate(value: LocalDate?): Long? {
    return value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
  }

  @TypeConverter
  fun toLocalDate(value: Long?): LocalDate? {
    return value?.let { LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
  }
}
