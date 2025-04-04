package com.iti.java.egyweather.Model.BOJO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)