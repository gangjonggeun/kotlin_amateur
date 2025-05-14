package com.example.kotlin_amateur.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



data class UserModel(
    val accessToken: String,
    val email: String,
    val name: String,
    val googleSub: String,
    val nickname: String = "",
    val tag: String = "",
    val profileImageUrl: String? = null
)

@Parcelize
data class PostModel(
    val id: String,         // 🔥 고유 ID 추가
    val title: String,
    val content: String,
    val images: List<String>,
    val likes: Int = 0,
    val comments: Int = 0,
    val commentList: @RawValue List<CommentModel> = emptyList(),
    val timestamp: String = getCurrentTime()
)  : Parcelable
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}