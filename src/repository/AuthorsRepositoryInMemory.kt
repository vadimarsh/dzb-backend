package com.example.repository

import arsh.dzdback.model.Vote
import com.example.model.Author
import com.example.model.Idea
import com.example.model.Media
import com.example.services.IdeaService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthorsRepositoryInMemory :AuthorsRepository {
    private var nextId = 1L
    private val items = mutableListOf<Author>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<Author> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Long): Author? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<Author> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun getByUsername(username: String): Author? {
        mutex.withLock {
            return items.find { it.username == username }
        }
    }

    override suspend fun save(item: Author): Author {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    val copy = items[index].copy(username = item.username, password = item.password)
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun getVotes(idea: Idea): List<Vote> {
            val listUsers = mutableListOf<Vote>()
            idea.votes.forEach {
                val user = getById(it.key)
                if (user != null) {
                    listUsers.add(it.value)
                }
            }
            return listUsers.sortedWith(compareBy { it.date }).reversed()
        }

    override suspend fun setAvatar(author: Author, media: Media):Author {
        val index = items.indexOfFirst { it.id == author.id }
        return mutex.withLock {
            val copy = items[index].copy(avatar = media)
            items[index] = copy
            copy
        }
    }

    override suspend fun saveFirebaseToken(id: Long, token: String): Author? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> {
                null
            }
            else -> {
                mutex.withLock {
                    val copy = items[index].copy(fBtoken = token)
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun checkReadOnly(userId: Long, ideaService: IdeaService): Boolean {
        val index = items.indexOfFirst { it.id == userId }
        val authorsIdeas = ideaService.getByAuthorId(userId)
        authorsIdeas.forEach() {
            if (it.dislikes >= 5 && it.likes == 0) {         // > 100
                if (!items[index].onlyReader) {
                    mutex.withLock {
                        val copy = items[index].copy(onlyReader = true)
                        items[index] = copy
                        return items[index].onlyReader
                    }
                }

            }
        }
        val copy = items[index].copy(onlyReader = false)
        items[index] = copy
        return items[index].onlyReader
    }
}
