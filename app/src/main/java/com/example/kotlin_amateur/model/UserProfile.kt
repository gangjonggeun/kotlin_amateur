package com.example.kotlin_amateur.model
/**
 * 사용자 프로필 도메인 모델 (UI에서 사용)
 *
 * 🎯 현재 버전 핵심 기능:
 * - 기본 프로필 정보
 * - 게시글 수
 * - 가입일 정보
 *
 * 🚫 나중에 추가할 기능들:
 * - followerCount, followingCount (팔로우 시스템)
 * - bio (자기소개)
 * - isVerified (인증 마크)
 */
data class UserProfile(
    val id: String,
    val nickname: String,
    val profileImageUrl: String?,
    val joinDate: String,           // "2024년 1월" 형태
    val postCount: Int
)