package br.com.expert.exemplo.mongo

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.registerCarRoutes() {
    val carServiceResult = runCatching {
        val mongoDatabase = connectToMongoDB()
        CarService(mongoDatabase)
    }

    carServiceResult.exceptionOrNull()?.let { ex ->
        environment.log.warn("Database not available. Car endpoints will return 503. Reason: ${ex.message}")
    }

    routing {
        val service = carServiceResult.getOrNull()
        route("/cars") {
            createCarRoute(service)
            carByIdRoute(service)
            updateCarRoute(service)
            deleteCarRoute(service)
        }
    }
}

fun Route.createCarRoute(service: CarService?) {
    if (service != null) {
        post {
            val car = call.receive<Car>()
            val id = service.create(car)
            call.respond(HttpStatusCode.Created, id)
        }
    } else {
        post { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.carByIdRoute(service: CarService?) {
    if (service != null) {
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "id is required")
            service.read(id)?.let { car ->
                call.respond(HttpStatusCode.OK, car)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        get("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.updateCarRoute(service: CarService?) {
    if (service != null) {
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "id is required")
            val car = call.receive<Car>()
            service.update(id, car)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        put("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.deleteCarRoute(service: CarService?) {
    if (service != null) {
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "id is required")
            service.delete(id)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        delete("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}
