package bot.utils

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun startHealthServer(port: Int = 8080) {
    embeddedServer(Netty, port = port) {
        routing {
            get("/health") {
                call.respondText("OK")
            }
        }
    }.start(wait = false)
}