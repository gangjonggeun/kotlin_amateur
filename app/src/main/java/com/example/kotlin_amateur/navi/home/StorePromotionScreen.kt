package com.example.kotlin_amateur.navi.home

// 🎨 Compose Animation
import androidx.compose.animation.*
import androidx.compose.animation.core.*

// 🖼️ Compose Foundation
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.location.LocationManager

import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.StorePromotionViewModel

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

// 🏷️ 키보드 관련
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePromotionScreen(
    viewModel: StorePromotionViewModel,
    onBackPress: () -> Unit
) {
    // 🗺️ 지도 관련 상태
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    // 📍 위치 관련 상태
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isGpsEnabled by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }

    // 🏪 가게 정보 입력 상태
    var storeName by remember { mutableStateOf("") }
    var storeType by remember { mutableStateOf("맛집") }
    var discountInfo by remember { mutableStateOf("") }
    var promotionContent by remember { mutableStateOf("") }

    // 🎨 UI 상태
    var isExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 🔥 위치 클라이언트
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 🚀 위치 권한 런처
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        isGpsEnabled = isLocationServiceEnabled(context)

        when {
            hasLocationPermission && isGpsEnabled -> {
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = location
                    showMap = true
                    kakaoMap?.let { map ->
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 17))
                        addCurrentLocationMarker(map, location)
                    }
                }
            }
            hasLocationPermission && !isGpsEnabled -> {
                showLocationSettingsDialog(context)
            }
            else -> {
                showMap = false
            }
        }
    }

    // 🚀 GPS 설정 런처
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isGpsEnabled = isLocationServiceEnabled(context)
        if (hasLocationPermission && isGpsEnabled) {
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
                showMap = true
                kakaoMap?.let { map ->
                    map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 17))
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
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            hasLocationPermission && !isGpsEnabled -> {
                showLocationSettingsDialog(context)
            }
            hasLocationPermission && isGpsEnabled -> {
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = location
                    showMap = true
                }
            }
        }
    }

    // 🔄 생명주기 관찰자
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val newGpsStatus = isLocationServiceEnabled(context)
                    if (isGpsEnabled != newGpsStatus) {
                        isGpsEnabled = newGpsStatus
                        if (hasLocationPermission && isGpsEnabled) {
                            getCurrentLocation(fusedLocationClient) { location ->
                                currentLocation = location
                                showMap = true
                                kakaoMap?.let { map ->
                                    map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 17))
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

    // 🧹 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                mapView?.finish()
                mapView = null
                kakaoMap = null
                fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
            } catch (e: Exception) {
                println("❌ 리소스 정리 실패: ${e.message}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🗺️ 지도 표시
        if (showMap) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        start(object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {}
                            override fun onMapError(exception: Exception) {
                                exception.printStackTrace()
                            }
                        }, object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                kakaoMap = map
                                currentLocation?.let { location ->
                                    map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 17))
                                    addCurrentLocationMarker(map, location)
                                }
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

        // 🎯 상단 헤더
        StorePromotionHeader(
            onBackPress = onBackPress,
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
        )

        // 🏪 하단 가게 정보 입력 패널
        StoreInfoInputPanel(
            storeName = storeName,
            onStoreNameChange = { storeName = it },
            storeType = storeType,
            onStoreTypeChange = { storeType = it },
            discountInfo = discountInfo,
            onDiscountInfoChange = { discountInfo = it },
            promotionContent = promotionContent,
            onPromotionContentChange = { promotionContent = it },
            isExpanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            isSubmitting = isSubmitting,
            onSubmit = {
                // 🔥 입력 검증
                if (storeName.isBlank()) {
                    return@StoreInfoInputPanel
                }

                isSubmitting = true
                currentLocation?.let { location ->
                    viewModel.submitStorePromotion(
                        storeName = storeName,
                        storeType = storeType,
                        discountInfo = discountInfo,
                        promotionContent = promotionContent,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }

                // TODO: 실제 API 호출 후 결과에 따라 처리
                // 임시로 2초 후 완료 처리
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    delay(2000)
                    isSubmitting = false
                    onBackPress() // 성공 시 이전 화면으로
                }
            },
            keyboardController = keyboardController,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(11f)
        )

        // 📍 현재 위치 새로고침 버튼 (지도 표시될 때만)
        if (showMap) {
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission && isGpsEnabled) {
                        getCurrentLocation(fusedLocationClient) { location ->
                            currentLocation = location
                            kakaoMap?.let { map ->
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(location, 17))
                                addCurrentLocationMarker(map, location)
                            }
                        }
                    }
                },
                containerColor = Color(0xFF667eea),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 80.dp)
                    .size(48.dp)
                    .zIndex(9f)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "현재 위치로 이동",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 🎯 상단 헤더
@Composable
fun StorePromotionHeader(
    onBackPress: () -> Unit,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPress,
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

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "🏪 가게 홍보하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = "현재 위치에 가게 정보를 등록해보세요",
                    fontSize = 14.sp,
                    color = Color(0xFF718096)
                )
            }
        }
    }
}

