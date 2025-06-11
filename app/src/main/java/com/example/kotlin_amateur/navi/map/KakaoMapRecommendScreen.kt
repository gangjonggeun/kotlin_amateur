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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.location.Location
import android.location.LocationManager

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

    // 📍 위치 관련 상태 - 명확한 네이밍
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isGpsEnabled by remember { mutableStateOf(false) } // 🔥 GPS 활성화 상태
    var showMap by remember { mutableStateOf(false) } // 🔥 지도 표시 여부

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔥 위치 클라이언트
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 🚀 통합된 위치 권한 런처 (중복 제거)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        isGpsEnabled = isLocationServiceEnabled(context)

        when {
            hasLocationPermission && isGpsEnabled -> {
                // ✅ 권한도 있고 GPS도 켜져있음
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = location
                    showMap = true

                    // 🗺️ 지도가 준비된 후 위치 이동
                    kakaoMap?.let { map ->
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 15))
                        addCurrentLocationMarker(map, location)
                    }
                }
            }

            hasLocationPermission && !isGpsEnabled -> {
                // ⚠️ 권한은 있지만 GPS가 꺼져있음 - 설정으로 유도
                showLocationSettingsDialog(context)
            }

            else -> {
                // ❌ 권한이 없음
                showMap = false
                println("⚠️ 위치 권한이 거부되었습니다.")
            }
        }
    }

    // 🚀 GPS 설정 런처
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // GPS 설정 화면에서 돌아온 후 다시 체크
        isGpsEnabled = isLocationServiceEnabled(context)

        if (hasLocationPermission && isGpsEnabled) {
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
                showMap = true

                kakaoMap?.let { map ->
                    map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 15))
                    addCurrentLocationMarker(map, location)
                }
            }
        }
    }

    // 📍 초기 위치 권한 및 GPS 상태 체크
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        isGpsEnabled = isLocationServiceEnabled(context)

        when {
            !hasLocationPermission -> {
                // 🔥 권한이 없으면 즉시 권한 요청 런처 호출
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            hasLocationPermission && !isGpsEnabled -> {
                // 🔥 권한은 있지만 GPS가 꺼져있으면 설정 유도
                showLocationSettingsDialog(context)
            }

            hasLocationPermission && isGpsEnabled -> {
                // ✅ 모든 조건이 만족하면 위치 획득 및 지도 표시
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = location
                    showMap = true
                }
            }
        }
    }

    // 🔄 생명주기 관찰자로 GPS 상태 변경 감지
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 🔥 앱이 포그라운드로 돌아올 때 GPS 상태 재확인
                    val newGpsStatus = isLocationServiceEnabled(context)
                    if (isGpsEnabled != newGpsStatus) {
                        isGpsEnabled = newGpsStatus

                        if (hasLocationPermission && isGpsEnabled) {
                            getCurrentLocation(fusedLocationClient) { location ->
                                currentLocation = location
                                showMap = true

                                kakaoMap?.let { map ->
                                    map.moveCamera(
                                        CameraUpdateFactory.newCenterPosition(
                                            location,
                                            15
                                        )
                                    )
                                    addCurrentLocationMarker(map, location)
                                }
                            }
                        } else if (!isGpsEnabled) {
                            showMap = false
                        }
                    }
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

