package com.example.model

import io.ktor.auth.*

data class Author(
        val id: Long = 0,
        val username: String,
        val password: String,
        val avatar:String = ""
) : Principal