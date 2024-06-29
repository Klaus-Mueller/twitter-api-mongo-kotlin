package com.twitter.folder

import spark.Filter
import spark.Request
import spark.Response
import spark.Spark

object CorsFilter {
    fun enableCORS() {
        val allowedOrigin = System.getenv("ALLOWED_ORIGIN") ?: "http://localhost:8080"

        // Handle preflight requests
        Spark.options("/*") { request, response ->
            val accessControlRequestHeaders = request.headers("Access-Control-Request-Headers")
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders)
            }

            val accessControlRequestMethod = request.headers("Access-Control-Request-Method")
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod)
            }
            response.header("Access-Control-Allow-Origin", allowedOrigin)
            response.header("Access-Control-Allow-Credentials", "true")
            "OK"
        }

        // Handle actual requests
        val filter: Filter = Filter { request, response ->
            if (!response.raw().containsHeader("Access-Control-Allow-Origin")) {
                response.header("Access-Control-Allow-Origin", allowedOrigin)
            }
            if (!response.raw().containsHeader("Access-Control-Allow-Credentials")) {
                response.header("Access-Control-Allow-Credentials", "true")
            }
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With")
        }
        Spark.after(filter)
    }
}