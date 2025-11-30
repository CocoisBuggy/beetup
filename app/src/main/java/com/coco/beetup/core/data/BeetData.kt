package com.coco.beetup.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [BeetProfile::class], version = 1)
@TypeConverters(Converters::class)
abstract class BeetData : RoomDatabase() {
}