package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReplyResponse (
    val replyId: String,

    val replyContent: String,
    val replyTimestamp: String,

    val commentID: String = ""
) : Parcelable