package com.twitter

import com.twitter.folder.CorsFilter
import com.twitter.folder.RouteHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Spark.*

fun main() {
    App().start()
}

class App {
    private val logger: Logger = LoggerFactory.getLogger(App::class.java)
    fun start() {
        port(4567)
        CorsFilter.enableCORS()
        val routeHandler = RouteHandler()
        routeHandler.setupRoutes()
        awaitInitialization()
    }
}
