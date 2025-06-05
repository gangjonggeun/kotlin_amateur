package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter

@Parcelize
data class PostListResponse(
    val postId: String,
    val postTitle: String,
    val postContent: String,
    val authorNickname: String,
    val authorUserId: String,
    val authorProfileImageUrl: String?,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val timeStamp: String,

) : Parcelable {

    // 계산된 프로퍼티들을 함수로 변경
    val hasImage: Boolean
        get() = imageUrls.isNotEmpty()

    val representativeImageUrl: String?
        get() = imageUrls.firstOrNull()

    val displayContent: String
        get() = postContent

    val formattedTime: String
        get() = formatTimeString(timeStamp)

    // 좋아요 토글 함수
    fun toggleLike(): PostListResponse {
        return this.copy(
            isLikedByCurrentUser = !isLikedByCurrentUser,
            likeCount = if (isLikedByCurrentUser) likeCount - 1 else likeCount + 1
        )
    }

    // 시간 포맷팅 함수
    private fun formatTimeString(timeString: String): String {
        return try {
            // ISO 8601 형식을 파싱해서 원하는 형태로 변환
            val dateTime = LocalDateTime.parse(timeString)
            // 원하는 형식으로 포맷팅 (예: "2시간 전", "5월 30일" 등)
            formatRelativeTime(dateTime)
        } catch (e: Exception) {
            timeString // 파싱 실패시 원본 반환
        }
    }

    private fun formatRelativeTime(dateTime: LocalDateTime): String {
        // 상대 시간 포맷팅 로직
        val now = LocalDateTime.now()
        val duration = Duration.between(dateTime, now)

        return when {
            duration.toMinutes() < 1 -> "방금 전"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
            duration.toHours() < 24 -> "${duration.toHours()}시간 전"
            duration.toDays() < 7 -> "${duration.toDays()}일 전"
            else -> dateTime.format(DateTimeFormatter.ofPattern("M월 d일"))
        }
    }
}

// 상세페이지용 별도 모델 (필요시)
//@Parcelize
//data class PostDetailResponse(
//    val postId: String,
//    val content: String,
//    val authorNickname: String,
//    val images: List<String>, // 전체 이미지 리스트
//    val likeCount: Int,
//    val commentCount: Int,
//    val createdAt: LocalDateTime,
//    val isLikedByCurrentUser: Boolean = false,
//    val comments: List<CommentModel> = emptyList() // 댓글 리스트
//) : Parcelable