package com.twitter.util

import at.favre.lib.crypto.bcrypt.BCrypt
import org.eclipse.jetty.util.security.Password

object PasswordUtil {
    fun hash(password: String):String {
        return BCrypt.withDefaults().hashToString(12,password.toCharArray())
    }

    fun check(password: String, hashed:String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashed)
        return result.verified
    }
}