package com.example.kotlin_amateur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.navi.map.HotPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapRecommendViewModel @Inject constructor(
    // 나중에 Repository 주입
) : ViewModel() {

    // 🗺️ 카카오 지도 상태 관리
    private val _mapState = MutableStateFlow(KakaoMapState())
    val mapState = _mapState.asStateFlow()

    // 🔍 검색 상태
    private val _searchResults = MutableStateFlow<List<Place>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // 🏷️ 카테고리별 장소들
    private val _categoryPlaces = MutableStateFlow<Map<String, List<Place>>>(emptyMap())
    val categoryPlaces = _categoryPlaces.asStateFlow()

    // 🔥 핫플레이스 목록
    private val _hotPlaces = MutableStateFlow<List<HotPlace>>(emptyList())
    val hotPlaces = _hotPlaces.asStateFlow()

    // 📍 현재 위치
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    // ⚡ 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ❌ 에러 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // 🏷️ 선택된 카테고리
    private val _selectedCategory = MutableStateFlow("전체")
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * 🎯 초기 데이터 로드
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: 실제 API 호출로 교체
                loadSampleData()
            } catch (e: Exception) {
                _errorMessage.value = "데이터 로드 중 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🔍 장소 검색 (카카오 로컬 API 활용 예정)
     */
    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: 카카오 로컬 API 검색 구현
                val results = performKakaoSearch(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _errorMessage.value = "검색 중 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🏷️ 카테고리별 장소 필터링
     */
    fun filterByCategory(category: String) {
        viewModelScope.launch {
            try {
                _selectedCategory.value = category

                val places = if (category == "전체") {
                    _categoryPlaces.value.values.flatten()
                } else {
                    _categoryPlaces.value[category] ?: emptyList()
                }

                // 지도에 마커 업데이트
                updateMapMarkers(places)

            } catch (e: Exception) {
                _errorMessage.value = "카테고리 필터링 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 📍 현재 위치 업데이트 (GPS 또는 사용자 선택)
     */
    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = Location(latitude, longitude)

        // 현재 위치 기반으로 주변 장소 재검색
        loadNearbyPlaces(latitude, longitude)

        // 지도 중심 이동
        updateMapCenter(latitude, longitude)
    }

    /**
     * 🔥 핫플레이스 새로고침
     */
    fun refreshHotPlaces() {
        viewModelScope.launch {
            try {
                // TODO: 실제 핫플레이스 API 호출
                val hotPlaces = loadHotPlaces()
                _hotPlaces.value = hotPlaces
            } catch (e: Exception) {
                _errorMessage.value = "핫플레이스 로드 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 🗺️ 지도 중심 위치 변경
     */
    fun updateMapCenter(latitude: Double, longitude: Double) {
        _mapState.value = _mapState.value.copy(
            centerLatitude = latitude,
            centerLongitude = longitude
        )
    }

    /**
     * 🔍 줌 레벨 변경
     */
    fun updateZoomLevel(zoomLevel: Int) {
        _mapState.value = _mapState.value.copy(
            zoomLevel = zoomLevel
        )
    }

    /**
     * ❌ 에러 메시지 클리어
     */
    fun clearError() {
        _errorMessage.value = null
    }

    // =========================
    // 🛠️ Private Helper 메서드들
    // =========================

    private suspend fun loadSampleData() {
        // 🏠 서울 중심 샘플 데이터
        val samplePlaces = mapOf(
            "카페" to listOf(
                Place("1", "브런치 카페 모모", "카페", 37.5666805, 126.9784147, "⭐ 4.8", "🔥 실시간 인기"),
                Place("2", "스타벅스 강남점", "카페", 37.5656805, 126.9794147, "⭐ 4.5", "🆕 신규 오픈"),
                Place("3", "이디야커피 홍대점", "카페", 37.5546805, 126.9224147, "⭐ 4.3", "☕ 아메리카노 할인")
            ),
            "맛집" to listOf(
                Place("4", "돈까스 맛집 우동이", "맛집", 37.5676805, 126.9774147, "⭐ 4.9", "🎉 30% 할인"),
                Place("5", "김치찌개 전문점", "맛집", 37.5686805, 126.9764147, "⭐ 4.6", "👨‍🍳 맛집 인증"),
                Place("6", "24시 칼국수", "맛집", 37.5696805, 126.9754147, "⭐ 4.7", "🌙 새벽까지 운영")
            ),
            "스터디" to listOf(
                Place("7", "24시 스터디카페", "스터디", 37.5706805, 126.9744147, "⭐ 4.7", "💺 좌석 여유"),
                Place("8", "조용한 도서관", "스터디", 37.5716805, 126.9734147, "⭐ 4.8", "📚 집중 최적화")
            ),
            "편의점" to listOf(
                Place("9", "CU 편의점", "편의점", 37.5646805, 126.9804147, "⭐ 4.2", "🏪 24시간 운영"),
                Place("10", "세븐일레븐", "편의점", 37.5636805, 126.9814147, "⭐ 4.1", "🍱 도시락 할인")
            )
        )

        _categoryPlaces.value = samplePlaces

        // 🔥 핫플레이스 샘플 데이터
        val sampleHotPlaces = listOf(
            HotPlace("1", "브런치 카페 모모", "카페", "⭐ 4.8", "🔥 실시간 인기", "🚶‍♂️ 3분"),
            HotPlace("4", "돈까스 맛집 우동이", "맛집", "⭐ 4.9", "🎉 30% 할인", "🚶‍♂️ 5분"),
            HotPlace("7", "24시 스터디카페", "스터디", "⭐ 4.7", "💺 좌석 여유", "🚶‍♂️ 2분")
        )

        _hotPlaces.value = sampleHotPlaces
    }

    private suspend fun performKakaoSearch(query: String): List<Place> {
        // TODO: 카카오 로컬 API 키워드 검색 구현
        // REST API 키 사용: b04809db6fe9cbfa8a34aa98e26f04c4

        return _categoryPlaces.value.values.flatten()
            .filter { it.name.contains(query, ignoreCase = true) }
    }

    private fun updateMapMarkers(places: List<Place>) {
        // TODO: 카카오 지도에 마커 업데이트 로직
        val markers = places.map { place ->
            PlaceMarker(
                id = place.id,
                latitude = place.latitude,
                longitude = place.longitude,
                title = place.name,
                snippet = place.promotion,
                category = place.category,
                isHotPlace = _hotPlaces.value.any { it.id == place.id }
            )
        }

        _mapState.value = _mapState.value.copy(markers = markers)
    }

    private fun loadNearbyPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                // TODO: 카카오 로컬 API 반경 검색 구현
                // 현재 위치 기준 반경 1km 내 장소 검색
            } catch (e: Exception) {
                _errorMessage.value = "주변 장소 검색 중 오류가 발생했습니다"
            }
        }
    }

    private suspend fun loadHotPlaces(): List<HotPlace> {
        // TODO: 실제 핫플레이스 API 호출 (실시간 인기도 기반)
        return _hotPlaces.value
    }
}

// =========================
// 📋 카카오 지도용 데이터 클래스들
// =========================

data class KakaoMapState(
    val centerLatitude: Double = 37.5666805, // 서울 중심 (광화문)
    val centerLongitude: Double = 126.9784147,
    val zoomLevel: Int = 15, // 카카오 지도 줌 레벨 (1~21)
    val markers: List<PlaceMarker> = emptyList()
)

data class Place(
    val id: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val rating: String,
    val promotion: String,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val address: String = "",
    val phoneNumber: String = "",
    val openingHours: String = "",
    val distance: String = "",
    val kakaoPlaceId: String = "" // 카카오 로컬 API의 place_id
)

data class PlaceMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String,
    val category: String,
    val isHotPlace: Boolean = false
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

// HotPlace는 동일하게 사용