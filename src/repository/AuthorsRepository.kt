package com.example.repository

import arsh.dzdback.model.Vote
import com.example.model.Author
import com.example.model.Idea
import com.example.model.Media

interface AuthorsRepository {
    suspend fun getAll(): List<Author>
    suspend fun getById(id: Long): Author?
    suspend fun getByIds(ids: Collection<Long>): List<Author>
    suspend fun getByUsername(username: String): Author?
    suspend fun save(item: Author): Author
    suspend fun getVotes(idea: Idea): List<Vote>
    suspend fun setAvatar(user: Author, media: Media):Author
}
