package br.com.expert.nutricao.paciente

import br.com.expert.nutricao.connectToMongoDB
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.registerPacienteRoutes() {
    val pacienteService = PacienteService(database = connectToMongoDB())

    routing {

        route("/pacientes") {

            post {
                val paciente = call.receive<Paciente>()
                val result = pacienteService.create(paciente)
                call.respond(HttpStatusCode.Created, result)
            }

            get{
                val result = pacienteService.readAll()
                if (result.isEmpty()) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.OK, result)

            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                pacienteService.readById(id)?.let { paciente ->
                    call.respond(HttpStatusCode.OK, paciente)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                pacienteService.update(id, call.receive<Paciente>())?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                pacienteService.delete(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

        }

    }
}

