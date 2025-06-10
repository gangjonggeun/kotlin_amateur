package com.example.kotlin_amateur.navi.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlin_amateur.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Scale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.kotlin_amateur.R

// 🎨 브랜드 컬러 정의
object BrandColors {
    val Primary = Color(0xFF667eea)
    val PrimaryLight = Color(0xFF8fa4f3)
    val PrimaryDark = Color(0xFF3f51b5)
    val Secondary = Color(0xFFE8F0FE)
    val Surface = Color(0xFFFAFBFF)
    val OnSurface = Color(0xFF1A1C20)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHomeScreen(
    onNavigateToAddPost: () -> Unit,
    onNavigateToPostDetail: (String, String?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 🔥 StateFlow 상태 수집
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoadingFlow.collectAsState()
    val errorMessage by viewModel.errorMessageFlow.collectAsState()
    val filteredPosts by viewModel.filteredPosts.collectAsState()

    // UI 상태
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSpeedDial by remember { mutableStateOf(true) }

    // 검색어 업데이트
    LaunchedEffect(searchQuery) {
        viewModel.updateSearchQuery(searchQuery)
    }

    // 🔍 실제 표시할 게시글
    val displayPosts = if (isSearchActive && searchQuery.isNotBlank()) {
        filteredPosts
    } else {
        posts
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.Secondary,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🎯 모던한 상단 바 (🔥 텍스트 줄임)
            ModernTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            // 📱 메인 콘텐츠 (SwipeRefresh 추가)
            Box(modifier = Modifier.weight(1f)) {
                if (displayPosts.isEmpty() && !isLoading) {
                    EmptyStateContent(
                        isSearchMode = isSearchActive && searchQuery.isNotBlank(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 🔥 새로고침 인디케이터 (당겨서 새로고침 효과)
                        if (isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(16.dp),
                                        color = BrandColors.Primary
                                    )
                                }
                            }
                        }

                        items(
                            items = displayPosts,
                            key = { post -> post.postId }
                        ) { post ->
                            ModernPostCard(
                                post = post,
                                onPostClick = {
                                    onNavigateToPostDetail(post.postId, post.postTitle)
                                },
                                onLikeClick = {
                                    viewModel.toggleLike(post.postId, !post.isLikedByCurrentUser) { success ->
                                        if (!success) {
                                            // 에러 처리는 ViewModel에서 처리
                                        }
                                    }
                                },
                                onProfileClick = { userId ->
                                    // 프로필 클릭 처리 - TODO: 프로필 화면으로 이동
                                    println("프로필 클릭: $userId")
                                }
                            )
                        }
                    }
                }

                // 🔥 로딩 오버레이 (전체 화면)
                if (isLoading && displayPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandColors.Primary)
                    }
                }
            }
        }

        // 🚀 개선된 플로팅 액션 버튼들 (🔥 색상 다양화)
        AnimatedVisibility(
            visible = showSpeedDial,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ModernSpeedDial(
                onAddPostClick = onNavigateToAddPost,
                onLocationPromoteClick = {
                    // 🔥 지역 홍보 기능
                    println("지역 홍보 기능")
                },
                modifier = Modifier.padding(16.dp)
            )
        }

        // 에러 스낵바
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                // TODO: 스낵바 표시 로직 구현
                viewModel.clearErrorMessage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        if (isSearchActive) {
            // 검색 모드
            SearchTextField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearchActiveChange = onSearchActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        } else {
            // 일반 모드
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 타이틀 (🔥 텍스트 줄임)
                Column {
                    Text(
                        text = "안녕하세요! 👋",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "동네 이야기", // 🔥 "오늘의 이야기" → "동네 이야기"로 줄임
                        fontSize = 22.sp, // 🔥 24sp → 22sp로 줄임
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.OnSurface
                    )
                }

                // 검색 버튼
                IconButton(
                    onClick = { onSearchActiveChange(true) },
                    modifier = Modifier
                        .background(
                            BrandColors.Primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "검색",
                        tint = BrandColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text("게시글 검색...")
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = BrandColors.Primary
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { onSearchActiveChange(false) }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "검색 닫기",
                    tint = Color.Gray
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandColors.Primary,
            cursorColor = BrandColors.Primary
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPostCard(
    post: com.example.kotlin_amateur.remote.response.PostListResponse,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onPostClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 작성자 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔥 실제 프로필 이미지 또는 플레이스홀더
                if (!post.authorProfileImageUrl.isNullOrBlank()) {
                    // 🔥 프로필 이미지 로딩 로깅
                    android.util.Log.d("ModernPostCard", "프로필 이미지 로딩 시도: ${post.authorProfileImageUrl}")
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.authorProfileImageUrl)
                            .crossfade(true)
                            // 🔥 메모리 최적화 설정
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .size(40, 40) // 정확한 크기 지정
                            .scale(Scale.FILL)
                            .allowHardware(false) // 메모리 안정성
                            .listener(
                                onStart = {
                                    android.util.Log.d("ModernPostCard", "프로필 이미지 로딩 시작: ${post.authorNickname}")
                                },
                                onSuccess = { _, _ ->
                                    android.util.Log.d("ModernPostCard", "프로필 이미지 로딩 성공: ${post.authorNickname}")
                                },
                                onError = { _, throwable ->
                                    android.util.Log.e("ModernPostCard", "프로필 이미지 로딩 실패: ${post.authorNickname} - ${throwable.throwable?.message}")
                                }
                            )
                            .build(),
                        contentDescription = "${post.authorNickname} 프로필",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick(post.authorUserId) },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.image_error_placeholder),
                        error = painterResource(id = R.drawable.image_error_placeholder)
                    )
                } else {
                    // 프로필 이미지가 없을 때 기본 이미지
                    Image(
                        painter = painterResource(id = R.drawable.image_error_placeholder),
                        contentDescription = "기본 프로필 이미지",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick(post.authorUserId) },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.authorNickname,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = BrandColors.OnSurface
                    )
                    Text(
                        text = post.formattedTime,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 더보기 버튼
                IconButton(onClick = { /* 더보기 메뉴 */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 게시글 내용
            if (post.postTitle.isNotBlank()) {
                Text(
                    text = post.postTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrandColors.OnSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = post.displayContent,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            // 🔥 실제 이미지 표시
            if (post.hasImage && post.imageUrls.isNotEmpty()) {
                // 🔥 이미지 로딩 로깅
                android.util.Log.d("ModernPostCard", "게시글 ${post.postId} 이미지 로딩 시도: ${post.imageUrls.first()}")
                
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrls.first()) // 첫 번째 이미지 표시
                        .crossfade(true)
                        // 🔥 메모리 최적화 설정
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(600, 400) // 적절한 크기로 제한
                        .scale(Scale.FILL)
                        .allowHardware(false) // 메모리 안정성
                        .listener(
                            onStart = {
                                android.util.Log.d("ModernPostCard", "게시글 ${post.postId} 이미지 로딩 시작")
                            },
                            onSuccess = { _, _ ->
                                android.util.Log.d("ModernPostCard", "게시글 ${post.postId} 이미지 로딩 성공")
                            },
                            onError = { _, throwable ->
                                android.util.Log.e("ModernPostCard", "게시글 ${post.postId} 이미지 로딩 실패: ${throwable.throwable?.message}")
                            }
                        )
                        .build(),
                    contentDescription = "게시글 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.image_error_placeholder),
                    error = painterResource(id = R.drawable.image_error_placeholder)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좋아요, 댓글 등
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        icon = if (post.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        text = "${post.likeCount}",
                        color = if (post.isLikedByCurrentUser) Color.Red else Color.Gray,
                        onClick = onLikeClick
                    )

                    ActionButton(
                        icon = R.drawable.ic_comment, // drawable 리소스 사용
                        text = "${post.commentCount}",
                        color = Color.Gray,
                        onClick = { /* 댓글 */ }
                    )
                }

                // 공유 버튼
                IconButton(onClick = { /* 공유 */ }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "공유",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: Any, // ImageVector 또는 drawable resource ID
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        when (icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            is Int -> {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmptyStateContent(
    isSearchMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearchMode) "🔍" else "📝",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearchMode) "검색 결과가 없어요" else "아직 게시글이 없어요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = BrandColors.OnSurface
        )
        Text(
            text = if (isSearchMode) "다른 검색어를 시도해보세요" else "첫 번째 이야기를 공유해보세요!",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// 🔥 색상 다양화된 SpeedDial
@Composable
fun ModernSpeedDial(
    onAddPostClick: () -> Unit,
    onLocationPromoteClick: () -> Unit,
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
                // 🔥 글 작성 버튼 (빨간색)
                SpeedDialOption(
                    icon = Icons.Default.Edit,
                    label = "글 작성",
                    backgroundColor = Color(0xFFFF6B6B), // 빨간색
                    onClick = {
                        onAddPostClick()
                        isExpanded = false
                    }
                )

                // 🔥 지역 홍보 버튼 (초록색)
                SpeedDialOption(
                    icon = Icons.Default.LocationOn, // 홍보 아이콘
                    label = "지역 홍보",
                    backgroundColor = Color(0xFF51CF66), // 초록색
                    onClick = {
                        onLocationPromoteClick()
                        isExpanded = false
                    }
                )
            }
        }

        // 메인 플로팅 버튼
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = BrandColors.Primary,
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
fun SpeedDialOption(
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

        // 🔥 색상이 적용된 버튼
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