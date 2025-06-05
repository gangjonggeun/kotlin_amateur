package com.example.kotlin_amateur.model

data class PostDetail(
    val id: String,
    val title: String?,
    val content: String,
    val images: List<String>,
    val authorNickname: String,
    val authorUserId:String,
    val authorProfileImage: String?,
    val createdAt: String,
    val likeCount: Int,
    val isLiked: Boolean
)