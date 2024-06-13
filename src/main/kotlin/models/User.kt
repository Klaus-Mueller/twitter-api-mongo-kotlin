package com.twitter.models

import java.util.*

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val createdDate: Date
)
