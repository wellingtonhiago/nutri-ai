package br.com.expert.nutricao.paciente

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document

@Serializable
data class Paciente(
    val nome: String
) {

    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Paciente = json.decodeFromString(document.toJson())

    }
}
