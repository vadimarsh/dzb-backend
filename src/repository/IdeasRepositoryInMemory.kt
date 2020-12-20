package com.example.repository

import arsh.dzdback.model.Vote
import arsh.dzdback.model.VoteType
import com.example.model.Idea
import io.ktor.features.*

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

class IdeasRepositoryInMemory() : IdeasRepository {
    private var nextId = 1L
    private val items = mutableListOf<Idea>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<Idea> {
        mutex.withLock {
            return items.reversed()
        }
    }

    override suspend fun getById(id: Long): Idea? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<Idea> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun save(item: Idea): Idea {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    val copy = item.copy()
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(id: Long, authorId: Long): Idea {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> throw NotFoundException()
                else -> {
                    val item = items[index]
                    val copy: Idea?
                    //    idea.votes[myId]!!.type == VoteType.LIKE

                    if (!item.votes.contains(authorId)) {
                        copy = item.copy(votes = HashMap(item.votes).apply { put(authorId, Vote(authorId = authorId,type = VoteType.LIKE,date = LocalDateTime.now())) })
                        items[index] = copy
                    } else {
                        copy = item
                    }
                    copy
                }
            }
        }
    }
    override suspend fun dislikeById(id: Long, authorId: Long): Idea {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> throw NotFoundException()
                else -> {
                    val item = items[index]
                    val copy: Idea?
                    //    idea.votes[myId]!!.type == VoteType.LIKE

                    if (!item.votes.contains(authorId)) {
                        copy = item.copy(votes = HashMap(item.votes).apply { put(authorId, Vote(authorId = authorId,type = VoteType.DISLIKE,date = LocalDateTime.now())) })
                        items[index] = copy
                    } else {
                        copy = item
                    }
                    copy
                }
            }
        }
    }
    /*override suspend fun dislikeById(id: Long, authorId: Long): Idea {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> throw NotFoundException()
                else -> {
                    val item = items[index]
                    val copy: Idea?
                    if (item.votes.contains(authorId)) {
                        copy = item.copy(votes = HashMap(item.votes).apply { put(authorId, Vote(authorId = authorId,type = VoteType.DISLIKE,date = LocalDateTime.now())) })
                        items[index] = copy
                    } else {
                        copy = item
                    }
                    copy
                }
            }
        }
    }*/
    override suspend fun getByAuthorId(idAuthor: Long): List<Idea> =
        items.filter { it.authorId == idAuthor }
                .sortedWith(compareBy { it.created }).reversed()
}


