package com.example.kotlin_amateur.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostResponse(

    val id: String,

    val title: String,
    val content: String,


    val images: List<String>,

    val likes: Int = 0,
    val comments: Int = 0,

    val commentList: List<CommentResponse> = emptyList(),

    val timestamp: String
) :Parcelable