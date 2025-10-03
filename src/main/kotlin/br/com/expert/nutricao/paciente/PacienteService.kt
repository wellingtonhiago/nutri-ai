package br.com.expert.nutricao.paciente

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class PacienteService(
    private val database: MongoDatabase
) {
    private val collection: MongoCollection<Paciente> = database.getCollection("pacientes")

    suspend fun create(paciente: Paciente) =
        collection.insertOne(paciente)

    suspend fun readById(id: String): Paciente? =
        collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()

    suspend fun readByName(nome: String): List<Paciente> =
        collection.find(Filters.eq("nome", nome)).toList()

    suspend fun update(id: String, paciente: Paciente): Paciente? =
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), paciente)

    suspend fun delete(id: String): Paciente? =
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
}
