package com.twitter.persistence


import com.mongodb.client.model.Filters
import com.twitter.models.User
import org.bson.Document
import org.bson.types.ObjectId

object UserRepository {
    private val usersCollection = Database.getUsersCollection()

    fun saveUser(user: User) {
        val document = Document("id", user.id)
            .append("name", user.name)
            .append("email", user.email)
        usersCollection.insertOne(document)
    }

    fun getUserById(userId: String): User? {
        val query = Filters.eq("id", userId)
        val userDocument = usersCollection.find(query).first()

        return userDocument?.let {
            User(
                it.getString("id"),
                it.getString("name"),
                it.getString("email")
            )
        }
    }
}
