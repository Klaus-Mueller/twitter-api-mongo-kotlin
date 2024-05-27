package com.twitter.models

import com.twitter.persistence.Database

data class User(
    val id: String,
    val name: String,
    val email: String
)
