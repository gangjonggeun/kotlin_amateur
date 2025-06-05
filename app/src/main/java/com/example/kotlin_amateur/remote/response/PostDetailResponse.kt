package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class PostDetailResponse(
    val id: String,
    val title: String,
    val content: String,
    val images: List<String>,
    val authorNickname: String,
    val authorUserId: String,
    val authorProfileImage: String?,
    val createdAt: String,
    val likeCount: Int,
    val isLiked: Boolean
): Parcelable