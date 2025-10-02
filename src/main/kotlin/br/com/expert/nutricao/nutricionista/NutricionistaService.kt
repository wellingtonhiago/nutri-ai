package br.com.expert.nutricao.nutricionista

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class NutricionistaService(
    private val database: MongoDatabase
) {

    private val collection: MongoCollection<Nutricionista>

    init {
        collection = database.getCollection("nutricionistas")
    }

    // Cria um novo nutricionista
    suspend fun create(nutricionista: Nutricionista) =
        collection.insertOne(nutricionista)

    // Lista todos
    suspend fun readAll(): List<Nutricionista> =
        collection.find().toList()

    // Busca por id
    suspend fun readById(id: String): Nutricionista? =
        collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()

    // Busca por nome
    suspend fun readByName(nome: String): List<Nutricionista> {
        require(nome.isNotBlank()) { "nome para busca não pode ser vazio" }
        return collection.find(Filters.eq("nome", nome)).toList()
    }

    // Atualiza por id (replace)
    suspend fun updateById(id: String, nutricionista: Nutricionista): Nutricionista? {
        require(id.isNotBlank()) { "id para atualização não pode ser vazio" }
        return collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), nutricionista)
    }

    // Exclui por id
    suspend fun deleteById(id: String): Nutricionista? =
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))

}
