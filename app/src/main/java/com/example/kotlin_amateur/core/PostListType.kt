package com.example.kotlin_amateur.core

import androidx.compose.ui.graphics.Color

/**
 * 🎯 게시글 목록 타입 정의
 * - 홈스크린 재사용으로 메모리 효율성 극대화
 */
enum class PostListType(
    val displayName: String,
    val backgroundColor: Color,
    val endpoint: String? = null // API 엔드포인트 (추후 구현)
) {
    HOME(
        displayName = "동네 이야기",
        backgroundColor = Color(0xFFF8F9FA),
        endpoint = null // 기본 홈 API
    ),
    
    MY_POSTS(
        displayName = "내 게시글",
        backgroundColor = Color(0xFFE3F2FD), // 파란색 계열
        endpoint = "/api/posts/my"
    ),
    
    LIKED_POSTS(
        displayName = "좋아요한 글",
        backgroundColor = Color(0xFFFFEBEE), // 빨간색 계열
        endpoint = "/api/posts/liked"
    ),
    
    RECENT_VIEWED(
        displayName = "최근 본 글",
        backgroundColor = Color(0xFFF3E5F5), // 보라색 계열
        endpoint = "/api/posts/recent"
    );
    
    companion object {
        /**
         * 🎨 배경색을 Brush로 변환
         */
        fun getGradientColors(type: PostListType): List<Color> {
            return when (type) {
                HOME -> listOf(
                    Color(0xFFF8F9FA),
                    Color.White
                )
                MY_POSTS -> listOf(
                    Color(0xFFE3F2FD),
                    Color(0xFFF8F9FA)
                )
                LIKED_POSTS -> listOf(
                    Color(0xFFFFEBEE),
                    Color(0xFFF8F9FA)
                )
                RECENT_VIEWED -> listOf(
                    Color(0xFFF3E5F5),
                    Color(0xFFF8F9FA)
                )
            }
        }
    }
}