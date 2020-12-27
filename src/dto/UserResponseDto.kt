package com.example.dto

import com.example.model.Author


data class UserResponseDto(val id: Long, val username: String, val avatar: MediaResponseDto?, val fbToken:String?, val onlyReader:Boolean = false) {
    companion object {
        fun fromModel(model: Author,reader:Boolean) = UserResponseDto(
                id = model.id,
                username = model.username,
                avatar = model.avatar?.let { MediaResponseDto.fromModel(model.avatar)},
                fbToken = model.fBtoken,
                onlyReader = reader
        )

        fun unknown() = UserResponseDto(
                id = 0,
                username = "unknown",
                avatar = null,
                fbToken = null, //fbtoken
                onlyReader = false
        )
    }
}
