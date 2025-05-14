package com.example.kotlin_amateur.remote.request

data class SetupProfileRequest(
    val nickname: String,
    val profileImageUrl: String? = null  // 이미지 업로드 나중에 붙일 경우
)
