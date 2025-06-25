package com.example.kotlin_amateur.state

sealed class StorePromotionResult {
    object Loading : StorePromotionResult()
    data class Success(val message: String) : StorePromotionResult()
    data class Error(val message: String) : StorePromotionResult()
}