package com.example.dto

import arsh.dzdback.model.Vote
import arsh.dzdback.model.VoteType
import com.example.model.Idea
import java.time.format.DateTimeFormatter


data class IdeaResponseDto(
        val id: Long,
        val authorName: String,
        val authorId: Long,
        val avatar: MediaResponseDto?,
        val created: String,
        val content: String? = null,
        val likes: Int,
        val dislikes: Int,
        val likedByMe: Boolean = false,
        val dislikedByMe: Boolean = false,
        val link: String? = null,
        val attachment: MediaResponseDto?
) {
    companion object {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY")
        fun fromModel(model: Idea, owner: UserResponseDto, likedByMe: Boolean = false, dislikedByMe: Boolean = false) = IdeaResponseDto(
                id = model.id,
                authorId = owner.id,
                authorName = owner.username,
                avatar = owner.avatar,
                content = model.content,
                created = model.created.format(formatter),
                likes = model.votes.count {entry: Map.Entry<Long, Vote> ->  entry.value.type==VoteType.LIKE},
                dislikes = model.votes.count {entry: Map.Entry<Long, Vote> ->  entry.value.type==VoteType.DISLIKE},
                likedByMe = likedByMe,
                dislikedByMe = dislikedByMe,
                link = model.link,
                attachment = model.attachment?.let { MediaResponseDto.fromModel(model.attachment) }
        )
    }
}
