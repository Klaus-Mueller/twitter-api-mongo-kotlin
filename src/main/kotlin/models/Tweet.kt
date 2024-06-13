package com.twitter.models

import org.bson.types.ObjectId
import java.util.Date

data class Tweet (
    val id: String,
    val text: String,
    var userId: String,
    val createdDate: Date,
    val likes: MutableList<String>  = mutableListOf()
)