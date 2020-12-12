package arsh.dzdback

import com.example.exception.ConfigurationException
import com.example.model.Idea
import com.example.repository.AuthorsRepository
import com.example.repository.AuthorsRepositoryInMemory
import com.example.repository.IdeasRepository
import com.example.repository.IdeasRepositoryInMemory
import com.example.route.RoutingV1
import com.example.services.FileService
import com.example.services.JWTTokenService
import com.example.services.IdeaService
import com.example.services.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.with
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }

    }
    install(KodeinFeature) {
        constant(tag = "upload-dir") with (environment.config.propertyOrNull("arsh.upload.dir")?.getString()
            ?: throw ConfigurationException("Upload dir is not specified"))
        constant(tag = "result-size") with (environment.config.propertyOrNull("arsh.api.result-size")?.getString()
            ?.toInt()
            ?: throw ConfigurationException("API result size is not specified"))
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService() }
        bind<IdeasRepository>() with eagerSingleton {
            IdeasRepositoryInMemory().apply {
                runBlocking {
                    save(
                        Idea(
                            id = -1,
                            content = "Первый пост!! Привет мир!",
                            authorId = 1
                        )
                    )
                    save(
                        Idea(
                            id = -1,
                            content = "На острове Ольхон, который является сакральным центром силы Байкала, расположен мыс Шаманка, который является обиталещем главного бурхана всей территории",
                            authorId = 1
                        )
                    )
                    save(
                        Idea(
                            id = -1,
                            content = "Make the USA great again!",
                            authorId = 2
                        )
                    )
                    save(
                        Idea(
                            id = -1,
                            content = "I will won the vote anyway",
                            authorId = 2
                        )
                    )
                    save(
                        Idea(
                            id = -1,
                            content = "Кажется я подхватил эту заразу",
                            authorId = 1
                        )
                    )
                }
            }
        }
        bind<IdeaService>() with eagerSingleton { IdeaService(instance(), instance(), instance(tag = "result-size")) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<AuthorsRepository>() with eagerSingleton {
            AuthorsRepositoryInMemory()
        }
        bind<UserService>() with eagerSingleton {
            UserService(instance(), instance(), instance()).apply {
                runBlocking {
                    this@apply.save("Vadim", "qwerty123456")
                    this@apply.save("Donald", "qwerty54321")
                }
            }
        }

        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                instance(tag = "upload-dir"),
                instance(),
                instance(),
                instance()
            )
        }

    }
        install(Authentication) {
            jwt {
                val jwtService by kodein().instance<JWTTokenService>()
                verifier(jwtService.verifier)
                val userService by kodein().instance<UserService>()

                validate {
                    val id = it.payload.getClaim("id").asLong()
                    userService.getModelById(id)
                }
            }
        }

        install(Routing) {
            val routingV1 by kodein().instance<RoutingV1>()
            routingV1.setup(this)
        }
    }

