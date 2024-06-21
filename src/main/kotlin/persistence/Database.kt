package com.twitter.persistence

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

object Database {
    private val mongoHost = System.getenv("MONGO_HOST") ?: "localhost"
    private val mongoPort = System.getenv("MONGO_PORT") ?: "27017"
    private val client = MongoClients.create("mongodb://$mongoHost:$mongoPort")
    private val database: MongoDatabase = client.getDatabase("twitter-db")

    fun getUsersCollection(): MongoCollection<Document> {
        return database.getCollection("user")
    }

    fun getTweetCollection(): MongoCollection<Document> {
        return database.getCollection("tweet")
    }
}
