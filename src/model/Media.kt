package com.example.model

enum class MediaType {
    IMAGE
}

data class Media(val id: String, val mediaType: MediaType)