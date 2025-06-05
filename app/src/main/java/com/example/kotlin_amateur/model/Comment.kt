package com.example.kotlin_amateur.model

data class Comment(
    val id: String,
    val content: String,
    val authorNickname: String,
    val authorUserId: String, // 🔧 위치 이동 - authorProfileImage 앞으로
    val authorProfileImage: String?,
    val createdAt: String,
    val replyCount: Int,
    val replies: List<Reply> = emptyList(),
    val isReplyInputVisible: Boolean = false,
    val isRepliesVisible: Boolean = false
)