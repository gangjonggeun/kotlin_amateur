package com.example.kotlin_amateur.state

sealed class LoginResult {
    data class Success(val accessToken: String) : LoginResult()
    data class NeedNickname(
        val email: String,
        val googleSub: String,
        val name: String,
        val accessToken: String
    ) : LoginResult()
    data class Failure(val exception: Exception) : LoginResult()
}