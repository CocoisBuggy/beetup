package com.coco.beetup.core.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class Converters {
  @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

  @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time?.toLong()
  
  @TypeConverter
  fun fromTimestampToLocalDateTime(value: Long?): LocalDateTime? {
    return value?.let {
      LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    }
  }

  @TypeConverter
  fun localDateTimeToTimestamp(date: LocalDateTime?): Long? {
    return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
  }
}
