package com.twitter.folder

import com.google.gson.Gson
import com.mongodb.MongoWriteException
import com.twitter.models.Tweet
import com.twitter.models.User
import com.twitter.persistence.TweetRepository
import com.twitter.persistence.UserRepository
import spark.Spark.get
import spark.Spark.post

class RouteHandler {
    private val gson = Gson()
    fun setupRoutes() {
        setupUserRoutes()
        setupTweetRoutes()
    }

    fun isAuthenticated(req: spark.Request): Boolean {
        return req.session().attribute<String>("user") != null
    }

    fun getUserIdFromSession(req: spark.Request): String {
        return req.session().attribute("user")
    }

    fun setupUserRoutes() {
        post("/login") { req, res ->
            val userCredentials = gson.fromJson(req.body(), User::class.java)
            val user = UserRepository.getUserByUsernameAndPassword(userCredentials.email, userCredentials.password)

            if (user != null) {
                req.session(true).attribute("user", user.id)
                res.status(200)
                gson.toJson(user)
            } else {
                res.status(401)
                "Invalid username or password"
            }
        }

        get("/logout") { req, res ->
            req.session().invalidate()
            res.status(200)
            "Logged out successfully"
        }

        get("/getUser") { req, res ->
            if (!isAuthenticated(req)) {
                res.status(401)
                "please log in to your account"
            } else {
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
        }

        post("/saveUser") { req, res ->
            try {
                val newUser = gson.fromJson(req.body(), User::class.java)
                UserRepository.saveUser(newUser)
                res.status(201)
                "User saved successfully"
            } catch (e: MongoWriteException) {
                res.status(400)
                "User with the same Email or ID already exists"
            } catch (e: Exception) {
                res.status(500)
                "Internal Server Error: ${e.message}"
            }
        }

    }

    fun setupTweetRoutes() {
        post("/saveTweet") { req, res ->
            if (!isAuthenticated(req)) {
                res.status(401)
                "please log in to your account"
            } else {
                val newTweet = gson.fromJson(req.body(), Tweet::class.java)
                newTweet.userId = getUserIdFromSession(req)
                TweetRepository.saveTweet(newTweet)
                res.status(201)
                "Tweet saved successfully"
            }
        }

        get("/getUserTweets") { req, res ->
            if (!isAuthenticated(req)) {
                res.status(401)
                "please log in to your account"
            } else {
                val userId = req.queryParams("userId")
                val tweets = TweetRepository.getUserTweets(userId)
                if (tweets.isNotEmpty()) {
                    res.status(200)
                    gson.toJson(tweets)
                } else {
                    res.status(404)
                    "No tweets found for user"
                }
            }
        }

        post("/likeTweet") { req, res ->
            if (!isAuthenticated(req)) {
                res.status(401)
                "please log in to your account"
            } else {
                val tweet = gson.fromJson(req.body(), Tweet::class.java)
                tweet.userId = getUserIdFromSession(req)
                TweetRepository.likeTweet(tweet.id, tweet.userId)
                res.status(201)
                "Tweet liked successfully"
            }
        }

        post("/unlikeTweet") { req, res ->
            if (!isAuthenticated(req)) {
                res.status(401)
                "please log in to your account"
            } else {
                val userId = getUserIdFromSession(req)
                val tweetId = req.queryParams("tweeId")
                if (tweetId != null) {
                    TweetRepository.unlikeTweet(tweetId, userId)
                    res.status(201)
                    "Tweet unliked successfully"
                } else {
                    res.status(400)
                    "Tweet not found"
                }
            }
        }
    }
}
