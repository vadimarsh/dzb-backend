import arsh.dzdback.module
import com.jayway.jsonpath.JsonPath
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.nio.file.Files

class ApplicationTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val multipartBoundary = "***blob***"
    private val multipartContentType =
        ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
    private val uploadPath = Files.createTempDirectory("test").toString()
    private val configure: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("arsh.upload.dir", uploadPath)
            put("arsh.api.result-size", "3")
        }
        module()
    }

    /*@org.junit.Test
    fun testUpload() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                setBody(
                        multipartBoundary,
                        listOf(
                                PartData.FileItem({
                                    Files.newInputStream(Paths.get("./testresources/uploads/test.png")).asInput()
                                }, {}, headersOf(
                                        HttpHeaders.ContentDisposition to listOf(
                                                ContentDisposition.File.withParameter(
                                                        ContentDisposition.Parameters.Name,
                                                        "file"
                                                ).toString(),
                                                ContentDisposition.File.withParameter(
                                                        ContentDisposition.Parameters.FileName,
                                                        "photo.png"
                                                ).toString()
                                        ),
                                        HttpHeaders.ContentType to listOf(ContentType.Image.PNG.toString())
                                )
                                )
                        )
                )
            }) {
                response
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("\"id\""))
            }
        }
    }*/

    @org.junit.Test
    fun testUnauthorized() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Get, "/api/v1/posts")) {
                response
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @org.junit.Test
    fun testAuth() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "name": "Vadim",
                            "password": "qwerty123456"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(500)
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    val username = JsonPath.read<String>(response.content!!, "$.username")
                    assertEquals("Vadim", username)
                }
            }
        }
    }

    /*@org.junit.Test
    fun testBadAuth() {
        withTestApplication(configure) {
            runBlocking {

                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "Vadim",
                            "password": "qwerty12345"
                        }
                    """.trimIndent()
                    )
                }) {
                    response
                    println(response.content)
                    assertEquals(HttpStatusCode.BadRequest, response.status())

                }
            }
        }
    }*/

    @org.junit.Test
    fun testRegistration() {
        withTestApplication(configure) {
            runBlocking {

                with(handleRequest(HttpMethod.Post, "/api/v1/registration") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "Peter",
                            "password": "a12345678"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.status())
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }

    /* @org.junit.Test
     fun testExpire() {
         withTestApplication(configure) {
             runBlocking {
                 var token: String? = null
                 with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                     addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                     setBody(
                             """
                         {
                             "username": "vasya",
                             "password": "password"
                         }
                     """.trimIndent()
                     )
                 }) {
                     println(response.content)
                     response
                     assertEquals(HttpStatusCode.OK, response.status())
                     token = JsonPath.read<String>(response.content!!, "$.token")
                 }
                 delay(5000)
                 with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                     addHeader(HttpHeaders.Authorization, "Bearer $token")
                 }) {
                     response
                     assertEquals(HttpStatusCode.Unauthorized, response.status())
                 }
             }
         }
     }*/
    @org.junit.Test
    fun testGetPosts() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                         {
                             "username": "Vadim",
                             "password": "qwerty123456"
                         }
                     """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(5000)
                with(handleRequest(HttpMethod.Get, "/api/v1/posts") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {

                    response
                    print(response.content)
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
    @org.junit.Test
    fun testGetMyPosts() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                            """
                         {
                             "username": "Vadim",
                             "password": "qwerty123456"
                         }
                     """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(5000)
                with(handleRequest(HttpMethod.Get, "/api/v1/posts/my") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {

                    response
                    print(response.content)
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
    @org.junit.Test
    fun testGetPostsByAuthor() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                         {
                             "username": "Vadim",
                             "password": "qwerty123456"
                         }
                     """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(5000)
                with(handleRequest(HttpMethod.Get, "/api/v1/posts/author/2") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {

                    response
                    print(response.content)
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
    @org.junit.Test
    fun testGetVotesForPost() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                            """
                         {
                             "username": "Vadim",
                             "password": "qwerty123456"
                         }
                     """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(5000)
                with(handleRequest(HttpMethod.Get, "/api/v1/posts/1/votes") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {

                    response
                    print(response.content)
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
    @org.junit.Test
    fun testPosting() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "Vadim",
                            "password": "qwerty123456"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    //assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(500)
                println("authorized")
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    addHeader(HttpHeaders.ContentType, "application/json")

                    setBody(
                        """
                        {
                            "id": -1,
                            "sourceId": null,
                            "content": "Test",
                            "link": null,
                            "attachmentId": null
                        }
                        """.trimIndent()
                    )


                }) {

                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    val test = JsonPath.read<String>(response.content!!, "$.content")
                    assertEquals("Test", test)
                    print(response.content)
                }
            }
        }
    }

    @org.junit.Test
    fun testLike() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "Vadim",
                            "password": "qwerty123456"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response

                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                println("authorized")
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/like/2") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")

                }) {
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    val test = JsonPath.read<String>(response.content!!, "$.content")
                    assertEquals("Это простая и незамысловатая идея", test)
                    print(response.content)
                }
            }
        }
    }
    @org.junit.Test
    fun testDisLike() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "Vadim",
                            "password": "qwerty123456"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response

                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                println("authorized")
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/dislike/1") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")

                }) {

                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    val test = JsonPath.read<String>(response.content!!, "$.content")
                    assertEquals("Привет мир!", test)
                    print(response.content)
                }
            }
        }
    }
}
