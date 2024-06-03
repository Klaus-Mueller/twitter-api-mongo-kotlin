package com.twitter.persistence

import com.mongodb.MongoException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.twitter.models.Tweet
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Date

object TweetRepository {
    private val tweetsCollection: MongoCollection<Document> = Database.getTweetCollection()
    private val logger:Logger = LoggerFactory.getLogger(TweetRepository::class.java)


    fun saveTweet(tweet: Tweet) {
        val document = Document("text", tweet.text)
            .append("userId", tweet.userId)
            .append("timestamp", Date())
        tweetsCollection.insertOne(document)
    }

    fun getUserTweets(userId: String): List<Tweet> {
        val query = Filters.eq("userId", userId)
        val tweetsDocument = tweetsCollection.find(query)
        try{
            logger.debug("Executing MongoDB find operation with query: {}", query)

            tweetsDocument.forEach { document ->
                logger.debug("Fetched document: {}", document)
            }
            return tweetsDocument.map { doc ->
                Tweet(
                    text = doc.getString("text"),
                    userId = doc.getString("userId")
                )
            }.toList()
        } catch (e:MongoException) {
            logger.error("Message {}  Error: {}",e.message, e.stackTrace)
            return emptyList()
        }
    }
}