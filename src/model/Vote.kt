package arsh.dzdback.model

data class Vote(val type: VoteType, val date:Int = (System.currentTimeMillis() / 1000).toInt())

enum class VoteType{
    LIKE,DISLIKE
}