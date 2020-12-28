package com.example.services

import arsh.dzdback.dto.VoteResponseDto
import com.example.dto.*
import com.example.exception.InvalidPasswordException
import com.example.exception.PasswordChangeException
import com.example.exception.UserNameExistException
import com.example.model.Author
import com.example.model.Media
import com.example.repository.AuthorsRepository
import io.ktor.features.*
import org.springframework.security.crypto.password.PasswordEncoder


class UserService(
        private val repo: AuthorsRepository,
        private val tokenService: JWTTokenService,
        private val passwordEncoder: PasswordEncoder
) {
    suspend fun getModelById(id: Long): Author? {
        return repo.getById(id)
    }

    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }

    suspend fun getByIds(ids: Collection<Long>): List<UserResponseDto> {
        return repo.getByIds(ids).map { UserResponseDto.fromModel(it) }
    }

    suspend fun changePassword(id: Long, input: PasswordChangeRequestDto): AuthenticationResponseDto  {
        val model = repo.getById(id) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
        val token = tokenService.generate(model.id)
        return AuthenticationResponseDto(token)
    }

    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model.id)
        return AuthenticationResponseDto(token)
    }
    suspend fun addAvatar(userId: Long, media: Media) {
        val user = repo.getById(userId) ?: throw NotFoundException()
        repo.setAvatar(user, media)
    }
    suspend fun save(username: String, password: String): RegisterResponseDto {
        if (repo.getByUsername(username) != null) {
            throw UserNameExistException("Пользователь с таким именем уже есть")
        }
        val newAuthor = repo.save(Author(username = username, password = passwordEncoder.encode(password)))
        val token = tokenService.generate(newAuthor.id)
        return RegisterResponseDto(token)
    }
    suspend fun getVotesByIdeaId(postId: Long, me: Long, ideaService: IdeaService): List<VoteResponseDto>{
        val idea = ideaService.getById(postId,me)
        return repo.getVotes(idea).map { VoteResponseDto.fromModel(it, this, repo.getById(it.authorId)!!) }
    }

    suspend fun saveFirebaseToken(id: Long, token: String) = repo.saveFirebaseToken(id, token)
            ?: throw NotFoundException()

}