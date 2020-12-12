package com.example.dto

import com.example.model.Media
import com.example.model.MediaType


data class MediaResponseDto(val id: String, val mediaType: MediaType) {
    companion object {
        fun fromModel(model: Media) = MediaResponseDto(
                id = model.id,
                mediaType = model.mediaType
        )
    }
}