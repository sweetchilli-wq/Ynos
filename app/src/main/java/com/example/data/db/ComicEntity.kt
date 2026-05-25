package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comics")
data class Comic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val filePath: String,
    val coverPath: String?,
    val artist: String,
    val volume: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis()
)
