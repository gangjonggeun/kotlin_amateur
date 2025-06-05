package com.example.kotlin_amateur.remote.response
/**
 * 사용자 게시글 요약 응답 DTO - API 전용
 */
data class UserPostResponse(
    val postId: String,
    val title: String?,
    val content: String,
    val thumbnailImageUrl: String?,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String?,
    // 🆕 작성자 정보 추가
    val authorNickname: String,
    val authorUserId: String
)