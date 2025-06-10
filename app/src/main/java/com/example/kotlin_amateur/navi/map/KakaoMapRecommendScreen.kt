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

// 📍 위치 관련
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.location.Location

import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.MapRecommendViewModel

// 🗺️ 카카오 지도 SDK
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

// 🎨 Compose Canvas
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp

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

    // 🆕 핫플레이스 카드 표시 상태
    var showHotPlaceCard by remember { mutableStateOf(true) }

    // 📍 위치 관련 상태
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔥 위치 클라이언트 (갱신을 위해 remember로 관리)
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 위치 권한 요청 런처
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // 권한 획득 시 즉시 위치 갱신
        if (hasLocationPermission) {
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(location, 15)
                )
            }
        }
    }

    // 위치 권한 체크 및 요청
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            hasLocationPermission = true
            // 권한이 있으면 즉시 위치 획득
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 🔄 위치 주기적 갱신 (5초마다)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = location
                    // 📍 간단하게 GPS 좌표에 마커만 찍기
                }
                delay(10000) // 5초마다 갱신
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🗺️ 카카오 지도
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
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
                            setupKakaoMap(map, currentLocation)
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

        // 🏷️ 카테고리 필터 (🔥 간격 조정: 90dp → 110dp)
        CategoryFilterSection(
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
                viewModel.filterByCategory(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 110.dp) // 🔥 검색창과 더 아래로
                .zIndex(9f)
        )

        // 🔥 핫플레이스 플로팅 카드들 (X 버튼 크기 줄임)
        if (showHotPlaceCard) {
            HotPlaceFloatingCards(
                onDismiss = { showHotPlaceCard = false },
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 88.dp, // 🔥 플로팅 버튼 크기(56dp) + 여백(32dp)
                        bottom = 16.dp // 🔥 플로팅 버튼과 같은 높이
                    )
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .zIndex(8f)
            )
        }

        // ➕ 개선된 플로팅 액션 버튼들
        ModernMapSpeedDial(
            onCurrentLocationPromoteClick = {
                // 🔥 현재 위치 홍보하기
                currentLocation?.let { location ->
                    // TODO: 현재 위치 기반 홍보 등록 화면으로 이동
                    println("현재 위치 홍보하기: ${location.latitude}, ${location.longitude}")
                }
            },
            onNearbyBusinessClick = {
                // 🔥 내 주변 사업체 찾기
                println("내 주변 사업체 찾기")
            },
            onLocationRefreshClick = {
                // 🔥 위치 갱신
                if (hasLocationPermission) {
                    getCurrentLocation(fusedLocationClient) { location ->
                        currentLocation = location
                        kakaoMap?.moveCamera(
                            CameraUpdateFactory.newCenterPosition(location, 15)
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(11f)
        )
    }

    // 🔍 검색 기능
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500) // 디바운싱
            viewModel.searchPlaces(searchQuery)
        }
    }

    // 📍 지도 위치 변경 감지 및 현재 위치 업데이트
    LaunchedEffect(selectedCategory, currentLocation) {
        kakaoMap?.let { map ->
            updateMapMarkers(map, selectedCategory, currentLocation)
            
            // 📍 현재 위치 마커 업데이트 (KakaoMapUtils 사용)
            currentLocation?.let { location ->
                KakaoMapUtils.updateCurrentLocationMarker(map, location)
            }
        }
    }
}

// 🔥 현재 위치 획득 함수 (갱신 가능)
@Suppress("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        // 최신 위치 요청
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5초 간격
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val latLng = LatLng.from(location.latitude, location.longitude)
                        onLocationReceived(latLng)
                        // 한 번만 받으면 되므로 콜백 제거
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            },
            null
        )

        // 백업으로 마지막 알려진 위치도 시도
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng.from(it.latitude, it.longitude)
                onLocationReceived(latLng)
            }
        }
    } catch (e: SecurityException) {
        // 권한이 없는 경우 기본 위치 사용
        onLocationReceived(LatLng.from(37.5666805, 126.9784147))
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
            if (isSearchVisible) {
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
            } else {
                // 검색창 플레이스홀더 (클릭 가능)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            Color(0xFFF5F5F5),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSearchToggle() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "맛집, 카페 검색...",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 검색/닫기 토글 버튼
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
                    contentDescription = if (isSearchVisible) "검색 닫기" else "검색",
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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    // 🔥 X 닫기 버튼 (크기 줄임, 원형 배경 제거)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp) // 🔥 24dp → 20dp
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "핫플레이스 카드 닫기",
                            tint = Color(0xFFFF4444), // 🔥 빨간색만 유지
                            modifier = Modifier.size(16.dp) // 🔥 아이콘 크기도 줄임
                        )
                    }
                }
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

