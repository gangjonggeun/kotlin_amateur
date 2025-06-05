package com.example.kotlin_amateur.state

import com.example.kotlin_amateur.remote.response.LoginResponse

sealed class LoginResult {
    data class Success(val accessToken: String,  val refreshToken: String) : LoginResult()
    data class NeedNickname(
        val email: String,
        val googleSub: String,
        val name: String,
        val accessToken: String,
        val refreshToken: String
    ) : LoginResult()

    data class Failure(val exception: Exception) : LoginResult()
}