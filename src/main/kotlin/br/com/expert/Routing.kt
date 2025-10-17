package br.com.expert

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.readByte
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

fun Application.configureRouting() {
    routing {
        frutasRoute()

        get("/") {
            call.respondText("Hello World!")
        }


        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}

@Serializable
data class Fruta(val nome: String, val preco: Double)

fun Route.frutasRoute() {
    val listaFrutas = mutableListOf(
        Fruta("Banana", 1.50),
        Fruta("Morango", 3.20),
        Fruta("Uva", 1.20),
        Fruta("Abacaxi", 2.10),
        Fruta("Morango", 2.20)
    )


    get("/frutas") {
        call.respond(
            message = listaFrutas,
            status = HttpStatusCode.OK
        )
    }

    // com parametros
    get("/frutasParametros") {
        val nome = call.queryParameters["nome"] ?: return@get call.respond(HttpStatusCode.OK, listaFrutas)
        val filtroFruta = listaFrutas.filter { it.nome == nome }

        if (filtroFruta.isEmpty()) return@get call.respond(HttpStatusCode.NotFound)

        call.respond(
            message = filtroFruta,
            status = HttpStatusCode.OK
        )
    }

    post("/frutas") {
        try {
            val novaFruta = call.receive<Fruta>()
            listaFrutas.add(novaFruta)
            call.respond(HttpStatusCode.Created, novaFruta)
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, e)
        } catch (e: SerializationException) {
            call.respond(HttpStatusCode.BadRequest, e)
        }
    }
}

//get("/frutasParametros") {
//    // Pega todos os nomes dos parâmetros de consulta enviados (ex: ["nome", "cor"])
//    val queryParams = call.request.queryParameters.names()
//
//    // Filtra para encontrar parâmetros que NÃO são "nome"
//    val invalidParams = queryParams.filterNot { it == "nome" }
//
//    // Se houver qualquer parâmetro inválido, retorna um erro 400 Bad Request
//    if (invalidParams.isNotEmpty()) {
//        return@get call.respond(
//            HttpStatusCode.BadRequest,
//            "Parâmetro(s) de consulta inválido(s): ${invalidParams.joinToString()}. Apenas 'nome' é permitido."
//        )
//    }
//
//    // Pega o valor do parâmetro 'nome'
//    val nome = call.queryParameters["nome"]
//
//    // Se 'nome' não foi fornecido (URL foi /frutasParametros), retorna a lista completa
//    if (nome == null) {
//        return@get call.respond(HttpStatusCode.OK, listaFrutas)
//    }
//
//    // Se 'nome' foi fornecido mas está em branco (ex: /frutasParametros?nome=)
//    if (nome.isBlank()) {
//        return@get call.respond(HttpStatusCode.BadRequest, "O parâmetro 'nome' não pode estar vazio.")
//    }
//
//    // Filtra a lista, ignorando se é maiúscula ou minúscula (melhora a usabilidade)
//    val filtroFruta = listaFrutas.filter { it.nome.equals(nome, ignoreCase = true) }
//
//    if (filtroFruta.isEmpty()) {
//        // Mensagem de erro mais clara
//        call.respond(HttpStatusCode.NotFound, "Nenhuma fruta encontrada com o nome '$nome'.")
//    } else {
//        call.respond(HttpStatusCode.OK, filtroFruta)
//    }
//}