// 🆕 개선된 지도용 SpeedDial
@Composable
fun ModernMapSpeedDial(
    onCurrentLocationPromoteClick: () -> Unit,
    onNearbyBusinessClick: () -> Unit,
    onLocationRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 확장된 옵션들
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 현재 위치 홍보하기
                MapSpeedDialOption(
                    icon = Icons.Default.LocationOn, // 홍보 아이콘
                    label = "현재 위치 홍보",
                    backgroundColor = Color(0xFFFF6B6B), // 빨간색
                    onClick = {
                        onCurrentLocationPromoteClick()
                        isExpanded = false
                    }
                )

                // 내 주변 사업체 찾기
                MapSpeedDialOption(
                    icon = Icons.Default.Search,
                    label = "주변 사업체",
                    backgroundColor = Color(0xFF4ECDC4), // 청록색
                    onClick = {
                        onNearbyBusinessClick()
                        isExpanded = false
                    }
                )

                // 위치 갱신
                MapSpeedDialOption(
                    icon = Icons.Default.Refresh,
                    label = "위치 갱신",
                    backgroundColor = Color(0xFF45B7D1), // 파란색
                    onClick = {
                        onLocationRefreshClick()
                        isExpanded = false
                    }
                )
            }
        }

        // 메인 플로팅 버튼
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = Color(0xFF667eea),
            contentColor = Color.White,
            modifier = Modifier.size(56.dp)
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    slideInVertically() + fadeIn() togetherWith
                            slideOutVertically() + fadeOut()
                }
            ) { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (expanded) "닫기" else "열기"
                )
            }
        }
    }
}

@Composable
fun MapSpeedDialOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 라벨
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                )
            )
        }

        // 버튼
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = backgroundColor,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 🗺️ 카카오 지도 설정 (현재 위치 적용 및 표시)
private fun setupKakaoMap(kakaoMap: KakaoMap, currentLocation: LatLng?) {
    // 📍 현재 위치 또는 기본 위치 설정
    val centerLocation = currentLocation ?: LatLng.from(37.5666805, 126.9784147)

    // 📷 카메라 위치 이동
    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(centerLocation, 15))

    // 📍 현재 위치 마커 추가 (위치가 있을 때만)
    currentLocation?.let { location ->
        addCurrentLocationMarker(kakaoMap, location)
        println("🎯 지도 초기화: 현재 위치로 설정 완료")
    } ?: run {
        println("⚠️ 지도 초기화: 기본 위치로 설정")
    }

    // 🏷️ 샘플 마커들 추가
    addSampleKakaoMarkers(kakaoMap)
}

// 📍 현재 위치 마커 추가 함수 (수정된 버전)
private fun addCurrentLocationMarker(kakaoMap: KakaoMap, currentLocation: LatLng) {
    try {
        // 🗑️ 기존 마커 제거 (중요!)
        removeCurrentLocationMarker(kakaoMap)

        val labelLayer = kakaoMap.labelManager?.layer

        // 🏷️ 현재 위치용 라벨 스타일 생성
        val styles = kakaoMap.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

        styles?.let { labelStyles ->
            // 🏷️ 라벨 옵션 설정 (고유 태그 사용)
            val options = LabelOptions.from(currentLocation)
                .setStyles(labelStyles)
                .setTag("current_location_marker") // 🏷️ 현재 위치 마커 식별용 고유 태그

            // 📍 라벨 추가
            labelLayer?.addLabel(options)
            println("✅ 현재 위치 마커 추가 완료: ${currentLocation.latitude}, ${currentLocation.longitude}")
        }
    } catch (e: Exception) {
        println("❌ 현재 위치 마커 추가 실패: ${e.message}")
    }
}

