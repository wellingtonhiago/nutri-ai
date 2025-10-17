package br.com.expert.nutricao.paciente

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
import br.com.expert.nutricao.connectToMongoDB

fun Application.registerPacienteRoutes() {

    val serviceResult = runCatching {
        PacienteService(database = connectToMongoDB())
    }

    serviceResult.exceptionOrNull()?.let { ex ->
        environment.log.warn("Database not available. Paciente endpoints will return 503. Reason: ${ex.message}")
    }

    routing {
        val pacienteService = serviceResult.getOrNull()
        route("/pacientes") {
            createPacienteRoute(pacienteService)
            listPacientesRoute(pacienteService)
            pacienteByIdRoute(pacienteService)
            updatePacienteRoute(pacienteService)
            deletePacienteRoute(pacienteService)
        }
    }
}

fun Route.createPacienteRoute(service: PacienteService?) {
    if (service != null) {
        post {
            val paciente = call.receive<Paciente>()
            val result = service.create(paciente)
            call.respond(HttpStatusCode.Created, result)
        }
    } else {
        post { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.listPacientesRoute(service: PacienteService?) {
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

fun Route.pacienteByIdRoute(service: PacienteService?) {
    if (service != null) {
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            service.readById(id)?.let { paciente ->
                call.respond(HttpStatusCode.OK, paciente)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        get("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.updatePacienteRoute(service: PacienteService?) {
    if (service != null) {
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            service.update(id, call.receive<Paciente>())?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        put("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

fun Route.deletePacienteRoute(service: PacienteService?) {
    if (service != null) {
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
            service.delete(id)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    } else {
        delete("/{id}") { call.respond(HttpStatusCode.ServiceUnavailable, "Database not configured") }
    }
}

