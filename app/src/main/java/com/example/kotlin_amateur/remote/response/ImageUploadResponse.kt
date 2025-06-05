package com.example.kotlin_amateur.remote.response

data class ImageUploadResponse(
    val success: Boolean,
    val message: String,
    val imageUrls: List<String>,
    val count: Int
)