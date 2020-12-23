package com.example.dto

import com.example.model.Author


data class UserResponseDto(val id: Long, val name: String, val avatar: MediaResponseDto?) {
    companion object {
        fun fromModel(model: Author) = UserResponseDto(
                id = model.id,
                name = model.username,
                avatar = model.avatar?.let { MediaResponseDto.fromModel(model.avatar) }
        )

        fun unknown() = UserResponseDto(
                id = 0,
                name = "unknown",
                avatar = null
        )
    }
}
