# Project Guidelines for nutri-ai

These notes are specific to this Ktor/Kotlin project to help advanced contributors build, test, and extend the codebase efficiently.

Project summary:
- Stack: Kotlin JVM, Ktor 3.3.x (Netty), kotlinx.serialization, MongoDB (sync + coroutine drivers), Logback, Koog Ktor integration for LLM-backed endpoints.
- Entry point: io.ktor.server.netty.EngineMain with Application.module() in br.com.expert.ApplicationKt.
- Key modules: routing in br.com.expert.RoutingKt, frameworks/LLM integration in br.com.expert.FrameworksKt, Mongo config in br.com.expert.nutricao.DatabaseConfigKt.

1) Build and configuration
- JDK: Use Java 17+ (tested with Kotlin 2.2.20, Ktor 3.3.0). If in doubt, set org.gradle.java.home to a JDK 17+ path or install a compatible JDK.
- Build with the provided Gradle wrapper:
  - Windows PowerShell/CMD: .\gradlew.bat build
  - Unix-like shells: ./gradlew build
- Running the server locally:
  - Windows: .\gradlew.bat run
  - The server uses src/main/resources/application.yaml:
    - Ktor module: br.com.expert.ApplicationKt.module
    - HTTP port: 8080
- Configuration via environment variables (read by application.yaml and code):
  - DB_MONGO_USER and DB_MONGO_PASSWORD: Optional. If unset, Mongo-backed endpoints may be disabled/unavailable; logs will indicate unavailability. Regular HTTP routes and tests still run without them.
  - OPENAI_API_KEY: Optional. If unset, Koog/LLM plugin is skipped and the /ai endpoints are not installed. This behavior is intentional to allow tests and local development without secrets.

2) Testing
- Frameworks: kotlin.test + ktor-server-test-host.
- Fastest path in terminal:
  - Windows: .\gradlew.bat test
  - Unix-like: ./gradlew test
- In IDE (IntelliJ IDEA): You can right-click a test class/function and Run. Ktor’s testApplication {} bootstraps Application.module() with in-memory engine.
- Running a single test class from terminal:
  - Windows: .\gradlew.bat test --tests br.com.expert.QuickCheckTest
- Existing tests:
  - src/test/kotlin/ApplicationTest.kt – sanity check for "/" returning 200.
  - src/test/kotlin/QuickCheckTest.kt – same coverage for root.

How to add and execute a new test (verified flow)
- Example minimal test that hits an existing route ("/frutas"):
  - File path: src/test/kotlin/MyFrutasSmokeTest.kt
  - Content:
    
    package br.com.expert
    
    import io.ktor.client.request.get
    import io.ktor.http.HttpStatusCode
    import io.ktor.server.testing.testApplication
    import kotlin.test.Test
    import kotlin.test.assertEquals
    
    class MyFrutasSmokeTest {
        @Test
        fun `frutas endpoint responds OK`() = testApplication {
            application { module() } // boots full app with routes
            val response = client.get("/frutas")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }
    
- Execution:
  - Windows: .\gradlew.bat test --tests br.com.expert.MyFrutasSmokeTest
  - This flow was validated during guideline preparation: a temporary test using the same logic passed when Koog was skipped (no OPENAI_API_KEY) and Mongo creds were absent.
- Notes:
  - Do not rely on /ai endpoints in tests unless you provide OPENAI_API_KEY at runtime; those endpoints are conditionally installed.
  - Mongo-dependent routes may also require DB_MONGO_USER/DB_MONGO_PASSWORD; otherwise they may return 503 by design.

3) Additional development information
- Code style:
  - Kotlin official style; use ktlint/Detekt if you add them. Keep functions small, prefer top-level functions for Ktor feature wiring as the project already does (configureRouting(), configureSerialization(), configureFrameworks()).
  - Prefer data classes with kotlinx.serialization annotations when exposing models via HTTP (see Fruta in Routing.kt).
- Routing:
  - Root: GET "/" → "Hello World!"
  - Sample resource: GET "/frutas" and POST "/frutas" (in-memory list within test run context).
  - Static assets served at "/static" from resources/static.
- LLM/Koog integration:
  - configureFrameworks() conditionally installs Koog. When OPENAI_API_KEY is missing, Koog is skipped and a warning is logged; this keeps local dev and CI tests hermetic. If you need /ai endpoints locally, export OPENAI_API_KEY before starting the app or running tests.
- MongoDB:
  - Database URI is composed in nutricao/DatabaseConfig.kt using Atlas connection with credentials from env. When unset, a graceful warning is logged and Mongo-backed endpoints/services should not be assumed available.
- Logging:
  - Logback is configured via src/main/resources/logback.xml. Use logger names consistent with Ktor modules; prefer warn/info for startup capability messages (e.g., when skipping Koog/Mongo).

4) CI/local reproducibility tips
- Keep tests independent of external services. Favor in-memory routes (e.g., /frutas) for smoke tests.
- Feature toggles via env vars should default to “off” for external dependencies so CI can run without secrets.
- When adding endpoints that require external services, add guards and clear log messages similar to existing patterns.

5) Troubleshooting quick reference
- NPE during tests at Koog installation: Ensure OPENAI_API_KEY is set or rely on the conditional installation (already implemented). If you modify configureFrameworks(), maintain this guard.
- 503 from Mongo-related endpoints: Provide DB_MONGO_USER/DB_MONGO_PASSWORD or mock those services for tests; otherwise ignore those endpoints in the default test suite.
- Port conflicts on 8080: Adjust ktor.deployment.port in application.yaml or set environment variable to override if you add such support.

This document targets advanced contributors; it omits general Kotlin/Gradle basics and focuses on project-specific behaviors and constraints verified as of 2025-10-11.
