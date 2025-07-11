package org.example.bot.utils

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun startHealthServer() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port) {
        routing {
            get("/health") {
                call.respondText("OK")
            }
        }
    }.start(wait = false) // НЕ блокирует основной поток (бот продолжает работать)
}