// 🗑️ 현재 위치 마커만 제거하는 함수 (새로 추가)
private fun removeCurrentLocationMarker(kakaoMap: KakaoMap) {
    try {
        val labelLayer = kakaoMap.labelManager?.layer
        val currentLocationLabel = labelLayer?.getLabel("current_location_marker")

        currentLocationLabel?.let { label ->
            labelLayer.remove(label)
            println("🗑️ 기존 현재 위치 마커 제거 완료")
        }
    } catch (e: Exception) {
        println("❌ 현재 위치 마커 제거 실패: ${e.message}")
    }
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
        try {
            // 🎨 라벨 스타일 생성
            val styles = kakaoMap.labelManager
                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

            styles?.let { labelStyles ->
                // 🏷️ 라벨 옵션 설정
                val options = LabelOptions.from(position)
                    .setStyles(labelStyles)

                // 📍 라벨 추가
                labelLayer?.addLabel(options)
            }
        } catch (e: Exception) {
            // 마커 추가 실패 시 로그만 출력
            println("마커 추가 실패: ${e.message}")
        }
    }
}

private fun updateMapMarkers(kakaoMap: KakaoMap, category: String, currentLocation: LatLng?) {
    // 🎨 KakaoMapUtils를 사용하여 카테고리별 마커 업데이트
    KakaoMapUtils.updateMarkersByCategory(kakaoMap, category, currentLocation)
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
// 📍 GPS 마커 컴포넌트 (카카오맵용)
@Composable
fun KakaoMapGpsMarker(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    isAnimated: Boolean = true,
    accuracy: Float = 10f // GPS 정확도
) {
    // 🌊 맥동 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAnimated) 1.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isAnimated) 0.1f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val baseRadius = size.toPx() / 3.5f

        // 🌊 정확도 범위 표시 (맥동 효과)
        if (isAnimated) {
            // 외곽 정확도 원 (실제 GPS 정확도 기반)
            val accuracyRadius = baseRadius * (accuracy / 10f).coerceIn(1f, 3f)
            drawCircle(
                color = Color(0xFF2196F3).copy(alpha = pulseAlpha * 0.2f),
                radius = accuracyRadius * pulseScale,
                center = center
            )
            
            // 중간 범위
            drawCircle(
                color = Color(0xFF2196F3).copy(alpha = pulseAlpha * 0.4f),
                radius = baseRadius * pulseScale * 1.6f,
                center = center
            )
        }

        // 🔷 메인 외곽 테두리 (진한 파란색)
        drawCircle(
            color = Color(0xFF1565C0),
            radius = baseRadius * 1.3f,
            center = center
        )

        // ⚪ 흰색 베이스 (대비 강화)
        drawCircle(
            color = Color.White,
            radius = baseRadius * 1.15f,
            center = center
        )

        // 🔵 메인 GPS 도트
        drawCircle(
            color = Color(0xFF2196F3),
            radius = baseRadius,
            center = center
        )

        // ✨ 중심 하이라이트
        drawCircle(
            color = Color(0xFF64B5F6),
            radius = baseRadius * 0.65f,
            center = center
        )

        // 💎 정확한 위치점
        drawCircle(
            color = Color(0xFF0D47A1),
            radius = baseRadius * 0.3f,
            center = center
        )
    }
}

// 🎨 GPS 정확도에 따른 동적 마커
@Composable
fun AdaptiveKakaoGpsMarker(
    accuracy: Float, // GPS 정확도 (미터)
    modifier: Modifier = Modifier
) {
    val (size, animated) = when {
        accuracy < 10f -> 20.dp to true     // 🎩 매우 정확 - 작고 애니메이션
        accuracy < 50f -> 24.dp to true     // 📍 보통 - 기본 크기
        accuracy < 100f -> 28.dp to false   // ⚠️ 부정확 - 크고 정적
        else -> 32.dp to false              // 🚨 매우 부정확 - 가장 크게
    }
    
    KakaoMapGpsMarker(
        modifier = modifier,
        size = size,
        isAnimated = animated,
        accuracy = accuracy
    )
}
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
            val hasCurrentLocationMarker = kakaoMap.labelManager?.layer?.getLabel("current_location_marker") != null

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

            // 📍 현재 위치 마커가 있었다면 다시 추가
            if (currentLocation != null && !hasCurrentLocationMarker) {
                addCurrentLocationMarker(kakaoMap, currentLocation)
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
}

data class HotPlace(
    val id: String,
    val name: String,
    val category: String,
    val rating: String,
    val promotion: String,
    val distance: String
)