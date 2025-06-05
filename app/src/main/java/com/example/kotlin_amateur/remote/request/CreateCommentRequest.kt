package com.example.kotlin_amateur.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateCommentRequest(
    val content: String
) : Parcelable
