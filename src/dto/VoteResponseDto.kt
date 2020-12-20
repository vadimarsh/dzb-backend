package arsh.dzdback.dto

import arsh.dzdback.model.Vote
import arsh.dzdback.model.VoteType
import com.example.dto.MediaResponseDto
import com.example.model.Author
import com.example.services.UserService
import java.time.format.DateTimeFormatter

class VoteResponseDto (
        val date: String,
        val authorId: Long,
        val authorName: String,
            //val status: UserStatus,
        val action: VoteType,
        val avatar: MediaResponseDto?
    ) {
        companion object {
            suspend fun fromModel(model: Vote, userService: UserService, author: Author): VoteResponseDto {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY")
                return VoteResponseDto(
                        date = model.date.format(formatter),
                        authorId = author.id,
                        authorName = author.username,
                       // status = userService.checkStatus(model.user.id),
                        action = model.type,
                        avatar = MediaResponseDto.fromModel(author.avatar)
                )
            }
        }
    }
