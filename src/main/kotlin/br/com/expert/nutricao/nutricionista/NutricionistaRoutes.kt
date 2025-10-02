package br.com.expert.nutricao.nutricionista

import io.ktor.http.HttpHeaders
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
import kotlinx.serialization.Serializable

fun Application.registerNutricionistaRoutes() {

    val mongoDataBase = connectToMongoDBConfigTwo()
    val service = NutricionistaService(mongoDataBase)

    @Serializable
    data class NutricionistaCreateRequest(
        val nome: String,
        val crn: String,
        val especialidade: String
    )

    @Serializable
    data class NutricionistaResponse(
        val _id: String,
        val nome: String,
        val crn: String,
        val especialidade: String,
        val createdAt: String
    )

    routing {
        route("/nutricionistas") {

            // Create
            post {
                val request = call.receive<NutricionistaCreateRequest>()
                val novo = Nutricionista(
                    nome = request.nome,
                    crn = request.crn,
                    especialidade = request.especialidade
                )
                val result = service.create(novo)
                val id = result.insertedId?.asObjectId()?.value?.toHexString()
                    ?: return@post call.respond(HttpStatusCode.InternalServerError, "Falha ao obter ID gerado")

                // Build Location header with example base path
                val location = "/api/v1/nutricionistas/$id"
                call.response.headers.append(HttpHeaders.Location, location)

                val response = NutricionistaResponse(
                    _id = id,
                    nome = novo.nome,
                    crn = novo.crn,
                    especialidade = novo.especialidade,
                    createdAt = novo.createdAt
                )
                call.respond(HttpStatusCode.Created, response)
            }

            // Read all
            get {
                val all = service.readAll()
                if (all.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.OK, all)
                }
            }

            // Read by id
            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                service.readById(id)?.let { n ->
                    call.respond(n)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            // Update by id (replace)
            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                val body = call.receive<Nutricionista>()
                service.updateById(id, body)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            // Delete by id
            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                service.deleteById(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}