package com.twitter.persistence


import com.mongodb.client.model.Filters
import com.twitter.models.User
import com.twitter.util.PasswordUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.eclipse.jetty.util.security.Password
import java.util.*

object UserRepository {
    private val usersCollection = Database.getUsersCollection()

    fun saveUser(user: User) {
        val document = Document("id", user.id)
            .append("name", user.name)
            .append("email", user.email)
            .append("password", PasswordUtil.hash(user.password))
            .append("createdDate", Date())
        usersCollection.insertOne(document)
    }

    fun getUserById(userId: String): User? {
        val query = Filters.eq("id", userId)
        val userDocument = usersCollection.find(query).first()

        return userDocument?.let {
            User(
                it.getString("id"),
                it.getString("name"),
                it.getString("email"),
                it.getString("password"),
                it.getDate("createdDate")
            )
        }
    }

    fun getUserByUsernameAndPassword(email: String, password: String): User? {
        val query = Filters.eq("email", email)
        val userDocument = usersCollection.find(query).first()

        return userDocument.let {
            val storedPassword = it.getString("password")
            if(PasswordUtil.check(password, storedPassword)) {
                User(
                    it.getString("id"),
                    it.getString("name"),
                    it.getString("email"),
                    it.getString("password"),
                    it.getDate("createdDate")
                )
            } else {
                null
            }
        }

    }
}
