package br.com.expert

import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureFrameworks() {
    val openAIApiKey = System.getenv("OPENAI_API_KEY")
    if (!openAIApiKey.isNullOrBlank()) {
        install(Koog) {
            llm {
                openAI(apiKey = openAIApiKey)
                anthropic(apiKey = "your-anthropic-api-key")
                ollama { baseUrl = "http://localhost:11434" }
                google(apiKey = "your-google-api-key")
                openRouter(apiKey = "your-openrouter-api-key")
                deepSeek(apiKey = "your-deepseek-api-key")
            }
        }
    } else {
        // Skip installing Koog if no API key is provided to keep local runs/tests working without secrets
        environment.log.warn("Koog not installed: OPENAI_API_KEY is not set. /ai endpoints will be unavailable.")
    }

    routing {
        route("/ai") {
            post("/chat") {
                val userInput = call.receive<String>()
                val output = aiAgent(userInput, model = OpenAIModels.Chat.GPT4_1)
                call.respondText(output)
            }
        }
    }
}
