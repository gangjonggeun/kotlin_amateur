package com.example.kotlin_amateur.remote.response

// API Response Wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)