// 🔄 위치 주기적 갱신 (수정된 버전)
    LaunchedEffect(hasLocationPermission, isGpsEnabled, kakaoMap) {
        if (hasLocationPermission && isGpsEnabled && kakaoMap != null) {
            // 🔥 한 번만 실행하고 필요할 때만 갱신
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
                addCurrentLocationMarker(kakaoMap, location)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try {
                // 🗑️ 지도 리소스 정리
                mapView?.finish()
                mapView = null
                kakaoMap = null

                // 🔄 위치 업데이트 중단
                fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})

                println("🧹 카카오맵 리소스 정리 완료")
            } catch (e: Exception) {
                println("❌ 리소스 정리 실패: ${e.message}")
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // 🗺️ 지도 표시 여부에 따른 조건부 렌더링
        if (showMap) {
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
        } else {
            // 🚫 위치 정보가 없을 때 표시할 화면
            NoLocationScreen(
                hasPermission = hasLocationPermission,
                isGpsEnabled = isGpsEnabled,
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenLocationSettings = {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationSettingsLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 🎯 상단 검색바 & 뒤로가기 (항상 표시)
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

        // 🏷️ 카테고리 필터 (지도가 표시될 때만)
        if (showMap) {
            CategoryFilterSection(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    viewModel.filterByCategory(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 110.dp)
                    .zIndex(9f)
            )

            // 🔥 핫플레이스 플로팅 카드들
            if (showHotPlaceCard) {
                HotPlaceFloatingCards(
                    onDismiss = { showHotPlaceCard = false },
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 88.dp,
                            bottom = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .zIndex(8f)
                )
            }

            // ➕ 개선된 플로팅 액션 버튼들 (지도 표시될 때만)
            ModernMapSpeedDial(
                onCurrentLocationPromoteClick = {
                    currentLocation?.let { location ->
                        println("현재 위치 홍보하기: ${location.latitude}, ${location.longitude}")
                    }
                },
                onNearbyBusinessClick = {
                    println("내 주변 사업체 찾기")
                },
                onLocationRefreshClick = {
                    if (hasLocationPermission && isGpsEnabled) {
                        getCurrentLocation(fusedLocationClient) { location ->
                            currentLocation = location
                            kakaoMap?.let { map ->
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 15))
                                addCurrentLocationMarker(map, location)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(11f)
            )
        }
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

            // 📍 현재 위치 마커 업데이트
            currentLocation?.let { location ->
                KakaoMapUtils.updateCurrentLocationMarker(map, location)
            }
        }
    }
}

// 🎯 상단 검색바 섹션
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
                    .background(Color(0x20667eea), CircleShape)
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .clickable { onSearchToggle() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

            IconButton(
                onClick = onSearchToggle,
                modifier = Modifier
                    .background(Color(0x20667eea), CircleShape)
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

// 🏷️ 카테고리 필터 섹션
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

// 🔥 핫플레이스 플로팅 카드들
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
            delay(3000)
            currentIndex = (currentIndex + 1) % hotPlaces.size
        }
    }

    Card(
        modifier = modifier.height(140.dp),
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
                            .background(Color(0x20667eea), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "핫플레이스 카드 닫기",
                            tint = Color(0xFFFF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

// 🎴 핫플레이스 카드
@Composable
fun HotPlaceCard(hotPlace: HotPlace) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xFF667eea).copy(alpha = 0.1f), CircleShape),
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
                Text(text = hotPlace.rating, fontSize = 12.sp, color = Color(0xFF718096))
                Text(
                    text = hotPlace.promotion,
                    fontSize = 12.sp,
                    color = Color(0xFFE53E3E),
                    fontWeight = FontWeight.Medium
                )
                Text(text = hotPlace.distance, fontSize = 12.sp, color = Color(0xFF718096))
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

// ➕ 개선된 지도용 SpeedDial
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
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapSpeedDialOption(
                    icon = Icons.Default.LocationOn,
                    label = "현재 위치 홍보",
                    backgroundColor = Color(0xFFFF6B6B),
                    onClick = {
                        onCurrentLocationPromoteClick()
                        isExpanded = false
                    }
                )

                MapSpeedDialOption(
                    icon = Icons.Default.Search,
                    label = "주변 사업체",
                    backgroundColor = Color(0xFF4ECDC4),
                    onClick = {
                        onNearbyBusinessClick()
                        isExpanded = false
                    }
                )

                MapSpeedDialOption(
                    icon = Icons.Default.Refresh,
                    label = "위치 갱신",
                    backgroundColor = Color(0xFF45B7D1),
                    onClick = {
                        onLocationRefreshClick()
                        isExpanded = false
                    }
                )
            }
        }

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

// 🎛️ SpeedDial 옵션
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
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

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

// 🚫 위치 정보 없을 때 표시 화면
@Composable
fun NoLocationScreen(
    hasPermission: Boolean,
    isGpsEnabled: Boolean,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 🌍 위치 아이콘
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 📱 상태에 따른 메시지
        val (title, description, buttonText, action) = when {
            !hasPermission -> {
                Quadruple(
                    "위치 권한이 필요해요",
                    "주변 맛집과 카페를 찾기 위해\n위치 권한을 허용해주세요",
                    "위치 권한 허용",
                    onRequestPermission
                )
            }

            !isGpsEnabled -> {
                Quadruple(
                    "GPS를 켜주세요",
                    "정확한 위치를 확인하기 위해\nGPS를 활성화해주세요",
                    "GPS 설정 열기",
                    onOpenLocationSettings
                )
            }

            else -> {
                Quadruple(
                    "위치를 확인하는 중...",
                    "잠시만 기다려주세요",
                    "",
                    {}
                )
            }
        }

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp
        )

        if (buttonText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF667eea)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 🔥 현재 위치 획득 함수 (개선된 버전)
@Suppress("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        // 🎯 고정밀 위치 요청
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateDistanceMeters(10f) // 10미터 이상 이동시에만 업데이트
            setMaxUpdateDelayMillis(10000L) // 최대 10초 지연
        }.build()

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

        // 💾 백업으로 마지막 알려진 위치도 시도
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng.from(it.latitude, it.longitude)
                onLocationReceived(latLng)
            }
        }
    } catch (e: SecurityException) {
        println("❌ 위치 획득 실패: ${e.message}")
    }
}

