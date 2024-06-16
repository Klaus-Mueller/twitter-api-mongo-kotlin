package com.twitter.folder

import com.google.gson.Gson

open class RouteHandler {
    private val gson = Gson()
    open fun setupRoutes() {
        val userRoute = UserRouteHandler(gson)
        val tweetRoute = TweetRouteHandler(gson)
        userRoute.setupRoutes()
        tweetRoute.setupRoutes()
    }

    fun isAuthenticated(req: spark.Request): Boolean {
        return req.session().attribute<String>("user") != null
    }
}