// 🏪 가게 정보 입력 패널
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreInfoInputPanel(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeType: String,
    onStoreTypeChange: (String) -> Unit,
    discountInfo: String,
    onDiscountInfoChange: (String) -> Unit,
    promotionContent: String,
    onPromotionContentChange: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    val storeTypes = listOf("맛집", "카페", "편의점", "미용", "헬스", "스터디")
    var isStoreTypeExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 🎛️ 핸들 바
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
                    .clickable { onExpandedChange(!isExpanded) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📊 패널 제목
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "가게 정보 입력" else "가게 정보를 입력해주세요",
                    fontSize = if (isExpanded) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                if (!isExpanded) {
                    IconButton(
                        onClick = { onExpandedChange(true) },
                        modifier = Modifier
                            .background(Color(0x20667eea), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ExpandLess,
                            contentDescription = "패널 확장",
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 🎨 확장 상태에 따른 내용 표시
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 🏪 가게 이름 입력
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = onStoreNameChange,
                        label = { Text("가게 이름 *") },
                        placeholder = { Text("예: 맛있는 김밥천국") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )

                    // 🏷️ 가게 타입 선택
                    ExposedDropdownMenuBox(
                        expanded = isStoreTypeExpanded,
                        onExpandedChange = { isStoreTypeExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = storeType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("가게 타입") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStoreTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isStoreTypeExpanded,
                            onDismissRequest = { isStoreTypeExpanded = false }
                        ) {
                            storeTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${getCategoryEmoji(type)} $type",
                                            fontSize = 16.sp
                                        )
                                    },
                                    onClick = {
                                        onStoreTypeChange(type)
                                        isStoreTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 💰 할인 정보 입력
                    OutlinedTextField(
                        value = discountInfo,
                        onValueChange = onDiscountInfoChange,
                        label = { Text("할인 정보") },
                        placeholder = { Text("예: 20% 할인, 2+1 이벤트") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )

                    // 📝 홍보 내용 입력
                    OutlinedTextField(
                        value = promotionContent,
                        onValueChange = {
                            if (it.length <= 200) { // 200자 제한
                                onPromotionContentChange(it)
                            }
                        },
                        label = { Text("홍보 내용") },
                        placeholder = { Text("가게의 특징이나 추천 메뉴를 소개해주세요 (최대 200자)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        supportingText = {
                            Text(
                                text = "${promotionContent.length}/200",
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🚀 등록 버튼
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onSubmit()
                        },
                        enabled = storeName.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea),
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSubmitting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "등록 중...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "🏪 가게 홍보 등록하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 🔽 축소 버튼
                    TextButton(
                        onClick = { onExpandedChange(false) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "패널 축소",
                            tint = Color(0xFF718096),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "축소",
                            color = Color(0xFF718096),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 🎯 축소 상태일 때의 간단한 정보 표시
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = if (storeName.isBlank()) "가게 이름을 입력하세요" else storeName,
                        fontSize = 14.sp,
                        color = if (storeName.isBlank()) Color(0xFF999999) else Color(0xFF2D3748),
                        modifier = Modifier.weight(1f)
                    )

                    if (storeName.isNotBlank()) {
                        Text(
                            text = getCategoryEmoji(storeType),
                            fontSize = 16.sp
                        )
                    }
                }
            }
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
                    "가게 위치를 등록하기 위해\n위치 권한을 허용해주세요",
                    "위치 권한 허용",
                    onRequestPermission
                )
            }

            !isGpsEnabled -> {
                Quadruple(
                    "GPS를 켜주세요",
                    "정확한 가게 위치를 등록하기 위해\nGPS를 활성화해주세요",
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
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
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

// 🔥 현재 위치 획득 함수
@Suppress("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateDistanceMeters(10f)
            setMaxUpdateDelayMillis(10000L)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val latLng = LatLng.from(location.latitude, location.longitude)
                        onLocationReceived(latLng)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            },
            null
        )

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

// 📍 현재 위치 마커 추가
private fun addCurrentLocationMarker(kakaoMap: KakaoMap?, currentLocation: LatLng) {
    try {
        val labelLayer = kakaoMap?.labelManager?.layer

        // 🔍 이미 있는지 확인
        val existingMarker = labelLayer?.getAllLabels()?.find {
            it.tag == "store_location_marker"
        }

        if (existingMarker != null) {
            labelLayer.remove(existingMarker)
        }

        val styles = kakaoMap?.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.red_dot_11)))

        styles?.let { labelStyles ->
            val options = LabelOptions.from(currentLocation)
                .setStyles(labelStyles)
                .setTag("store_location_marker")

            labelLayer?.addLabel(options)
            println("✅ 가게 위치 마커 추가 성공")
        }

    } catch (e: Exception) {
        println("❌ 가게 위치 마커 추가 실패: ${e.message}")
    }
}

// 🏷️ 카테고리별 이모지 반환
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