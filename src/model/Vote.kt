package arsh.dzdback.model

import com.example.model.Author

data class Vote(val date:Long, val type: VoteType)

enum class VoteType{
    LIKE,DISLIKE
}