package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostResponse(

    val postId: String,
    val postTitle: String,
    val postContent: String,
    val nickname: String,
    val imageUrls: List<String>,
    val likes: Int,
    val comments: Int,
    val timeStamp: String
) :Parcelable