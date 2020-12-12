package com.example.model

import arsh.dzdback.model.Vote
import kotlin.collections.HashMap

data class Idea(
    val id: Long,
    val authorId: Long,
    val content: String? = null,
    val created: Int = (System.currentTimeMillis() / 1000).toInt(),
        //val votes: Set<Vote> = HashSet(),
    val votes: MutableMap<Long,Vote> = HashMap<Long,Vote>(),
    val link: String? = null,
    val attachment: Media? = null
)

