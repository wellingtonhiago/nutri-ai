package br.com.expert

import br.com.expert.exemplo.mongo.registerCarRoutes
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    registerCarRoutes()
    configureFrameworks()
    configureRouting()
}
