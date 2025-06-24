package com.example.kotlin_amateur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.repository.StorePromotionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 🏷️ 데이터 클래스
data class StorePromotionRequest(
    val storeName: String,
    val storeType: String,
    val discountInfo: String,
    val promotionContent: String,
    val latitude: Double,
    val longitude: Double
)

sealed class StorePromotionResult {
    object Loading : StorePromotionResult()
    data class Success(val message: String) : StorePromotionResult()
    data class Error(val message: String) : StorePromotionResult()
}

@HiltViewModel
class StorePromotionViewModel @Inject constructor(
    private val storePromotionRepository: StorePromotionRepository
) : ViewModel() {

    // 📊 상태 관리 (메모리 효율적)
    private val _promotionResult = MutableStateFlow<StorePromotionResult?>(null)
    val promotionResult: StateFlow<StorePromotionResult?> = _promotionResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 🏪 가게 홍보 등록 함수
     * 
     * @param storeName 가게 이름 (필수)
     * @param storeType 가게 타입 (맛집, 카페, 편의점, 미용, 헬스, 스터디)
     * @param discountInfo 할인 정보 (선택)
     * @param promotionContent 홍보 내용 (선택)
     * @param latitude 위도
     * @param longitude 경도
     */
    fun submitStorePromotion(
        storeName: String,
        storeType: String,
        discountInfo: String,
        promotionContent: String,
        latitude: Double,
        longitude: Double
    ) {
        // 🔍 입력 검증
        if (storeName.isBlank()) {
            _promotionResult.value = StorePromotionResult.Error("가게 이름을 입력해주세요")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _promotionResult.value = StorePromotionResult.Loading

                val request = StorePromotionRequest(
                    storeName = storeName.trim(),
                    storeType = storeType,
                    discountInfo = discountInfo.trim(),
                    promotionContent = promotionContent.trim(),
                    latitude = latitude,
                    longitude = longitude
                )

                // 🚀 API 호출
                val result = storePromotionRepository.submitStorePromotion(request)
                
                result.fold(
                    onSuccess = { response ->
                        _promotionResult.value = StorePromotionResult.Success(
                            response.message ?: "가게 홍보가 성공적으로 등록되었습니다!"
                        )
                        println("✅ 가게 홍보 등록 성공: ${response.message}")
                    },
                    onFailure = { error ->
                        val errorMessage = when {
                            error.message?.contains("network", true) == true -> 
                                "네트워크 연결을 확인해주세요"
                            error.message?.contains("timeout", true) == true -> 
                                "요청 시간이 초과되었습니다. 다시 시도해주세요"
                            else -> 
                                error.message ?: "가게 홍보 등록에 실패했습니다. 다시 시도해주세요"
                        }
                        
                        _promotionResult.value = StorePromotionResult.Error(errorMessage)
                        println("❌ 가게 홍보 등록 실패: ${error.message}")
                    }
                )

            } catch (e: Exception) {
                _promotionResult.value = StorePromotionResult.Error(
                    "예상치 못한 오류가 발생했습니다: ${e.localizedMessage}"
                )
                println("❌ 예상치 못한 오류: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🔄 결과 상태 초기화
     */
    fun clearResult() {
        _promotionResult.value = null
    }

    /**
     * 📍 위치 유효성 검증
     */
    fun isValidLocation(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * 📝 홍보 내용 길이 검증
     */
    fun isValidPromotionContent(content: String): Boolean {
        return content.length <= 200
    }

    // 🧠 메모리 안전 - 자동 정리
    override fun onCleared() {
        super.onCleared()
        // StateFlow는 자동으로 정리되므로 별도 작업 불필요
        println("🧠 StorePromotionViewModel 메모리 정리 완료")
    }
}

/**
 * 📊 API 응답 데이터 클래스
 */
data class StorePromotionResponse(
    val success: Boolean,
    val message: String?,
    val storeId: String? = null,
    val createdAt: String? = null
)
