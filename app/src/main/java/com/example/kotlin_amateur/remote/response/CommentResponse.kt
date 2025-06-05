package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentResponse(
    val id: String,
    val content: String,
    val authorNickname: String,
    val authorUserId: String,
    val authorProfileImage: String?,
    val createdAt: String,
    val replyCount: Int,
    val replies: List<ReplyResponse> = emptyList()
) : Parcelable