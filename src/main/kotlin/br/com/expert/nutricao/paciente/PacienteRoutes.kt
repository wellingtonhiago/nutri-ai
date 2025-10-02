package br.com.expert.nutricao.paciente

import br.com.expert.nutricao.connectToMongoDBConfigOne
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

fun Application.configurePacienteRoutes() {
    val mongoDataBase = connectToMongoDBConfigOne()
    val pacienteService = PacienteService(mongoDataBase)

    routing {

        route("/pacientes") {

            post {
                val paciente = call.receive<Paciente>()
                val id = pacienteService.create(paciente)
                call.respond(HttpStatusCode.Created, id)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                pacienteService.readById(id)?.let { paciente ->
                    call.respond(paciente)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: throw java.lang.IllegalArgumentException("No Id found")
                pacienteService.update(id, call.receive<Paciente>())?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                pacienteService.delete(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

        }

    }
}

