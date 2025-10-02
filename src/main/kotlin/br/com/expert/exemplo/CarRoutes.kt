package br.com.expert.exemplo

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.configureDatabases() {
    val carServiceResult = runCatching {
        val mongoDatabase = connectToMongoDB()
        CarService(mongoDatabase)
    }

    // Log any initialization problem but keep the server running
    carServiceResult.exceptionOrNull()?.let { ex ->
        environment.log.warn("Database not available. Car endpoints will return 503. Reason: ${ex.message}")
    }

    routing {
        val carService = carServiceResult.getOrNull()
        if (carService != null) {
            // Create car
            post("/cars") {
                val car = call.receive<Car>()
                val id = carService.create(car)
                call.respond(HttpStatusCode.Created, id)
            }
            // Read car
            get("/cars/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                carService.read(id)?.let { car ->
                    call.respond(car)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            // Update car
            put("/cars/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                val car = call.receive<Car>()
                carService.update(id, car)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            // Delete car
            delete("/cars/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                carService.delete(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        } else {
            // Fallback routes when DB is not configured/available
            post("/cars") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
            get("/cars/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
            put("/cars/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
            delete("/cars/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
        }
    }
}
