package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentResponse (
    val commentId: String,  // UUID

    val commentContent: String,
    val commentTimestamp: String,


    val post: PostResponse,

    val replies: List<ReplyResponse>? = emptyList()
) : Parcelable