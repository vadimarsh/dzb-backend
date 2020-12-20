package com.example.services

import arsh.dzdback.model.VoteType
import com.example.dto.IdeaRequestDto
import com.example.dto.IdeaResponseDto
import com.example.dto.UserResponseDto
import com.example.exception.InvalidOwnerException
import com.example.model.*
import com.example.repository.IdeasRepository
import io.ktor.features.*


class IdeaService(private val repo: IdeasRepository, private val userService: UserService, private val resultSize: Int) {
    suspend fun getAll(myId: Long): List<IdeaResponseDto> {
        return combineIdeasDto(repo.getAll(), myId)
    }

    suspend fun getRecent(myId: Long): List<IdeaResponseDto> {
        val posts = repo.getAll().take(resultSize)
        return combineIdeasDto(posts, myId)
    }

    suspend fun getBefore(id: Long, myId: Long): List<IdeaResponseDto> {
        val posts = repo.getAll().asSequence().filter { it.id < id }.take(resultSize).toList()
        return combineIdeasDto(posts, myId)
    }

    suspend fun getAfter(id: Long, myId: Long): List<IdeaResponseDto> {
        val posts = repo.getAll().asSequence().filter { it.id > id }.take(resultSize).toList()
        return combineIdeasDto(posts, myId)
    }

    suspend fun getById(id: Long, myId: Long): Idea {
        return repo.getById(id) ?: throw NotFoundException()
    }
    suspend fun getByAuthorId(myId: Long): List<IdeaResponseDto> {
        val model = repo.getByAuthorId(myId) ?: throw NotFoundException()
        return combineIdeasDto(model, myId)
    }
    suspend fun save(input: IdeaRequestDto, myId: Long): IdeaResponseDto {

        val model = Idea(
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
        val post = repo.save(model)
        val owners = listOf(userService.getById(myId))
        return mapToIdeaDto(post, owners, myId)
    }

    suspend fun delete(id: Long, myId: Long) {
        val post = repo.getById(id)
        if (post != null && post.authorId == myId) {
            repo.removeById(id)
        } else {
            throw InvalidOwnerException()
        }
    }

    suspend fun like(id: Long, myId: Long): IdeaResponseDto {
        val me = userService.getById(myId)
        return combineIdeaDto(repo.likeById(id, myId), myId)
    }

    suspend fun dislike(id: Long, myId: Long): IdeaResponseDto {
        return combineIdeaDto(repo.dislikeById(id, myId), myId)
    }



    private fun mapToIdeaDto(
        idea: Idea,
        owners: List<UserResponseDto>,
        myId: Long
    ): IdeaResponseDto {
        return IdeaResponseDto.fromModel(
            model = idea,
            owner = owners.find { it.id == idea.authorId } ?: UserResponseDto.unknown(),
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
         val owners = userService.getByIds(listOfNotNull(idea.authorId))
        val postDto = mapToIdeaDto(idea, owners, myId)
        return postDto
    }

    private suspend fun combineIdeasDto(
        ideas: List<Idea>,
        myId: Long
    ): List<IdeaResponseDto> {
        val ownerIds = (ideas).map { it.authorId }
        val owners = userService.getByIds(ownerIds)
        val postsDto = ideas.map { mapToIdeaDto(it, owners, myId) }

        return postsDto
    }
}