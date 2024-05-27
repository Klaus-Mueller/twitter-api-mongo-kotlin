package com.twitter

import com.twitter.folder.RouteHandler
import spark.Spark.*

fun main() {
    App().start()
}

class App {
    fun start() {
        port(8080)
        val routeHandler = RouteHandler()
        routeHandler.setupRoutes()
        println("Spark server started on port 8080")
    }
}
