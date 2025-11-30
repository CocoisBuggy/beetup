package com.coco.beetup.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BeetProfile(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "profile_name") val profileName: String,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "birth_year") val birthYear: Int,
    )