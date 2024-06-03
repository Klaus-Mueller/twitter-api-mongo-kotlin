package com.twitter.folder

import com.google.gson.Gson
import com.twitter.models.Tweet
import com.twitter.models.User
import com.twitter.persistence.TweetRepository
import com.twitter.persistence.UserRepository
import org.bson.types.ObjectId
import spark.Spark.*

class RouteHandler {
    val gson = Gson()
    fun setupRoutes() {
        setupUserRoutes()
        setupTweetRoutes()
    }

    fun setupUserRoutes() {
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
    fun setupTweetRoutes() {
        // Tweet routes
        post("/saveTweet") { req, res ->
            val newTweet = gson.fromJson(req.body(), Tweet::class.java)
            TweetRepository.saveTweet(newTweet)
            res.status(201)
            "Tweet saved successfully"
        }

        get("/getUserTweets") { req, res ->
            val userId = req.queryParams("userId")
            if (userId != null) {
                val tweets = TweetRepository.getUserTweets(userId)
                if (tweets.isNotEmpty()) {
                    res.status(200)
                    gson.toJson(tweets)
                } else {
                    res.status(404)
                    "No tweets found for user"
                }
            } else {
                res.status(400)
                "Missing or invalid user ID"
            }
        }
    }
}
