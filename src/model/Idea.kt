package com.example.model

import arsh.dzdback.model.Vote
import java.time.LocalDateTime
import kotlin.collections.HashMap

data class Idea(
    val id: Long,
    val authorId: Long,
    val content: String? = null,
    val created: LocalDateTime = LocalDateTime.now(),
    val votes: MutableMap<Long,Vote> = HashMap<Long,Vote>(),
    val link: String? = null,
    val attachment: Media? = null
)

