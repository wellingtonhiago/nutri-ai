package br.com.expert.nutricao.nutricionista

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.config.tryGetString

fun Application.connectToMongoDBConfigTwo(): MongoDatabase {
    val user = environment.config.tryGetString("db.mongo.user") ?: ""
    val password = environment.config.tryGetString("db.mongo.password") ?: ""
    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

    val uri = "mongodb+srv://$user:$password@expert-agent-cluster.uu9o9xd.mongodb.net/?retryWrites=true&w=majority&appName=expert-agent-cluster"

    val mongoClient = MongoClient.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
