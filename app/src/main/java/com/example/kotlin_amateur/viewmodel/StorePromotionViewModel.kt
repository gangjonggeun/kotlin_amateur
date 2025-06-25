package com.example.kotlin_amateur.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.remote.request.StorePromotionRequest
import com.example.kotlin_amateur.remote.response.StorePromotionResponse
import com.example.kotlin_amateur.repository.ApiResult
import com.example.kotlin_amateur.repository.StorePromotionRepository
import com.example.kotlin_amateur.state.StorePromotionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

/**
 * 🏪 스토어 프로모션 ViewModel (메모리 최적화)
 *
 * 📌 메모리 안전 원칙:
 * - StateFlow 사용 (생명주기 안전)
 * - ApiResult로 타입 안전 에러 처리
 * - 자동 로딩 상태 관리
 * - Exception 대신 가벼운 에러 처리
 */
@HiltViewModel
class StorePromotionViewModel @Inject constructor(
    private val storePromotionRepository: StorePromotionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 📊 가게 등록 상태 관리
    private val _promotionResult = MutableStateFlow<StorePromotionResult?>(null)
    val promotionResult: StateFlow<StorePromotionResult?> = _promotionResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 📍 근처 가게 목록 상태
    private val _nearbyStores = MutableStateFlow<List<StorePromotionResponse>>(emptyList())
    val nearbyStores: StateFlow<List<StorePromotionResponse>> = _nearbyStores.asStateFlow()

    // 🔍 검색 결과 상태
    private val _searchResults = MutableStateFlow<List<StorePromotionResponse>>(emptyList())
    val searchResults: StateFlow<List<StorePromotionResponse>> = _searchResults.asStateFlow()

    // 👤 내 가게 목록 상태
    private val _myStores = MutableStateFlow<List<StorePromotionResponse>>(emptyList())
    val myStores: StateFlow<List<StorePromotionResponse>> = _myStores.asStateFlow()

    // 🏪 가게 타입 목록 상태
    private val _storeTypes = MutableStateFlow<List<String>>(emptyList())
    val storeTypes: StateFlow<List<String>> = _storeTypes.asStateFlow()

    /**
     * 🏪 가게 홍보 등록 (메모리 안전)
     */
    fun submitStorePromotion(
        storeName: String,
        storeType: String,
        discountInfo: String,
        promotionContent: String,
        latitude: Double,
        longitude: Double
    ) {
        // 🔍 기본 검증
        if (storeName.isBlank()) {
            _promotionResult.value = StorePromotionResult.Error("가게 이름을 입력해주세요")
            return
        }

        if (!isValidLocation(latitude, longitude)) {
            _promotionResult.value = StorePromotionResult.Error("올바른 위치 정보가 필요합니다")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _promotionResult.value = StorePromotionResult.Loading

                val request = StorePromotionRequest(
                    storeName = storeName.trim(),
                    storeType = storeType, // 영어 코드 그대로 사용
                    latitude = latitude,
                    longitude = longitude,
                    discountInfo = discountInfo.trim().takeIf { it.isNotEmpty() },
                    promotionContent = promotionContent.trim().takeIf { it.isNotEmpty() }
                )

                // 🚀 Repository 호출 (ApiResult 처리)
                val result = storePromotionRepository.submitStorePromotion(request)

                when (result) {
                    is ApiResult.Success -> {
                        val successMessage = "🎉 ${result.data.storeName} 가게가 성공적으로 등록되었습니다!"
                        _promotionResult.value = StorePromotionResult.Success(successMessage)
                        
                        // 성공 토스트 표시
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                        println("✅ 가게 홍보 등록 성공: ${result.data.storeName}")
                    }

                    is ApiResult.Error -> {
                        val errorMessage = result.message
                        _promotionResult.value = StorePromotionResult.Error(errorMessage)
                        
                        // 실패 토스트 표시
                        Toast.makeText(context, "❌ $errorMessage", Toast.LENGTH_LONG).show()
                        println("❌ 가게 홍보 등록 실패: $errorMessage")
                    }

                    is ApiResult.Loading -> {
                        // 이미 로딩 상태 설정됨
                    }
                }

            } catch (e: Exception) {
                _promotionResult.value = StorePromotionResult.Error(
                    "예상치 못한 오류가 발생했습니다"
                )
                println("❌ ViewModel 예외: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 📍 근처 가게 검색
     */
    fun getNearbyStores(
        latitude: Double,
        longitude: Double,
        radius: Double = 5.0
    ) {
        if (!isValidLocation(latitude, longitude)) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val result = storePromotionRepository.getNearbyStores(latitude, longitude, radius)

                when (result) {
                    is ApiResult.Success -> {
                        _nearbyStores.value = result.data
                        println("✅ 근처 가게 ${result.data.size}개 조회 성공")
                    }

                    is ApiResult.Error -> {
                        _nearbyStores.value = emptyList()
                        _promotionResult.value = StorePromotionResult.Error(result.message)
                        println("❌ 근처 가게 조회 실패: ${result.message}")
                    }

                    is ApiResult.Loading -> {
                        // 로딩 상태 유지
                    }
                }

            } catch (e: Exception) {
                _nearbyStores.value = emptyList()
                _promotionResult.value = StorePromotionResult.Error("근처 가게 조회 중 오류가 발생했습니다")
                println("❌ 근처 가게 조회 예외: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🔍 가게 검색
     */
    fun searchStores(keyword: String) {
        if (keyword.trim().length < 2) {
            _promotionResult.value = StorePromotionResult.Error("검색어는 2자 이상 입력해주세요")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val result = storePromotionRepository.searchStores(keyword)

                when (result) {
                    is ApiResult.Success -> {
                        _searchResults.value = result.data
                        println("✅ 가게 검색 ${result.data.size}개 결과")
                    }

                    is ApiResult.Error -> {
                        _searchResults.value = emptyList()
                        _promotionResult.value = StorePromotionResult.Error(result.message)
                    }

                    is ApiResult.Loading -> {
                        // 로딩 상태 유지
                    }
                }

            } catch (e: Exception) {
                _searchResults.value = emptyList()
                _promotionResult.value = StorePromotionResult.Error("검색 중 오류가 발생했습니다")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 👤 내 가게 목록 조회
     */
    fun getMyStores() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val result = storePromotionRepository.getMyStores()

                when (result) {
                    is ApiResult.Success -> {
                        _myStores.value = result.data
                        println("✅ 내 가게 ${result.data.size}개 조회 성공")
                    }

                    is ApiResult.Error -> {
                        _myStores.value = emptyList()
                        _promotionResult.value = StorePromotionResult.Error(result.message)
                    }

                    is ApiResult.Loading -> {
                        // 로딩 상태 유지
                    }
                }

            } catch (e: Exception) {
                _myStores.value = emptyList()
                _promotionResult.value = StorePromotionResult.Error("내 가게 목록 조회 중 오류가 발생했습니다")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🗑️ 가게 삭제
     */
    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val result = storePromotionRepository.deleteStorePromotion(storeId)

                when (result) {
                    is ApiResult.Success -> {
                        if (result.data) {
                            _promotionResult.value = StorePromotionResult.Success("가게가 삭제되었습니다")
                            // 내 가게 목록 새로고침
                            getMyStores()
                        } else {
                            _promotionResult.value = StorePromotionResult.Error("가게 삭제에 실패했습니다")
                        }
                    }

                    is ApiResult.Error -> {
                        _promotionResult.value = StorePromotionResult.Error(result.message)
                    }

                    is ApiResult.Loading -> {
                        // 로딩 상태 유지
                    }
                }

            } catch (e: Exception) {
                _promotionResult.value = StorePromotionResult.Error("삭제 중 오류가 발생했습니다")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🏪 가게 타입 목록 조회
     */
    fun getStoreTypes() {
        viewModelScope.launch {
            try {
                val result = storePromotionRepository.getStoreTypes()

                when (result) {
                    is ApiResult.Success -> {
                        _storeTypes.value = result.data
                        println("✅ 가게 타입 ${result.data.size}개 조회 성공")
                    }

                    is ApiResult.Error -> {
                        // 기본 타입 목록 사용 (한글 UI 표시용)
                        _storeTypes.value = StorePromotionRequest.getKoreanTypes()
                        println("⚠️ 가게 타입 조회 실패, 기본 목록 사용: ${result.message}")
                    }

                    is ApiResult.Loading -> {
                        // 로딩 상태 유지
                    }
                }

            } catch (e: Exception) {
                _storeTypes.value = StorePromotionRequest.getKoreanTypes()
                println("❌ 가게 타입 조회 예외, 기본 목록 사용: ${e.message}")
            }
        }
    }

    /**
     * 🔄 상태 초기화 함수들
     */
    fun clearResult() {
        _promotionResult.value = null
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearNearbyStores() {
        _nearbyStores.value = emptyList()
    }

    /**
     * 📍 위치 유효성 검증 (메모리 효율적)
     */
    fun isValidLocation(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * 📝 입력 검증 함수들
     */
    fun isValidStoreName(name: String): Boolean {
        return name.trim().isNotEmpty() && name.length <= 50
    }

    fun isValidPromotionContent(content: String): Boolean {
        return content.length <= 200
    }

    fun isValidDiscountInfo(info: String): Boolean {
        return info.length <= 100
    }

    // 🧠 메모리 안전 - 자동 정리
    override fun onCleared() {
        super.onCleared()
        println("🧠 StorePromotionViewModel 메모리 정리 완료")
    }
}