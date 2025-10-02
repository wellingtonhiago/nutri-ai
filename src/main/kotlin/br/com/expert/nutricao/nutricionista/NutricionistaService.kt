package br.com.expert.nutricao.nutricionista

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.all
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class NutricionistaService(
    private val database: MongoDatabase
) {

    private val collection: MongoCollection<Nutricionista> = database.getCollection<Nutricionista>("nutricionistas")

    suspend fun create(nutricionista: Nutricionista) =
        collection.insertOne(nutricionista)

    suspend fun readAll(): List<Nutricionista> =
        collection.find().toList()

    suspend fun readById(id: String): Nutricionista? =
        collection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()

    suspend fun readByName(nome: String): List<Nutricionista> {
        require(nome.isNotBlank()) { "nome para busca não pode ser vazio" }
        return collection.find(Filters.eq("nome", nome)).toList()
    }

    suspend fun updateById(id: String, nutricionista: Nutricionista): Nutricionista? {
        require(id.isNotBlank()) { "id para atualização não pode ser vazio" }
        return collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), nutricionista)
    }

    suspend fun deleteById(id: String): Nutricionista? =
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))

}
