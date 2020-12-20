package com.example.route

import com.example.dto.*
import com.example.model.Author
import com.example.model.Media
import com.example.services.FileService
import com.example.services.IdeaService
import com.example.services.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


class RoutingV1(
    private val staticPath: String,
    private val ideaService: IdeaService,
    private val fileService: FileService,
    private val userService: UserService
) {
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }

                route("/") {
                    post("/registration") {
                        val input = call.receive<RegisterRequestDto>()
                        val response = userService.save(input.username, input.password)
                        call.respond(HttpStatusCode.OK, response)
                    }

                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(HttpStatusCode.OK, response)
                        print("recieve auth")
                    }
                }

                authenticate {
                    route("/me") {

                        get {
                            val me = call.authentication.principal<Author>()
                            call.respond(UserResponseDto.fromModel(me!!))
                        }
                        post("/avatar"){
                            val me = call.authentication.principal<Author>()
                            val avatar = call.receive<Media>()
                            userService.addAvatar(me!!.id, avatar)
                            call.respond(HttpStatusCode.OK)
                        }
                    }

                    route("/posts") {

                        get {
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.getAll(myId = me.id)
                            call.respond(response)
                        }
                        get("/my") {
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.getByAuthorId(me.id)
                            call.respond(response)
                        }
                        get("/recent") {
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.getRecent(myId = me.id)
                            call.respond(response)
                        }
                        get("/before/{id}") {
                            val me = call.authentication.principal<Author>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = ideaService.getBefore(id, myId = me.id)
                            call.respond(response)
                        }
                        get("/after/{id}") {
                            val me = call.authentication.principal<Author>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = ideaService.getAfter(id, myId = me.id)
                            call.respond(response)
                        }
                        get("/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.getById(id, me.id)
                            call.respond(response)
                        }
                        get("/{id}/votes"){
                            val postId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val me = call.authentication.principal<Author>()!!
                            val response = userService.getVotesByIdeaId(postId, me.id, ideaService)
                            call.respond(response)

                        }
                        post {

                            val input = call.receive<IdeaRequestDto>()

                            val me = call.authentication.principal<Author>()!!

                            val response = ideaService.save(input, me.id)
                            call.respond(HttpStatusCode.OK, response)
                        }
                        post("/{id}") {
                            val me = call.authentication.principal<Author>()!!
                            val id = call.parameters["id"]?.toLongOrNull()
                                ?: throw ParameterConversionException("id", "Long")
                            val input = call.receive<IdeaRequestDto>()
                            if (ideaService.getById(id, me.id).authorId == me.id) {
                                ideaService.save(input, me.id)
                                call.respond(HttpStatusCode.Accepted)
                            } else {
                                call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                        delete("/{id}") {
                            val me = call.authentication.principal<Author>()!!
                            val id = call.parameters["id"]?.toLongOrNull()
                                ?: throw ParameterConversionException("id", "Long")
                            if (ideaService.getById(id, me.id).id == me.id) {
                                ideaService.delete(id, me.id)
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                        post("/like/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull()
                                ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.like(id, me.id)
                            call.respond(response)
                        }
                        post("/dislike/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull()
                                ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<Author>()!!
                            val response = ideaService.dislike(id, me.id)
                            call.respond(response)
                        }

                    }
                    route("/media") {
                        post {
                            val multipart = call.receiveMultipart()
                            val response = fileService.save(multipart)
                            call.respond(response)
                        }
                    }
                }
            }
        }
    }
}