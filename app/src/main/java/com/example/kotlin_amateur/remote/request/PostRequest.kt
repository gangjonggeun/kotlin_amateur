package com.example.kotlin_amateur.remote.request

data class PostRequest(
    val postTitle: String,
    val postContent: String,
    val postImageUrls: List<String>
)