// 📍 GPS 서비스 활성화 상태 체크
private fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

// 🔧 GPS 설정 화면으로 유도하는 함수
private fun showLocationSettingsDialog(context: Context) {
    println("⚠️ GPS가 비활성화되어 있습니다. 설정에서 위치 서비스를 켜주세요.")
}

// 🗺️ 카카오 지도 설정 (현재 위치 적용 및 표시)
private fun setupKakaoMap(kakaoMap: KakaoMap, currentLocation: LatLng?) {
    // 📍 현재 위치 또는 기본 위치 설정
    val centerLocation = currentLocation ?: LatLng.from(37.5666805, 126.9784147)
    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(centerLocation, 8))

    // 🏷️ 샘플 마커들 먼저 추가
    addSampleKakaoMarkers(kakaoMap)

    // 📍 현재 위치 마커 추가 (위치가 있을 때만)
    currentLocation?.let { location ->
        addCurrentLocationMarker(kakaoMap, location)
        println("🎯 지도 초기화: 현재 위치 마커 추가 완료")
    }
}
// 📍 중복 추가 방지
private fun addCurrentLocationMarker(kakaoMap: KakaoMap?, currentLocation: LatLng) {
    try {
        val labelLayer = kakaoMap?.labelManager?.layer

        // 🔍 이미 있는지 확인
        val existingMarker = labelLayer?.getAllLabels()?.find {
            it.tag == "current_location_marker"
        }

        if (existingMarker != null) {
            println("⚠️ 현재 위치 마커가 이미 존재함 - 추가 중단")
            return
        }

        val styles = kakaoMap?.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(android.R.drawable.ic_menu_mylocation)))

        styles?.let { labelStyles ->
            val options = LabelOptions.from(currentLocation)
                .setStyles(labelStyles)
                .setTag("current_location_marker")

            labelLayer?.addLabel(options)
            println("✅ 현재 위치 마커 추가 성공")
        }

    } catch (e: Exception) {
        println("❌ 현재 위치 마커 추가 실패: ${e.message}")
    }
}

// 🗑️ 현재 위치 마커만 제거하는 함수
private fun removeCurrentLocationMarker(kakaoMap: KakaoMap) {
    try {
        val labelLayer = kakaoMap.labelManager?.layer

        // 🔍 모든 라벨을 검사해서 current_location_marker 찾기
        labelLayer?.getAllLabels()?.forEach { label ->
            if (label.tag == "current_location_marker") {
                labelLayer.remove(label)
                println("🗑️ 현재 위치 마커 제거 성공")
                return
            }
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

// 🏷️ 유틸리티 데이터 클래스
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class HotPlace(
    val id: String,
    val name: String,
    val category: String,
    val rating: String,
    val promotion: String,
    val distance: String
)