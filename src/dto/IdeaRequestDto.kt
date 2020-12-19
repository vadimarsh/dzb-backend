package com.example.dto

data class IdeaRequestDto(
        val id: Long,
        val content: String? = null,
        val attachmentLink: String? = null,
        val attachmentImage: String? = null
)