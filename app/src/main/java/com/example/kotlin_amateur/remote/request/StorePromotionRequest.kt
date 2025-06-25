package com.example.kotlin_amateur.remote.request

/**
 * 🏪 가게 홍보 등록 요청 DTO (서버 스펙 맞춤)
 *
 * 📌 서버 API와 완벽 호환:
 * - 필드 순서 서버와 일치
 * - 옵셔널 필드 처리 (discountInfo, promotionContent)
 * - 위치 정보 우선 배치 (latitude, longitude)
 * - 메모리 효율적인 nullable 처리
 */
data class StorePromotionRequest(
    val storeName: String,
    val storeType: String,
    val latitude: Double,              // 🔄 순서 변경: 3번째로 이동
    val longitude: Double,             // 🔄 순서 변경: 4번째로 이동
    val discountInfo: String? = null,     // 🔄 옵셔널로 변경
    val promotionContent: String? = null,  // 🔄 옵셔널로 변경
    val postId: Long? = null               // 📝 포스트 ID (선택사항)
) {
    /**
     * 🔍 클라이언트 검증 로직 (메모리 안전)
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // 가게 이름 검증 (Exception 대신 가벼운 검증)
        if (storeName.trim().isEmpty()) {
            errors.add("가게 이름을 입력해주세요")
        }
        if (storeName.length > 50) {
            errors.add("가게 이름은 50자 이내로 입력해주세요")
        }

        // 가게 타입 검증 (영어 코드)
        val validTypes = setOf("restaurant", "cafe", "convenience", "beauty", "fitness", "study", "other")
        if (storeType !in validTypes) {
            errors.add("올바른 가게 타입을 선택해주세요")
        }

        // 위치 정보 검증 (메모리 효율적)
        if (latitude !in -90.0..90.0) {
            errors.add("올바른 위도를 입력해주세요 (-90 ~ 90)")
        }
        if (longitude !in -180.0..180.0) {
            errors.add("올바른 경도를 입력해주세요 (-180 ~ 180)")
        }

        // 옵셔널 필드 검증
        discountInfo?.let { discount ->
            if (discount.length > 100) {
                errors.add("할인 정보는 100자 이내로 입력해주세요")
            }
        }

        promotionContent?.let { content ->
            if (content.length > 200) {
                errors.add("홍보 내용은 200자 이내로 입력해주세요")
            }
        }

        return errors
    }

    /**
     * 🧹 데이터 정리 (메모리 최적화)
     */
    fun sanitize(): StorePromotionRequest {
        return copy(
            storeName = storeName.trim(),
            discountInfo = discountInfo?.trim()?.takeIf { it.isNotEmpty() },
            promotionContent = promotionContent?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    companion object {
        /**
         * 🏷️ 한글 가게 타입 → 영어 코드 매핑
         */
        fun mapKoreanToEnglishType(koreanType: String): String {
            return when (koreanType) {
                "맛집" -> "restaurant"
                "카페" -> "cafe"
                "편의점" -> "convenience"
                "미용" -> "beauty"
                "헬스" -> "fitness"
                "스터디" -> "study"
                "기타" -> "other"
                else -> "other" // 기본값
            }
        }

        /**
         * 🏷️ 영어 코드 → 한글 가게 타입 매핑 (UI 표시용)
         */
        fun mapEnglishToKoreanType(englishType: String): String {
            return when (englishType) {
                "restaurant" -> "맛집"
                "cafe" -> "카페"
                "convenience" -> "편의점"
                "beauty" -> "미용"
                "fitness" -> "헬스"
                "study" -> "스터디"
                "other" -> "기타"
                else -> "기타" // 기본값
            }
        }

        /**
         * 🏷️ 가능한 한글 타입 목록
         */
        fun getKoreanTypes(): List<String> {
            return listOf("맛집", "카페", "편의점", "미용", "헬스", "스터디", "기타")
        }
    }
}