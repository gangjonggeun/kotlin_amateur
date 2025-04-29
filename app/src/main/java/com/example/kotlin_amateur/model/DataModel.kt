package com.example.kotlin_amateur.model


data class DataModel(
    val title: String,
    val content: String,
    val images: List<String>,
    val likes: Int = 0,
    val comments: Int = 0
)
