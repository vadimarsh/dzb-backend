package arsh.dzdback.model

import com.example.model.Author
import java.time.LocalDateTime

data class Vote(val type: VoteType, val authorId: Long,val date: LocalDateTime)

enum class VoteType{
    LIKE,DISLIKE
}