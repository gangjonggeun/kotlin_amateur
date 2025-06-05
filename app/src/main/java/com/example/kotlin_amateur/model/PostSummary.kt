package com.example.kotlin_amateur.model

data class PostSummary(
    val id: String,
    val content: String,
    val firstImage: String?,
    val likeCount: Int,
    val createdAt: String,
    // 🆕 작성자 정보 추가
    val authorNickname: String,
    val authorUserId: String
)