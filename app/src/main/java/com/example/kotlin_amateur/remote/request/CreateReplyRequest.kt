package com.example.kotlin_amateur.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateReplyRequest(
    val content: String,
    val commentId: String
): Parcelable
