package com.twitter.persistence

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.twitter.models.User
import com.twitter.util.PasswordUtil
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object UserRepository {
    private val usersCollection = Database.getUsersCollection()
    private val logger: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun saveUser(user: User) {
        try {
            val document = Document("id", user.id).append("name", user.name)
                .append("email", user.email)
                .append("password", PasswordUtil.hash(user.password))
                .append("followers", mutableListOf<String>())
                .append("following", mutableListOf<String>())
                .append("createdDate", Date())
            usersCollection.insertOne(document)
            logger.info("User saved successfully: ${user.id}")
        } catch (e: Exception) {
            logger.error("Error saving user: ${e.message}")
            throw e
        }
    }

    fun getUserById(userId: String): User? {
        try {
            val query = Filters.eq("id", userId)
            val userDocument = usersCollection.find(query).first()
            return userDocument?.let {
                User(
                    it.getString("id"),
                    it.getString("name"),
                    it.getString("email"),
                    it.getString("password"),
                    it.getList("followers", String::class.java) ?: mutableListOf(),
                    it.getList("following", String::class.java) ?: mutableListOf(),
                    it.getDate("createdDate")
                )
            }
        } catch (e: Exception) {
            logger.error("Error retrieving user by ID: $userId, ${e.message}")
            throw e
        }
    }

    fun getUserByUsernameAndPassword(email: String, password: String): User? {
        try {
            val query = Filters.eq("email", email)
            val userDocument = usersCollection.find(query).first()

            return userDocument?.let {
                val storedPassword = it.getString("password")
                if (PasswordUtil.check(password, storedPassword)) {
                    User(
                        it.getString("id"),
                        it.getString("name"),
                        it.getString("email"),
                        it.getString("password"),
                        it.getList("followers", String::class.java) ?: mutableListOf(),
                        it.getList("following", String::class.java) ?: mutableListOf(),
                        it.getDate("createdDate")
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Error retrieving user by username and password: $email, ${e.message}")
            throw e
        }
    }

    fun followUser(userId: String, userToFollow: User): Boolean {
        try {
            // Update current user's following list
            val followingUpdate = Updates.addToSet("following", userToFollow.id)
            usersCollection.updateOne(Filters.eq("id", userId), followingUpdate)

            // Update userToFollow's followers list
            val followersUpdate = Updates.addToSet("followers", userId)
            val updateResult = usersCollection.updateOne(Filters.eq("id", userToFollow.id), followersUpdate)

            if (updateResult.modifiedCount > 0) {
                logger.debug("User {} followed user {}", userId, userToFollow.id)
                return true
            } else {
                logger.debug("Failed to follow user {}", userToFollow.id)
                return false
            }
        } catch (e: Exception) {
            logger.error("An error occurred while following user {}: {}", userToFollow.id, e.message)
            return false
        }
    }


    fun unfollowUser(userId: String, userToUnfollow: User): Boolean {
        try {
            // Update current user's following list
            val followingUpdate = Updates.pull("following", userToUnfollow.id)
            usersCollection.updateOne(Filters.eq("id", userId), followingUpdate)

            // Update userToUnfollow's followers list
            val followersUpdate = Updates.pull("followers", userId)
            val updateResult = usersCollection.updateOne(Filters.eq("id", userToUnfollow.id), followersUpdate)

            if (updateResult.modifiedCount > 0) {
                logger.debug("User {} unfollowed user {}", userId, userToUnfollow.id)
                return true
            } else {
                logger.debug("Failed to unfollow user {}", userToUnfollow.id)
                return false
            }
        } catch (e: Exception) {
            logger.error("An error occurred while unfollowing user {}: {}", userToUnfollow.id, e.message)
            return false
        }
    }

    fun getFollowers(userId: String): List<User> {
        try {
            val query = Filters.eq("id", userId)
            val usersDocument = usersCollection.find(query).firstOrNull()
            val followersIds = usersDocument?.getList("followers", String::class.java) ?: emptyList()
            val followers = usersCollection.find(Filters.`in`("id", followersIds))
            return followers.map { doc ->
                User(
                    id = doc.getString("id").toString(),
                    name = doc.getString("name"),
                    email = doc.getString("email"),
                    password = "",
                    createdDate = doc.getDate("createdDate")
                )
            }.toList()
        } catch (e: Exception) {
            logger.error("Error retrieving followers for user $userId: ${e.message}")
            throw e
        }
    }

    fun getFollowing(userId: String): List<User> {
        try {
            val query = Filters.eq("id", userId)
            val userDocument = usersCollection.find(query).firstOrNull()
            val followingIds = userDocument?.getList("following", String::class.java) ?: emptyList()

            val followingUsers = usersCollection.find(Filters.`in`("id", followingIds))
            return followingUsers.map { doc ->
                User(
                    id = doc.getString("id"),
                    name = doc.getString("name"),
                    email = doc.getString("email"),
                    password = "",
                    createdDate = doc.getDate("createdDate")
                )
            }.toList()
        } catch (e: Exception) {
            logger.error("Error retrieving following users for user $userId: ${e.message}")
            throw e
        }
    }

    fun updateUser(userId: String, updatedUser: User): Boolean {
        try {
            val query = Filters.eq("id", userId)
            val update = Updates.combine(
                Updates.set("name", updatedUser.name),
                Updates.set("email", updatedUser.email),
                Updates.set("password", PasswordUtil.hash(updatedUser.password) )
            )
            val updateResult = usersCollection.updateOne(query, update)

            if (updateResult.modifiedCount > 0) {
                logger.info("User $userId updated successfully")
                return true
            } else {
                logger.warn("Failed to update user $userId")
                return false
            }
        } catch (e: Exception) {
            logger.error("Error updating user $userId: ${e.message}")
            throw e
        }
    }

    fun searchUsers(queryStr: String): List<User> {
        try {
            val query = Filters.or(
                Filters.regex("name", queryStr, "i"), Filters.regex("email", queryStr, "i")
            )
            val usersDocument = usersCollection.find(query)
            return usersDocument.map { doc ->
                User(
                    id = doc.getString("id"),
                    name = doc.getString("name"),
                    email = doc.getString("email"),
                    password = "",
                    createdDate = doc.getDate("createdDate")
                )
            }.toList()
        } catch (e: Exception) {
            logger.error("Error searching users with query: $queryStr, ${e.message}")
            throw e
        }
    }
}
