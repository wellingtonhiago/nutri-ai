package br.com.expert.nutricao.nutricionista

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Nutricionista(
    val nome: String,
    val crn: String,
    val especialidade: String,
    val createdAt: String = Instant.now().toString()
) {
    init {
        require(nome.isNotBlank()) { "nome do nutricionista não pode ser vazio" }
        require(crn.isNotBlank()) { "crn do nutricionista não pode ser vazio" }
    }
}
