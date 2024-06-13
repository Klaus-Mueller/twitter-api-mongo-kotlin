package com.twitter.persistence

import com.mongodb.MongoException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.twitter.models.Tweet
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Date

object TweetRepository {
    private val tweetsCollection: MongoCollection<Document> = Database.getTweetCollection()
    private val logger: Logger = LoggerFactory.getLogger(TweetRepository::class.java)


    fun saveTweet(tweet: Tweet) {
        val document = Document("text", tweet.text).append("userId", tweet.userId).append("createdDate", Date())
            .append("likes", mutableListOf<String>())
        tweetsCollection.insertOne(document)
    }

    fun likeTweet(tweetId: String, userId: String) {
        try {
            val tweetObjectId = ObjectId(tweetId)
            val updateQuery = Document("\$addToSet", Document("likes", userId))
            tweetsCollection.updateOne(Filters.eq("_id", tweetObjectId), updateQuery)
            logger.debug("User {} liked tweet {}", userId, tweetId)
        } catch (e: MongoException) {
            logger.error("Error liking tweet: {} Error: {}", e.message, e.stackTrace)
        }
    }

    fun unlikeTweet(tweetId: String, userId: String) {
        try {
            val tweetObjectId = ObjectId(tweetId)
            val updateQuery = Document("\$pull", Document("likes", userId))
            tweetsCollection.updateOne(Filters.eq("_id", tweetObjectId), updateQuery)
            logger.debug("User {} unliked tweet {}", userId, tweetId)
        } catch (e: MongoException) {
            logger.error("Error unliking tweet: {} Error: {}", e.message, e.stackTrace)
        }
    }

    fun getUserTweets(userId: String): List<Tweet> {
        val query = Filters.eq("userId", userId)
        val tweetsDocument = tweetsCollection.find(query)
        try {
            logger.debug("Executing MongoDB find operation with query: {}", query)

            val tweetsList = tweetsDocument.map { doc ->
                try {
                    Tweet(
                        id = doc.getObjectId("_id").toString(),
                        text = doc.getString("text"),
                        userId = doc.getString("userId"),
                        createdDate = doc.getDate("createdDate"),
                        likes = doc.getList("likes", String::class.java) ?: mutableListOf()
                    ).also {
                        logger.debug("Successfully created Tweet object: {}", it)
                    }
                } catch (e: Exception) {
                    logger.error("Error converting document to Tweet: {}. Document: {}", e.message, doc)
                    throw e
                }
            }.toList()

            logger.debug("Successfully fetched {} tweets for userId: {}", tweetsList.size, userId)
            return tweetsList
        } catch (e: MongoException) {
            logger.error("MongoException occurred while fetching tweets for userId {}: {}", userId, e.message)
            return emptyList()
        } catch (e: Exception) {
            logger.error("An error occurred while fetching tweets for userId {}: {}", userId, e.message)
            return emptyList()
        }
    }
}