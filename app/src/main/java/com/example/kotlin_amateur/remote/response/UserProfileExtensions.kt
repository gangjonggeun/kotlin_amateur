package com.example.kotlin_amateur.remote.response

import com.example.kotlin_amateur.model.PostSummary
import com.example.kotlin_amateur.model.UserProfile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * ✅ Extension Function을 사용한 매퍼
 * - DTO 클래스를 건드리지 않음
 * - 여러 파일로 분리 가능
 * - 테스트하기 쉬움
 */

/**
 * UserProfileResponse → UserProfile 변환
 */
fun UserProfileResponse.toDomainModel(): UserProfile {
    return UserProfile(
        id = userId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        joinDate = joinedAt.formatJoinDate(),
        postCount = postCount
//        followerCount = followerCount,
//        followingCount = followingCount
//        bio = bio,
//        isVerified = isVerified
    )
}

/**
 * UserPostResponse → PostSummary 변환
 */
fun UserPostResponse.toDomainModel(): PostSummary {
    return PostSummary(
        id = postId,
        content = content,
        firstImage = imageUrls.firstOrNull() ?: thumbnailImageUrl,
        likeCount = likeCount,
//        commentCount = commentCount,
        createdAt = createdAt.formatRelativeTime(),
        // 🆕 작성자 정보 추가
        authorNickname = authorNickname,
        authorUserId = authorUserId
    )
}

/**
 * List<UserPostResponse> → List<PostSummary> 변환
 */
fun List<UserPostResponse>.toDomainModels(): List<PostSummary> {
    return map { it.toDomainModel() }
}

/**
 * 가입일 포맷 유틸리티
 */
private fun String.formatJoinDate(): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(this.replace("Z", ""))
        "${dateTime.year}년 ${dateTime.monthValue}월"
    } catch (e: Exception) {
        "가입일 미상"
    }
}

/**
 * 상대적 시간 포맷 유틸리티
 */
private fun String.formatRelativeTime(): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(this.replace("Z", ""))
        val now = LocalDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours = ChronoUnit.HOURS.between(dateTime, now)
        val days = ChronoUnit.DAYS.between(dateTime, now)

        when {
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days < 30 -> "${days}일 전"
            else -> {
                val months = ChronoUnit.MONTHS.between(dateTime, now)
                "${months}개월 전"
            }
        }
    } catch (e: Exception) {
        "시간 미상"
    }
}