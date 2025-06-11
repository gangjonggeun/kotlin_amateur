package com.example.kotlin_amateur.navi.map

// 🖼️ Compose Foundation

// 🎨 Material Icons

// 🎨 Material3

// 🔧 Compose Runtime

// 🖥️ Compose UI

// 📍 위치 관련

// 🗺️ 카카오 지도 SDK

// ⏰ Coroutines
import com.example.kotlin_amateur.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles


// 🗺️ 라벨 관리 유틸리티 함수들
object KakaoMapUtils {

    // 🗑️ 라벨 제거 함수 (개선된 버전)
    fun removeLabel(kakaoMap: KakaoMap, labelTag: String) {
        try {
            val labelLayer = kakaoMap.labelManager?.layer
            val labelToRemove = labelLayer?.getLabel(labelTag)

            labelToRemove?.let { label ->
                labelLayer.remove(label)
                println("✅ 라벨 제거 성공: $labelTag")
            } ?: run {
                println("⚠️ 제거할 라벨을 찾지 못함: $labelTag")
            }
        } catch (e: Exception) {
            println("❌ 라벨 제거 실패: ${e.message}")
        }
    }

    // 🗺️ 전체 라벨 제거 (현재 위치 마커는 제외하고 싶다면 로직 추가 필요)
    fun removeAllLabels(kakaoMap: KakaoMap) {
        try {
            val labelLayer = kakaoMap.labelManager?.layer
            labelLayer?.removeAll()
            println("모든 라벨 제거 성공.")
        } catch (e: Exception) {
            println("전체 라벨 제거 실패: ${e.message}")
        }
    }

    // 📍 현재 위치 마커 업데이트 (개선된 버전)
    fun updateCurrentLocationMarker(kakaoMap: KakaoMap, currentLocation: LatLng) {
        // 기존 현재 위치 마커 제거
        removeLabel(kakaoMap, "current_location_marker")

        // 새로운 현재 위치 마커 추가
        addCurrentLocationMarker(kakaoMap, currentLocation)
    }

    // 📍 카테고리별 마커 필터링 (현재 위치 보존)
    fun updateMarkersByCategory(kakaoMap: KakaoMap, category: String, currentLocation: LatLng?) {
        try {
            // 🔍 현재 위치 마커 상태 체크
//            val hasCurrentLocationMarker = kakaoMap.labelManager?.layer?.getLabel("current_location_marker") != null

            // 🗑️ 카테고리 마커들만 제거 (현재 위치 마커는 보존)
            removeCategoryMarkers(kakaoMap)

            // 🎨 카테고리에 따른 새 마커 추가
            when (category) {
                "전체" -> addAllCategoryMarkers(kakaoMap)
                "맛집" -> addRestaurantMarkers(kakaoMap)
                "카페" -> addCafeMarkers(kakaoMap)
                "편의점" -> addConvenienceStoreMarkers(kakaoMap)
                else -> addAllCategoryMarkers(kakaoMap)
            }



        } catch (e: Exception) {
            println("❌ 카테고리별 마커 업데이트 실패: ${e.message}")
        }
    }

    // 🗑️ 카테고리 마커들만 제거하는 함수 (새로 추가)
    private fun removeCategoryMarkers(kakaoMap: KakaoMap) {
        val categoriesToRemove = listOf("restaurant", "cafe", "convenience")

        categoriesToRemove.forEach { category ->
            // 각 카테고리별로 인덱스 기반 라벨들 제거
            for (i in 0..10) { // 최대 10개까지 체크
                removeLabel(kakaoMap, "${category}_$i")
            }
        }
    }

    // 🍽️ 맛집 마커들
    private fun addRestaurantMarkers(kakaoMap: KakaoMap) {
        val restaurants = listOf(
            LatLng.from(37.5656805, 126.9794147) to "돈까스 맛집 우동이",
            LatLng.from(37.5646805, 126.9804147) to "한식 집 백주마당",
            LatLng.from(37.5636805, 126.9814147) to "중국집 새별루"
        )
        addMarkersToMap(kakaoMap, restaurants, "restaurant")
    }

    // ☕ 카페 마커들
    private fun addCafeMarkers(kakaoMap: KakaoMap) {
        val cafes = listOf(
            LatLng.from(37.5666805, 126.9784147) to "브런치 카페 모모",
            LatLng.from(37.5676805, 126.9774147) to "디저트 카페 스윗",
            LatLng.from(37.5686805, 126.9764147) to "아메리카노 카페"
        )
        addMarkersToMap(kakaoMap, cafes, "cafe")
    }

    // 🏪 편의점 마커들
    private fun addConvenienceStoreMarkers(kakaoMap: KakaoMap) {
        val stores = listOf(
            LatLng.from(37.5696805, 126.9754147) to "CU 강남점",
            LatLng.from(37.5706805, 126.9744147) to "GS25 역삼점",
            LatLng.from(37.5716805, 126.9734147) to "세븐일레븐 대학로점"
        )
        addMarkersToMap(kakaoMap, stores, "convenience")
    }

    // 🎯 전체 카테고리 마커들
    private fun addAllCategoryMarkers(kakaoMap: KakaoMap) {
        addRestaurantMarkers(kakaoMap)
        addCafeMarkers(kakaoMap)
        addConvenienceStoreMarkers(kakaoMap)
    }

    // 📍 마커 추가 유틸리티
    private fun addMarkersToMap(kakaoMap: KakaoMap, locations: List<Pair<LatLng, String>>, categoryTag: String) {
        val labelLayer = kakaoMap.labelManager?.layer

        locations.forEachIndexed { index, (position, title) ->
            try {
                // 동일한 태그가 이미 있다면 추가하지 않음 (중복 방지)
                if (labelLayer?.getLabel("${categoryTag}_$index") != null) return@forEachIndexed

                val styles = kakaoMap.labelManager
                    ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

                styles?.let { labelStyles ->
                    val options = LabelOptions.from(position)
                        .setStyles(labelStyles)
                        .setTag("${categoryTag}_$index") // 고유 태그 설정

                    labelLayer?.addLabel(options)
                }
            } catch (e: Exception) {
                println("마커 추가 실패 ($title): ${e.message}")
            }
        }
    }

    // 📍 현재 위치 마커 추가 함수 (private로 분리)
    private fun addCurrentLocationMarker(kakaoMap: KakaoMap, currentLocation: LatLng) {
        try {
            val labelLayer = kakaoMap.labelManager?.layer

            // 🏷️ 현재 위치용 라벨 스타일 생성
            val styles = kakaoMap.labelManager
                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

            styles?.let { labelStyles ->
                // 🏷️ 라벨 옵션 설정
                val options = LabelOptions.from(currentLocation)
                    .setStyles(labelStyles)
                    .setTag("current_location_marker") // 🏷️ 고유 태그

                // 📍 라벨 추가
                labelLayer?.addLabel(options)
                println("✅ 현재 위치 마커 추가: ${currentLocation.latitude}, ${currentLocation.longitude}")
            }
        } catch (e: Exception) {
            println("❌ 현재 위치 마커 추가 실패: ${e.message}")
        }
    }
}