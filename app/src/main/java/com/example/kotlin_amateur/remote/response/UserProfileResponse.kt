package com.example.kotlin_amateur.remote.response

import com.example.kotlin_amateur.model.UserProfile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 서버 응답용 DTO - 핵심 기능만 포함
 *
 * 🎯 현재 구현 범위:
 * - 사용자 기본 정보
 * - 게시글 수
 * - 가입일
 */
data class UserProfileResponse(
    val userId: String,
    val nickname: String,
    val profileImageUrl: String?,
    val joinedAt: String,           // ISO 8601 형태 서버 응답
    val postCount: Int
) {
    /**
     * ✅ DTO → Domain Model 변환
     * 서버 데이터를 UI에서 사용하기 편한 형태로 변환
     */
    fun toDomainModel(): UserProfile {
        return UserProfile(
            id = userId,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            joinDate = formatJoinDate(joinedAt),
            postCount = postCount
        )
    }

    /**
     * 📅 가입일을 사용자 친화적 형식으로 변환
     * "2024-01-15 10:30" → "2024년 1월"
     */
    private fun formatJoinDate(dateTimeString: String): String {
        return try {
            // 서버 형태: "yyyy-MM-dd HH:mm"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.parse(dateTimeString, formatter)

            "${dateTime.year}년 ${dateTime.monthValue}월"
        } catch (e: Exception) {
            // 파싱 실패 시 기본값
            try {
                // 혹시 날짜만 있는 경우도 처리 ("yyyy-MM-dd")
                val dateOnly = dateTimeString.split(" ")[0]
                val parts = dateOnly.split("-")
                if (parts.size >= 2) {
                    "${parts[0]}년 ${parts[1].toInt()}월"
                } else {
                    "가입일 미상"
                }
            } catch (e2: Exception) {
                "가입일 미상"
            }
        }
    }
}