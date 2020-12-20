package com.example.repository

import com.example.model.Idea

interface IdeasRepository {
    suspend fun getAll(): List<Idea>
    suspend fun getById(id: Long): Idea?
    suspend fun getByIds(ids: Collection<Long>): List<Idea>
    suspend fun save(item: Idea): Idea
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, authorId: Long): Idea
    suspend fun dislikeById(id: Long, authorId: Long): Idea
    suspend fun getByAuthorId(idAuthor: Long):List<Idea>

}