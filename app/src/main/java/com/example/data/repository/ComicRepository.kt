package com.example.data.repository

import com.example.data.db.Comic
import com.example.data.db.ComicDao
import kotlinx.coroutines.flow.Flow

class ComicRepository(private val comicDao: ComicDao) {
    val allComics: Flow<List<Comic>> = comicDao.getAllComics()

    suspend fun getComicById(id: Int): Comic? {
        return comicDao.getComicById(id)
    }

    suspend fun insertComic(comic: Comic): Long {
        return comicDao.insertComic(comic)
    }

    suspend fun updateComic(comic: Comic) {
        comicDao.updateComic(comic)
    }

    suspend fun updateProgress(id: Int, page: Int) {
        comicDao.updateProgress(id, page)
    }

    suspend fun deleteComicById(id: Int) {
        comicDao.deleteComicById(id)
    }
}
