package br.com.expert.nutricao.paciente

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId

class PacienteService(
    private val database: MongoDatabase
) {
    var collection: MongoCollection<Document>

    init {
        database.createCollection("pacientes")
        collection = database.getCollection("pacientes")
    }

    suspend fun create(paciente: Paciente): String = withContext(Dispatchers.IO) {
        val doc = paciente.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    suspend fun readById(id: String): Paciente? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let(Paciente.Companion::fromDocument)
    }

    suspend fun readByName(nome: String): List<Paciente> = withContext(Dispatchers.IO) {
        collection
            .find(Filters.eq("nome", nome))
            .map { Paciente.fromDocument(it) }
            .toList()
    }

    suspend fun update(id: String, paciente: Paciente): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), paciente.toDocument())
    }

    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
}