package com.twitter.folder

import com.google.gson.Gson
import com.mongodb.MongoWriteException
import com.twitter.models.User
import com.twitter.persistence.TweetRepository
import com.twitter.persistence.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark.get
import spark.Spark.post

class UserRouteHandler(private val gson: Gson) : RouteHandler() {
    private val logger: Logger = LoggerFactory.getLogger(UserRouteHandler::class.java)


    override fun setupRoutes() {
        post("/login", this::login)
        get("/logout", this::logout)
        get("/getUser", this::getUser)
        post("/saveUser", this::saveUser)
        post("/followUser", this::followUser)
        post("/unfollowUser", this::unfollowUser)
        get("/getFollowers", this::getFollowers)
        get("/getFollowing", this::getFollowing)
        post("/updateUser", this::updateUser)
        get("/searchUsers", this::searchUsers)
    }

    private fun getUserIdFromSession(req: Request): String {
        return req.session().attribute("user")
    }

    private fun login(req: Request, res: Response): String {
        val userCredentials = gson.fromJson(req.body(), User::class.java)
        val user = UserRepository.getUserByUsernameAndPassword(userCredentials.email, userCredentials.password)
        res.type("application/json")
        return if (user != null) {
            req.session(true).attribute("user", user.id)
            res.status(200)
            logger.info("User session set to user:${req.session().attribute<String>("user")}")
            gson.toJson(user)
        } else {
            res.status(401)
            "Invalid username or password"
        }
    }

    private fun logout(req: Request, res: Response): String {
        req.session().invalidate()
        res.status(200)
        return "Logged out successfully"
    }

    private fun getUser(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "Please log in to your account"
        }

        val userId = req.queryParams("id")
        if (userId != null) {
            val user = UserRepository.getUserById(userId)
            return if (user != null) {
                res.status(200)
                gson.toJson(user)
            } else {
                res.status(404)
                "User not found"
            }
        } else {
            res.status(400)
            return "Missing or invalid user ID"
        }
    }

    private fun saveUser(req: Request, res: Response): String {
        try {
            val newUser = gson.fromJson(req.body(), User::class.java)
            UserRepository.saveUser(newUser)
            res.status(201)
            return "User saved successfully"
        } catch (e: MongoWriteException) {
            res.status(400)
            return "User with the same Email or ID already exists"
        } catch (e: Exception) {
            res.status(500)
            return "Internal Server Error: ${e.message}"
        }
    }

    private fun followUser(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        }

        val userToFollow = gson.fromJson(req.body(), User::class.java)
        val userId = getUserIdFromSession(req)
        return if (userToFollow != null ) {
            if (UserRepository.followUser(userId, userToFollow)) {
                res.status(200)
                "User followed successfully"
            } else {
                res.status(400)
                "Unable to follow user"
            }
        } else {
            res.status(400)
            "Missing user ID to follow or authenticated user ID"
        }
    }

    private fun unfollowUser(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        }

        val userToUnfollow = gson.fromJson(req.body(), User::class.java)
        val userId = getUserIdFromSession(req)
        return if (userToUnfollow != null && userId != null) {
            if (UserRepository.unfollowUser(userId, userToUnfollow)) {
                res.status(200)
                "User unfollowed successfully"
            } else {
                res.status(400)
                "Unable to unfollow user"
            }
        } else {
            res.status(400)
            "Missing user ID to unfollow or authenticated user ID"
        }
    }

    private fun getFollowers(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        }

        val userId = getUserIdFromSession(req)
        val followers = UserRepository.getFollowers(userId)
        res.status(200)
        return gson.toJson(followers)
    }

    private fun getFollowing(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        }

        val userId = getUserIdFromSession(req)
        val following = UserRepository.getFollowing(userId)
        res.status(200)
        return gson.toJson(following)
    }

    private fun updateUser(req: Request, res: Response): String {
        if (!isAuthenticated(req)) {
            res.status(401)
            return "User not authenticated"
        }

        val userId = getUserIdFromSession(req)
        val updatedUser = gson.fromJson(req.body(), User::class.java)
        return if (UserRepository.updateUser(userId, updatedUser)) {
            res.status(200)
            "User profile updated successfully"
        } else {
            res.status(400)
            "Unable to update user profile"
        }
    }

    private fun searchUsers(req: Request, res: Response): String {
        val query = req.queryParams("searchAttr")
        return if (query != null) {
            val users = UserRepository.searchUsers(query)
            res.status(200)
            gson.toJson(users)
        } else {
            res.status(400)
            "Missing search query"
        }
    }
}
