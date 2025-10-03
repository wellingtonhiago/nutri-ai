package br.com.expert.nutricao.nutricionista

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
import br.com.expert.nutricao.connectToMongoDB

fun Application.registerNutricionistaRoutes() {

    val mongoDataBase = connectToMongoDB()
    val service = NutricionistaService(mongoDataBase)

    routing {
        route("/nutricionistas") {

            // Create
            post {
                val request = call.receive<Nutricionista>()
                val result = service.create(request)
                call.respond(HttpStatusCode.Created, result)
            }

            // Read all
            get {
                val result = service.readAll()
                if (result.isEmpty()) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.OK, result)

            }

            // Read by id
            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                service.readById(id)?.let { n ->
                    call.respond(HttpStatusCode.OK, n)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            // Read by name
            get("/{nome}") {
                val nome =
                    call.parameters["nome"] ?: return@get call.respond(HttpStatusCode.BadRequest, "nome é obrigatório")

                val nutricionistasByName = service.readByName(nome)

                if (nutricionistasByName.isEmpty()) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.OK, nutricionistasByName)
            }

            // Update by id (replace)
            put("/{id}") {
                val id =
                    call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")

                val body = call.receive<Nutricionista>()

                service.updateById(id, body)?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.NotFound)
            }

            // Delete by id
            delete("/{id}") {
                val id =
                    call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id é obrigatório")
                service.deleteById(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
