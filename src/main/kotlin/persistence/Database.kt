package com.twitter.persistence

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

object Database {
    private val client = MongoClients.create("mongodb://localhost:27017")
    private val database = client.getDatabase("twitter-db")

    fun getUsersCollection(): MongoCollection<Document> {
        return database.getCollection("user")
    }

    fun getTweetCollection(): MongoCollection<Document> {
        return database.getCollection("tweet")
    }
}
