package com.example.kotlin_amateur.model

import android.os.Parcelable
import com.example.kotlin_amateur.remote.request.PostRequest
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PostModel(
    val id: String,         // 🔥 고유 ID 추가
    val title: String,
    val content: String,
    val images: List<String>,
    val likes: Int = 0,
    val comments: Int = 0,
    val commentList: @RawValue List<CommentModel> = emptyList()
)  : Parcelable

fun PostModel.toRequest(): PostRequest {
    return PostRequest(
        postTitle = this.title,
        postContent = this.content,
        postImageUrls = this.images
    )
}