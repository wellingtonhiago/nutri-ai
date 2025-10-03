package br.com.expert.nutricao.paciente

import kotlinx.serialization.Serializable

@Serializable
data class Paciente(
    val nome: String
) {
    init {
        require(nome.isNotBlank()) { "nome do paciente não pode ser vazio" }
    }
}
