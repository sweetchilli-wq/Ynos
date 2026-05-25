package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    @Query("SELECT * FROM comics ORDER BY addedTimestamp DESC")
    fun getAllComics(): Flow<List<Comic>>

    @Query("SELECT * FROM comics WHERE id = :id")
    suspend fun getComicById(id: Int): Comic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComic(comic: Comic): Long

    @Update
    suspend fun updateComic(comic: Comic)

    @Query("UPDATE comics SET currentPage = :page WHERE id = :id")
    suspend fun updateProgress(id: Int, page: Int)

    @Delete
    suspend fun deleteComic(comic: Comic)

    @Query("DELETE FROM comics WHERE id = :id")
    suspend fun deleteComicById(id: Int)
}
