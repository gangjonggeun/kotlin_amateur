package com.example.kotlin_amateur.post.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.example.kotlin_amateur.viewmodel.FloatingAddViewModel
import com.example.kotlin_amateur.state.SubmitResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FloatingAddScreen(
    modifier: Modifier = Modifier,
    viewModel: FloatingAddViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackPress: () -> Unit = {}
) {
    // 🔥 ViewModel State 처리
    val submitResult by viewModel.submitResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 🔥 멀티 이미지 선택 런처
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    // 🎨 트렌디한 그라데이션 배경
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F9FF),
            Color(0xFFFFFFFF),
            Color(0xFFF0F4FF)
        )
    )

    // 🔥 결과 처리 (Toast 메시지)
    LaunchedEffect(submitResult) {
        when (submitResult) {
            is SubmitResult.Success -> {
                Toast.makeText(context, "게시글이 성공적으로 업로드되었습니다! 🎉", Toast.LENGTH_SHORT).show()
                onBackPress() // 성공 시 뒤로가기
            }
            is SubmitResult.Failure -> {
                Toast.makeText(context, "에러: ${(submitResult as SubmitResult.Failure).exception.message}", Toast.LENGTH_LONG).show()
                viewModel.clearSubmitResult()
            }
            else -> { /* 아무 작업 안함 */ }
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = "새 게시글",
                onBackClick = onBackPress
            )
        },
        bottomBar = {
            SubmitButton(
                enabled = viewModel.title.isNotBlank() &&
                         viewModel.content.isNotBlank() &&
                         viewModel.selectedImages.isNotEmpty() &&
                         viewModel.selectedCommunity.isNotBlank(),
                isLoading = isLoading,
                onClick = {
                    viewModel.submitPost()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 📸 사진 업로드 섹션
            PhotoUploadSection(
                images = viewModel.selectedImages,
                onAddPhotos = {
                    if (viewModel.selectedImages.size < 6) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
                onRemovePhoto = { index ->
                    viewModel.removeImage(index)
                },
                onReorderPhotos = { fromIndex, toIndex ->
                    // 🔥 메모리 안전한 순서 변경
                    viewModel.reorderImages(fromIndex, toIndex)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🏘️ 커뮤니티 선택 섹션
            CommunitySelectSection(
                selectedCommunity = viewModel.selectedCommunity,
                onCommunitySelected = { viewModel.updateSelectedCommunity(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✏️ 제목 입력 섹션
            TitleInputSection(
                title = viewModel.title,
                onTitleChange = { viewModel.updateTitle(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 📝 내용 입력 섹션
            ContentInputSection(
                content = viewModel.content,
                onContentChange = { viewModel.updateContent(it) }
            )

            Spacer(modifier = Modifier.height(100.dp)) // 하단 버튼 여백
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF666666)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoUploadSection(
    images: List<Uri>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onReorderPhotos: (Int, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📸 사진",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "${images.size}/6",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                modifier = Modifier
                    .background(
                        Color(0xFFF0F4FF),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 개선된 LazyRow - 사용자 인터랙션 최적화
        LazyRow(
            state = rememberLazyListState(), // 🔥 상태 관리로 스크롤 개선
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // 📷 사진 추가 버튼
            item {
                AddPhotoButton(
                    enabled = images.size < 6,
                    onClick = onAddPhotos
                )
            }

            // 🖼️ 선택된 사진들 - 드래그 앤 드롭 지원
            itemsIndexed(
                items = images,
                key = { index, uri -> "$uri-$index" }
            ) { index, uri ->
                DraggablePhotoItem(
                    uri = uri,
                    index = index,
                    isFirst = index == 0,
                    onRemove = { onRemovePhoto(index) },
                    onMove = onReorderPhotos,
                    modifier = Modifier.animateItemPlacement(
                        // 🎨 부드러운 애니메이션
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun AddPhotoButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "addButtonScale"
    )

    Card(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0xFFF8F9FF) else Color(0xFFF5F5F5)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (enabled) Color(0xFF6366F1) else Color(0xFFE5E5E5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "사진 추가",
                modifier = Modifier.size(32.dp),
                tint = if (enabled) Color(0xFF6366F1) else Color(0xFFBBBBBB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (enabled) "사진 추가" else "최대 6장",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color(0xFF6366F1) else Color(0xFFBBBBBB),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhotoItem(
    uri: Uri,
    isFirst: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "photoScale"
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false }
                ) { _, _ ->
                    // 드래그 앤 드롭 구현 (향후 확장)
                }
            }
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "선택된 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 🏆 대표 사진 라벨
        AnimatedVisibility(
            visible = isFirst,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xCC000000)
                )
            ) {
                Text(
                    text = "🏆 대표 사진",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // ❌ 삭제 버튼
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    Color(0xCC000000),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "사진 삭제",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// 🔥 드래그 앤 드롭 가능한 사진 아이템
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraggablePhotoItem(
    uri: Uri,
    index: Int,
    isFirst: Boolean,
    onRemove: () -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var isDragReady by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var startPosition by remember { mutableStateOf(Offset.Zero) }

    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = when {
            isDragging -> 1.2f
            isDragReady -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dragScale"
    )

    val elevation by animateFloatAsState(
        targetValue = when {
            isDragging -> 24f
            isDragReady -> 12f
            else -> 8f
        },
        animationSpec = tween(200),
        label = "dragElevation"
    )

    // 🎯 아이템 크기와 간격
    val itemWidth = 120f + 12f // 아이템 크기 + 간격

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .graphicsLayer {
                // 🎨 드래그 중일 때만 이동 표시
                translationX = if (isDragging) dragOffset.x else 0f
                translationY = if (isDragging) dragOffset.y else 0f
                // 🎨 드래그 중 약간의 회전으로 생동감 추가
                rotationZ = if (isDragging) (dragOffset.x * 0.05f).coerceIn(-5f, 5f) else 0f
            }
            .zIndex(if (isDragging) 1f else 0f) // 🔥 드래그 중 최상위로
            .pointerInput(Unit) {
                // 🔥 길게 누르기 감지
                detectTapGestures(
                    onLongPress = { offset ->
                        isDragReady = true
                        startPosition = offset
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }
            .pointerInput(isDragReady) {
                if (isDragReady) {
                    // 🔥 드래그 앤 드롭 구현
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragOffset = Offset.Zero
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            // 🎯 드롭 위치 계산 및 순서 변경
                            val totalMoveX = dragOffset.x
                            val itemsMoved = (totalMoveX / itemWidth).toInt()

                            val newIndex = when {
                                itemsMoved > 0 -> {
                                    // 우측으로 이동
                                    (index + itemsMoved).coerceAtMost(5) // 최대 6개까지
                                }
                                itemsMoved < 0 -> {
                                    // 좌측으로 이동
                                    (index + itemsMoved).coerceAtLeast(0)
                                }
                                else -> index
                            }

                            // 🎯 실제 순서 변경 (최소 30dp 이상 이동해야 함)
                            if (kotlin.math.abs(totalMoveX) > 30f && newIndex != index) {
                                onMove(index, newIndex)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            // 🔄 상태 초기화
                            isDragging = false
                            isDragReady = false
                            dragOffset = Offset.Zero
                            startPosition = Offset.Zero
                        },
                        onDrag = { change, dragAmount ->
                            // 🎨 자유로운 드래그 (모든 방향)
                            dragOffset += dragAmount
                            change.consume()
                        }
                    )
                }
            }
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDragging) Color(0xFFF8F9FF) else Color.White
            )
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "선택된 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 🏆 대표 사진 라벨 (더 예쁜 디자인)
        AnimatedVisibility(
            visible = isFirst,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xDD6366F1),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 8.sp
                    )
                    Text(
                        text = "대표",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ❌ 개선된 삭제 버튼 (더 작고 우측 상단에)
        Surface(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp) // 🔥 더 우측 상단으로 이동
                .size(22.dp), // 🔥 작아진 사이즈
            shape = CircleShape,
            color = Color(0xEE000000),
            shadowElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "사진 삭제",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp) // 🔥 아이콘도 작게
                )
            }
        }
        
        // 📱 드래그 준비 인디케이터
        AnimatedVisibility(
            visible = isDragReady && !isDragging,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xAA6366F1),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "🎯 드래그해서 이동",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // 📦 드래그 중 표시
        AnimatedVisibility(
            visible = isDragging,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCC000000),
                modifier = Modifier.padding(8.dp)
            ) {
                val targetPosition = kotlin.math.abs(dragOffset.x / itemWidth).toInt()
                val moveDirection = when {
                    dragOffset.x > 30f -> "📦 ${index + targetPosition + 1}번째로 이동"
                    dragOffset.x < -30f -> "📦 ${(index - targetPosition).coerceAtLeast(1)}번째로 이동"
                    else -> "📍 드래그 중..."
                }

                Text(
                    text = moveDirection,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunitySelectSection(
    selectedCommunity: String,
    onCommunitySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val communities = listOf(
        "🏠 아파트 단지",
        "🥊 복싱장 커뮤",
        "🏘️ 동네 커뮤",
        "🏃 헬스장 커뮤",
        "☕ 카페 커뮤",
        "🛒 마트 커뮤"
    )
    
    Column {
        Text(
            text = "🏘️ 커뮤니티",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCommunity.ifEmpty { "커뮤니티를 선택하세요" },
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "드롭다운",
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (expanded) 180f else 0f
                        }
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    focusedLabelColor = Color(0xFF6366F1)
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                communities.forEach { community ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = community,
                                fontSize = 16.sp,
                                fontWeight = if (community == selectedCommunity) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onCommunitySelected(community)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TitleInputSection(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column {
        Text(
            text = "✏️ 제목",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = {
                Text(
                    text = "제목을 입력하세요",
                    color = Color(0xFF999999)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFFE5E5E5),
                focusedLabelColor = Color(0xFF6366F1)
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentInputSection(
    content: String,
    onContentChange: (String) -> Unit
) {
    Column {
        Text(
            text = "📝 내용",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            placeholder = {
                Text(
                    text = "내용을 입력하세요",
                    color = Color(0xFF999999)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFFE5E5E5),
                focusedLabelColor = Color(0xFF6366F1)
            ),
            maxLines = 8
        )
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    var buttonScale by remember { mutableStateOf(1f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                buttonScale = 1.02f
                delay(1000)
                buttonScale = 1f
                delay(1000)
            }
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shadowElevation = if (enabled) 12.dp else 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(buttonScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color(0xFF6366F1) else Color(0xFFCCCCCC),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "🚀 게시글 올리기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}