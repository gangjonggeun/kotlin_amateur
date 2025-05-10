package com.example.kotlin_amateur.model

data class LoginRequest(
    val idToken: String
)

data class LoginResponse(
    val accessToken: String,
    val isNewUser: Boolean,
    val email: String?,
    val name: String?,
    val googleSub: String?
)

