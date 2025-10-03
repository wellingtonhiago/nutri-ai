package br.com.expert.nutricao.paciente

import java.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Paciente(
    val nome: String,
    val cpf: String,
    val createdAt: String = Instant.now().toString()
) {
    init {
        require(nome.isNotBlank()) { "nome do paciente não pode ser vazio" }
        require(cpf.isNotBlank()) { "CPF do paciente deve ser informado" }
    }
}
