package com.example.kotlin_amateur.remote.response

import org.jetbrains.annotations.Blocking
import java.util.UUID


data class LoginResponse(
    val email: String?,
    val googleSub: String?,
    val name: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
    var message :String?
)

