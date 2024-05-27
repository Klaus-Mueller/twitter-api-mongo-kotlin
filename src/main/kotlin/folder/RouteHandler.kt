package com.twitter.folder

import com.google.gson.Gson
import com.twitter.models.User
import com.twitter.persistence.UserRepository
import org.bson.types.ObjectId
import spark.Spark.*

class RouteHandler {

    fun setupRoutes() {
        val gson = Gson()

        get("/getUser") { req, res ->
            val userId = req.queryParams("id")
            if (userId != null) {
                val user = UserRepository.getUserById(userId)
                if (user != null) {
                    res.status(200)
                    gson.toJson(user)
                } else {
                    // User not found
                    res.status(404)
                    "User not found"
                }
            } else {
                res.status(400)
                "Missing or invalid user ID"
            }
        }

        post("/saveUser") { req, res ->
            val newUser = gson.fromJson(req.body(), User::class.java)
            UserRepository.saveUser(newUser)
            res.status(201)
            "User saved successfully"
        }
    }
}
