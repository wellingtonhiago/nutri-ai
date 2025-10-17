package br.com.expert.nutricao.nutricionista

import br.com.expert.nutricao.connectToMongoDB
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

fun Application.registerNutricionistaRoutes() {
    // Guarded initialization to keep server healthy without DB creds
    val serviceResult = runCatching {
        val mongo = connectToMongoDB()
        NutricionistaService(mongo)
    }

    serviceResult.exceptionOrNull()?.let { ex ->
        environment.log.warn("Database not available. Nutricionista endpoints will return 503. Reason: ${ex.message}")
    }

    routing {
        val service = serviceResult.getOrNull()
        route("/nutricionistas") {
            createNutricionistaRoute(service)
            listNutricionistasRoute(service)
            nutricionistaByIdRoute(service)
            nutricionistasByNameRoute(service)
            updateNutricionistaRoute(service)
            deleteNutricionistaRoute(service)
        }
    }
}

// Extension routes grouped per endpoint, wired by the Application installer below
fun Route.createNutricionistaRoute(service: NutricionistaService?) {
    if (service != null) {
        post {
            val request = call.receive<Nutricionista>()
            val result = service.create(request)
            call.respond(HttpStatusCode.Created, result)
        }
    } else {
        post { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.listNutricionistasRoute(service: NutricionistaService?) {
    if (service != null) {
        get {
            val result = service.readAll()
            if (result.isEmpty()) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.OK, result)
        }
    } else {
        get { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.nutricionistaByIdRoute(service: NutricionistaService?) {
    if (service != null) {
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            service.readById(id)?.let { n ->
                call.respond(HttpStatusCode.OK, n)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        get("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.nutricionistasByNameRoute(service: NutricionistaService?) {
    if (service != null) {
        get("/by-name/{nome}") {
            val nome = call.parameters["nome"] ?: return@get call.respond(HttpStatusCode.BadRequest, "nome é obrigatório")
            val nutricionistasByName = service.readByName(nome)
            if (nutricionistasByName.isEmpty()) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.OK, nutricionistasByName)
        }
    } else {
        get("/by-name/{nome}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.updateNutricionistaRoute(service: NutricionistaService?) {
    if (service != null) {
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            val body = call.receive<Nutricionista>()
            service.updateById(id, body)?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        put("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.deleteNutricionistaRoute(service: NutricionistaService?) {
    if (service != null) {
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            service.deleteById(id)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        delete("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}
