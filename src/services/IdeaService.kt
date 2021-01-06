package com.example.services

import arsh.dzdback.model.VoteType
import arsh.dzdback.services.FCMService
import com.example.dto.IdeaRequestDto
import com.example.dto.IdeaResponseDto
import com.example.dto.UserResponseDto
import com.example.exception.InvalidOwnerException
import com.example.model.*
import com.example.repository.IdeasRepository
import io.ktor.features.*


class IdeaService(private val repo: IdeasRepository, private val userService: UserService, private val fcmService: FCMService, private val resultSize: Int) {
    suspend fun getAll(myId: Long): List<IdeaResponseDto> {
        return combineIdeasDto(repo.getAll(), myId)
    }

    suspend fun getRecent(myId: Long): List<IdeaResponseDto> {
        val ideas = repo.getAll().take(resultSize)
        return combineIdeasDto(ideas, myId)
    }

    suspend fun getBefore(id: Long, myId: Long): List<IdeaResponseDto> {
        val ideas = repo.getAll().asSequence().filter { it.id < id }.take(resultSize).toList()
        return combineIdeasDto(ideas, myId)
    }

    suspend fun getAfter(id: Long, myId: Long): List<IdeaResponseDto> {
        val ideas = repo.getAll().asSequence().filter { it.id > id }.take(resultSize).toList()
        return combineIdeasDto(ideas, myId)
    }

    suspend fun getById(id: Long, myId: Long): Idea {
        return repo.getById(id) ?: throw NotFoundException()
    }

    suspend fun getByAuthorId(myId: Long): List<IdeaResponseDto> {
        val model = repo.getByAuthorId(myId)
        return combineIdeasDto(model, myId)
    }

    suspend fun save(input: IdeaRequestDto, myId: Long): IdeaResponseDto {

        val idea = Idea(
                id = input.id,
                authorId = myId,
                content = input.content,
                link = input.attachmentLink,
                attachment = input.attachmentImage?.let {
                    Media(input.attachmentImage, mediaType = MediaType.IMAGE)
                })
        if (input.id != -1L) {
            val existing = repo.getById(input.id)!!
            if (existing.authorId != myId) {
                throw InvalidOwnerException()
            }
        }
        val authors = listOf(userService.getById(myId))
        return mapToIdeaDto(repo.save(idea), authors, myId)
    }

    suspend fun delete(id: Long, myId: Long) {
        val idea = repo.getById(id)
        if (idea != null && idea.authorId == myId) {
            repo.removeById(id)
        } else {
            throw InvalidOwnerException()
        }
    }

    suspend fun like(id: Long, myId: Long): IdeaResponseDto {
        val me = userService.getById(myId)

        val idea = repo.getById(id)

        if (idea != null) {
            val author = userService.getById(idea.authorId)

            if (!author.fbToken.isNullOrEmpty()) {
                fcmService.send(author.id, author.fbToken, "Ваша идея понравилась ${me.username}")
            }
        }
        return combineIdeaDto(repo.likeById(id, myId), myId)
    }

    suspend fun dislike(id: Long, myId: Long): IdeaResponseDto {
        val me = userService.getById(myId)
        val idea = repo.getById(id)
        if (idea != null) {
            val author = userService.getById(idea.authorId)

            if (!author.fbToken.isNullOrEmpty()) {
                fcmService.send(author.id, author.fbToken, "Ваша идея не понравилась ${me.username}")
            }
        }
        return combineIdeaDto(repo.dislikeById(id, myId), myId)
    }


    private fun mapToIdeaDto(
            idea: Idea,
            authors: List<UserResponseDto>,
            myId: Long
    ): IdeaResponseDto {
        return IdeaResponseDto.fromModel(
                model = idea,
                owner = authors.find { it.id == idea.authorId } ?: UserResponseDto.unknown(),
                likedByMe = if (idea.votes.contains(myId)) {
                    idea.votes[myId]!!.type == VoteType.LIKE
                } else {
                    false
                },
                dislikedByMe = if (idea.votes.contains(myId)) {
                    idea.votes[myId]!!.type == VoteType.DISLIKE
                } else {
                    false
                }
        )
    }

    private suspend fun combineIdeaDto(
            idea: Idea,
            myId: Long
    ): IdeaResponseDto {
        val author = userService.getByIds(listOfNotNull(idea.authorId))
        return mapToIdeaDto(idea, author, myId)
    }

    private suspend fun combineIdeasDto(
            ideas: List<Idea>,
            myId: Long
    ): List<IdeaResponseDto> {
        val autorsIds = (ideas).map { it.authorId }
        val authors = userService.getByIds(autorsIds)

        return ideas.map { mapToIdeaDto(it, authors, myId) }
    }
}