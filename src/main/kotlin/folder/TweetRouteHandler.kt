package com.twitter.folder

import com.google.gson.Gson
import com.twitter.models.Tweet
import com.twitter.persistence.TweetRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark.get
import spark.Spark.post

class TweetRouteHandler(private val gson: Gson) : RouteHandler() {
    private val logger: Logger = LoggerFactory.getLogger(TweetRouteHandler::class.java)


    override fun setupRoutes() {
        post("/saveTweet", this::saveTweet)
        get("/getUserTweets", this::getUserTweets)
        post("/likeTweet", this::likeTweet)
        post("/unlikeTweet", this::unlikeTweet)
        post("/deleteTweet", this::deleteTweet)
        get("/timeline", this::getTimeline)
    }

    private fun getUserIdFromSession(req: Request): String {
        return req.session().attribute("user")
    }

    private fun saveTweet(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "Please log in to your account"
        }

        val newTweet = gson.fromJson(req.body(), Tweet::class.java)
        newTweet.userId = getUserIdFromSession(req)
        TweetRepository.saveTweet(newTweet)
        res.status(201)
        return "Tweet saved successfully"
    }

    private fun getUserTweets(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "Please log in to your account"
        }

        val userId = req.queryParams("userId")
        val tweets = TweetRepository.getUserTweets(userId)
        if (tweets.isNotEmpty()) {
            res.status(200)
            return gson.toJson(tweets)
        } else {
            res.status(404)
            return "No tweets found for user"
        }
    }

    private fun likeTweet(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "Please log in to your account"
        }

        val tweet = gson.fromJson(req.body(), Tweet::class.java)
        tweet.userId = getUserIdFromSession(req)
        TweetRepository.likeTweet(tweet.id, tweet.userId)
        res.status(201)
        return "Tweet liked successfully"
    }

    private fun unlikeTweet(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "Please log in to your account"
        }

        val userId = getUserIdFromSession(req)
        val tweet = gson.fromJson(req.body(), Tweet::class.java)
        if (tweet != null) {
            TweetRepository.unlikeTweet(tweet, userId)
            res.status(201)
            return "Tweet unliked successfully"
        } else {
            res.status(400)
            return "Tweet not found"
        }
    }

    private fun deleteTweet(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        } else {
            val tweet = gson.fromJson(req.body(), Tweet::class.java)
            val userId = getUserIdFromSession(req)
            if (tweet != null && userId != null) {
                if (TweetRepository.deleteTweet(tweet, userId)) {
                    res.status(200)
                    return "Tweet deleted successfully"
                } else {
                    res.status(404)
                    return "Tweet not found or not authorized to delete"
                }
            } else {
                res.status(400)
                return "Missing tweet ID or user ID"
            }
        }
    }

    private fun getTimeline(req: Request, res: Response): String {
        logger.info("User session set to user:${req.session().attribute<String>("user")}")
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        } else {
            val userId = getUserIdFromSession(req)
            val tweets = TweetRepository.getTimeline(userId)
            res.status(200)
            return gson.toJson(tweets)
        }
    }
}
