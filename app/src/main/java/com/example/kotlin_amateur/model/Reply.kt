package com.example.kotlin_amateur.model

data class Reply(
    val id: String,
    val content: String,
    val authorNickname: String,
    val authorUserId: String,
    val authorProfileImage: String?,
    val createdAt: String,
    val commentId: String
)