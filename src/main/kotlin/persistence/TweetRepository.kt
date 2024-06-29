package com.twitter.persistence

import com.mongodb.MongoException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.twitter.models.Tweet
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object TweetRepository {
    private val tweetsCollection: MongoCollection<Document> = Database.getTweetCollection()
    private val logger: Logger = LoggerFactory.getLogger(TweetRepository::class.java)

    fun saveTweet(tweet: Tweet) {
        try {
            val document = Document("text", tweet.text)
                .append("userId", tweet.userId)
                .append("createdDate", Date())
                .append("likes", mutableListOf<String>())
            tweetsCollection.insertOne(document)
            logger.info("Tweet saved successfully: ${tweet.id}")
        } catch (e: Exception) {
            logger.error("Error saving tweet: ${e.message}")
            throw e
        }
    }

    fun likeTweet(tweetId: String, userId: String) {
        try {
            val updateQuery = Document("\$addToSet", Document("likes", userId))
            tweetsCollection.updateOne(Filters.eq("_id", ObjectId(tweetId)), updateQuery)
            logger.debug("User {} liked tweet {}", userId, tweetId)
        } catch (e: MongoException) {
            logger.error("Error liking tweet: ${e.message}")
            throw e
        }
    }

    fun unlikeTweet(tweet: Tweet, userId: String) {
        try {
            val updateQuery = Document("\$pull", Document("likes", userId))
            tweetsCollection.updateOne(Filters.eq("_id", ObjectId(tweet.id)), updateQuery)
            logger.debug("User {} unliked tweet {}", userId, tweet.id)
        } catch (e: MongoException) {
            logger.error("Error unliking tweet: ${e.message}")
            throw e
        }
    }

    fun getUserTweets(userId: String): List<Tweet> {
        try {
            val query = Filters.eq("userId", userId)
            val tweetsDocument = tweetsCollection.find(query)
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
            throw e
        } catch (e: Exception) {
            logger.error("An error occurred while fetching tweets for userId {}: {}", userId, e.message)
            throw e
        }
    }

    fun deleteTweet(tweet: Tweet, userId: String): Boolean {
        try {
            val query = Filters.and(
                Filters.eq("_id", ObjectId(tweet.id)),
                Filters.eq("userId", userId)
            )
            val deleteResult = tweetsCollection.deleteOne(query)

            if (deleteResult.deletedCount > 0) {
                logger.info("Tweet ${tweet.id} deleted successfully by user $userId")
                return true
            } else {
                logger.warn("Tweet ${tweet.id} not found or user $userId not authorized to delete")
                return false
            }
        } catch (e: MongoException) {
            logger.error("MongoException occurred while deleting tweet ${tweet.id} by user $userId: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("An error occurred while deleting tweet ${tweet.id} by user $userId: ${e.message}")
            throw e
        }
    }

    fun getTimeline(userId: String): List<Tweet> {
        try {
            val following = UserRepository.getFollowing(userId)
            val followingUserIds = following.map { it.id }.toSet()  // Convert to a Set for quick lookup
            val allTweets = tweetsCollection.find().sort(Sorts.descending("createdDate"))

            return allTweets.map { doc ->
                val tweetUserId = doc.getString("userId")
                TweetWithRelevance(
                    tweet = Tweet(
                        id = doc.getObjectId("_id").toString(),
                        text = doc.getString("text"),
                        userId = tweetUserId,
                        createdDate = doc.getDate("createdDate"),
                        likes = doc.getList("likes", String::class.java) ?: mutableListOf()
                    ),
                    isFollowedUser = followingUserIds.contains(tweetUserId)
                )
            }.sortedWith(compareByDescending<TweetWithRelevance> { it.isFollowedUser }.thenByDescending { it.tweet.createdDate })
                .map { it.tweet }  // Map back to the Tweet objects
                .toList()
        } catch (e: MongoException) {
            logger.error("MongoException occurred while fetching timeline for userId {}: {}", userId, e.message)
            throw e
        } catch (e: Exception) {
            logger.error("An error occurred while fetching timeline for userId {}: {}", userId, e.message)
            throw e
        }
    }
    data class TweetWithRelevance(
        val tweet: Tweet,
        val isFollowedUser: Boolean
    )
}
