package com.example.kotlin_amateur.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class CommentModel(
    val commentId: String,
    val commentContent: String,
    val commentTimestamp: String,
    val replies: @RawValue List<ReplyModel> = emptyList()
) : Parcelable
@Parcelize
data class ReplyModel(
    val replyId: String,
    val replyContent: String,
    val replyTimestamp: String
) : Parcelable

