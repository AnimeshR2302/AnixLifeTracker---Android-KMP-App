package com.anix.android.anixlifetracker.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("""{"status":"healthy","service":"AnixLifeTracker Sync Server","version":"1.0.0"}""")
        }

        get("/api/v1/health") {
            val nowMs = System.currentTimeMillis()
            call.respondText("""{"status":"UP","db":"connected","timestamp":$nowMs}""")
        }

        post("/api/v1/auth/register") {
            val nowSec = System.currentTimeMillis() / 1000
            val expSec = nowSec + 86400
            call.respondText(
                """{"status":"success","message":"User registered successfully","session":{"accessToken":"jwt_access_demo","refreshToken":"jwt_refresh_demo","expiresAtEpochSec":$expSec,"user":{"id":"usr_demo","email":"user@anixlifetracker.internal","createdAt":"2026-02-23T00:00:00Z"}}}"""
            )
        }

        post("/api/v1/auth/login") {
            val nowSec = System.currentTimeMillis() / 1000
            val expSec = nowSec + 86400
            call.respondText(
                """{"status":"success","session":{"accessToken":"jwt_access_demo","refreshToken":"jwt_refresh_demo","expiresAtEpochSec":$expSec,"user":{"id":"usr_demo","email":"user@anixlifetracker.internal","createdAt":"2026-02-23T00:00:00Z"}}}"""
            )
        }

        post("/api/v1/sync/push") {
            call.respondText("""{"pushedCount":1,"pulledCount":0,"conflictsCount":0}""")
        }

        post("/api/v1/sync/pull") {
            call.respondText("""{"ownerId":"usr_demo","accounts":[],"transactions":[],"serverCheckpointTimestamp":"2026-02-23T00:00:00Z"}""")
        }
    }
}
