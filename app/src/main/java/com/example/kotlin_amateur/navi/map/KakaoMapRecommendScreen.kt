package com.example.kotlin_amateur.navi.map

// 🎨 Compose Animation
import androidx.compose.animation.*
import androidx.compose.animation.core.*

// 🖼️ Compose Foundation
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// 🎨 Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// 🎨 Material3
import androidx.compose.material3.*

// 🔧 Compose Runtime
import androidx.compose.runtime.*

// 🖥️ Compose UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.MapRecommendViewModel

// 🗺️ 카카오 지도 SDK (올바른 import)
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

// ⏰ Coroutines
import kotlinx.coroutines.delay
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoMapRecommendScreen(
    viewModel: MapRecommendViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("전체") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 🗺️ 카카오 지도
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // 🔧 카카오 지도 초기화
                    start(object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            // 지도 종료시 정리
                        }

                        override fun onMapError(exception: Exception) {
                            // 지도 에러 처리
                            exception.printStackTrace()
                        }
                    }, object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                            setupKakaoMap(map)
                        }
                    })
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { view ->
                view.finish()
            }
        )

        // 🎯 상단 검색바 & 뒤로가기
        TopSearchSection(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isSearchVisible = isSearchVisible,
            onSearchToggle = { isSearchVisible = !isSearchVisible },
            onNavigateBack = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
        )

        // 🏷️ 카테고리 필터
        CategoryFilterSection(
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
                viewModel.filterByCategory(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp)
                .zIndex(9f)
        )

        // 🔥 핫플레이스 플로팅 카드들
        HotPlaceFloatingCards(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(8f)
        )

        // ➕ 플로팅 액션 버튼 (홍보 등록)
        FloatingActionButton(
            onClick = { /* 홍보 등록 화면으로 */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(11f),
            containerColor = Color(0xFF667eea),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "홍보 등록")
        }
    }

    // 🔍 검색 기능
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500) // 디바운싱
            viewModel.searchPlaces(searchQuery)
        }
    }

    // 📍 지도 위치 변경 감지
    LaunchedEffect(selectedCategory) {
        kakaoMap?.let { map ->
            updateMapMarkers(map, selectedCategory)
        }
    }
}

@Composable
fun TopSearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchVisible: Boolean,
    onSearchToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로가기 버튼
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Color(0x20667eea),
                        CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF667eea)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 검색창
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("맛집, 카페 검색...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (!isSearchVisible) {
                // 제목
                Text(
                    text = "🔥 동네 핫플레이스",
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            }

            // 검색 토글 버튼
            IconButton(
                onClick = onSearchToggle,
                modifier = Modifier
                    .background(
                        Color(0x20667eea),
                        CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color(0xFF667eea)
                )
            }
        }
    }
}

@Composable
fun CategoryFilterSection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("전체", "맛집", "카페", "편의점", "미용", "헬스", "스터디")

    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selectedCategory == category,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF667eea),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.9f),
                    labelColor = Color(0xFF4A5568)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = Color(0xFF667eea),
                    selectedBorderColor = Color(0xFF667eea)
                )
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HotPlaceFloatingCards(
    modifier: Modifier = Modifier
) {
    // 🔥 샘플 핫플레이스 데이터
    val hotPlaces = remember {
        listOf(
            HotPlace("1", "브런치 카페 모모", "카페", "⭐ 4.8", "🔥 실시간 인기", "🚶‍♂️ 3분"),
            HotPlace("2", "돈까스 맛집 우동이", "맛집", "⭐ 4.9", "🎉 30% 할인", "🚶‍♂️ 5분"),
            HotPlace("3", "24시 스터디카페", "스터디", "⭐ 4.7", "💺 좌석 여유", "🚶‍♂️ 2분")
        )
    }

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // 3초마다 자동 스크롤
            currentIndex = (currentIndex + 1) % hotPlaces.size
        }
    }

    Card(
        modifier = modifier
            .padding(16.dp)
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 지금 핫한 곳",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                Text(
                    text = "${currentIndex + 1}/${hotPlaces.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF718096),
                    modifier = Modifier
                        .background(
                            Color(0x20667eea),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 핫플레이스 카드
            AnimatedContent(
                targetState = hotPlaces[currentIndex],
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() with
                            slideOutHorizontally { -it } + fadeOut()
                }
            ) { hotPlace ->
                HotPlaceCard(hotPlace = hotPlace)
            }
        }
    }
}

@Composable
fun HotPlaceCard(hotPlace: HotPlace) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 카테고리 아이콘
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Color(0xFF667eea).copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getCategoryEmoji(hotPlace.category),
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hotPlace.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = hotPlace.rating,
                    fontSize = 12.sp,
                    color = Color(0xFF718096)
                )
                Text(
                    text = hotPlace.promotion,
                    fontSize = 12.sp,
                    color = Color(0xFFE53E3E),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = hotPlace.distance,
                    fontSize = 12.sp,
                    color = Color(0xFF718096)
                )
            }
        }

        IconButton(
            onClick = { /* 상세보기 */ },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "상세보기",
                tint = Color(0xFF667eea)
            )
        }
    }
}

// 🗺️ 카카오 지도 설정
private fun setupKakaoMap(kakaoMap: KakaoMap) {
    // 🏠 서울 중심으로 초기 위치 설정
    val seoul = LatLng.from(37.5666805, 126.9784147)

    // 📷 카메라 위치 이동
    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(seoul, 15))

    // 🏷️ 샘플 마커들 추가
    addSampleKakaoMarkers(kakaoMap)
}

private fun addSampleKakaoMarkers(kakaoMap: KakaoMap) {
    val places = listOf(
        Triple(LatLng.from(37.5666805, 126.9784147), "브런치 카페 모모", "☕"),
        Triple(LatLng.from(37.5656805, 126.9794147), "돈까스 맛집 우동이", "🍽️"),
        Triple(LatLng.from(37.5676805, 126.9774147), "24시 스터디카페", "📚")
    )

    // 🏷️ 라벨 레이어 가져오기
    val labelLayer = kakaoMap.labelManager?.layer

    places.forEach { (position, title, emoji) ->
        // 🎨 라벨 스타일 생성
        val styles = kakaoMap.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

        styles?.let { labelStyles ->
            // 🏷️ 라벨 옵션 설정
            val options = LabelOptions.from(position)
                .setStyles(labelStyles)
//                .setTexts(title)
//TODO: 임시로 주석 처리함 고쳐야함 여기
            // 📍 라벨 추가
            labelLayer?.addLabel(options)
        }
    }
}

private fun updateMapMarkers(kakaoMap: KakaoMap, category: String) {
    // TODO: 카테고리에 따른 마커 업데이트 로직
    val labelLayer = kakaoMap.labelManager?.layer
    labelLayer?.removeAll() // 기존 마커 제거

    // 새로운 마커 추가 (카테고리별 필터링)
    addSampleKakaoMarkers(kakaoMap)
}

private fun getCategoryEmoji(category: String): String {
    return when (category) {
        "카페" -> "☕"
        "맛집" -> "🍽️"
        "스터디" -> "📚"
        "편의점" -> "🏪"
        "미용" -> "💄"
        "헬스" -> "💪"
        else -> "📍"
    }
}

// 🏷️ 데이터 클래스 (동일)
data class HotPlace(
    val id: String,
    val name: String,
    val category: String,
    val rating: String,
    val promotion: String,
    val distance: String
)