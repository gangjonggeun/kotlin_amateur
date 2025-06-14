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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.kotlin_amateur.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Scale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.remote.response.PostListResponse

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
    // 🔥 새로운 Paging3 StateFlow 상태 수집
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val postsPagingItems = viewModel.postsPagingFlow.collectAsLazyPagingItems()

    // UI 상태
    var isSearchActive by remember { mutableStateOf(false) }
    var showSpeedDial by remember { mutableStateOf(true) }

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
            // 🎯 모던한 상단 바 (기존 UI 유지)
            ModernTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            // 📱 메인 콘텐츠 - Paging3으로 업그레이드
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔥 Paging3 아이템들
                    items(
                        count = postsPagingItems.itemCount,
                        key = { index ->
                            postsPagingItems[index]?.postId ?: "loading_$index"
                        }
                    ) { index ->
                        val post = postsPagingItems[index]
                        
                        if (post != null) {
                            ModernPostCard(
                                post = post,
                                onPostClick = {
                                    onNavigateToPostDetail(post.postId, post.postTitle)
                                },
                                onLikeClick = {
                                    viewModel.toggleLike(post.postId, !post.isLikedByCurrentUser) { success ->
                                        if (!success) {
                                            android.util.Log.e("ModernHomeScreen", "좋아요 실패")
                                        }
                                    }
                                },
                                onProfileClick = { userId ->
                                    println("프로필 클릭: $userId")
                                }
                            )
                        } else {
                            // 로딩 아이템
                            ModernPostCardSkeleton()
                        }
                    }

                    // 🔄 로딩 상태 처리
                    when {
                        postsPagingItems.loadState.refresh is LoadState.Loading -> {
                            if (postsPagingItems.itemCount == 0) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = BrandColors.Primary)
                                    }
                                }
                            }
                        }
                        
                        postsPagingItems.loadState.append is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = BrandColors.Primary
                                    )
                                }
                            }
                        }
                        
                        postsPagingItems.loadState.refresh is LoadState.Error -> {
                            item {
                                ModernErrorItem(
                                    message = "게시글을 불러올 수 없어요",
                                    onRetryClick = { postsPagingItems.retry() }
                                )
                            }
                        }
                        
                        postsPagingItems.loadState.refresh is LoadState.NotLoading && postsPagingItems.itemCount == 0 -> {
                            item {
                                EmptyStateContent(
                                    isSearchMode = isSearchActive && searchQuery.isNotBlank(),
                                    modifier = Modifier.fillParentMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🚀 개선된 플로팅 액션 버튼들 (기존 유지)
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
                    println("지역 홍보 기능")
                },
                modifier = Modifier.padding(16.dp)
            )
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
                // 타이틀
                Column {
                    Text(
                        text = "안녕하세요! 👋",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "동네 이야기",
                        fontSize = 22.sp,
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
                onClick = { 
                    onQueryChange("")
                    onSearchActiveChange(false) 
                }
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
    post: PostListResponse,
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
                // 프로필 이미지 또는 플레이스홀더
                if (!post.authorProfileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.authorProfileImageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .size(40, 40)
                            .scale(Scale.FILL)
                            .allowHardware(false)
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
                text = post.postContent.take(150), // 150자로 제한
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            // 이미지 표시
            if (post.hasImage && !post.imageUrls.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrls) // String 타입
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(600, 400)
                        .scale(Scale.FILL)
                        .allowHardware(false)
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
                        icon = R.drawable.ic_comment,
                        text = "${post.commentCount}",
                        color = Color.Gray,
                        onClick = { /* 댓글 */ }
                    )
                }

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

// 💀 로딩 스켈레톤
@Composable
fun ModernPostCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 작성자 정보 스켈레톤
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 제목 스켈레톤
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 내용 스켈레톤
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            )
        }
    }
}

// ❌ 에러 아이템
@Composable
fun ModernErrorItem(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = BrandColors.OnSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Primary
                )
            ) {
                Text("다시 시도")
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

// 🔥 색상 다양화된 SpeedDial (기존 유지)
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
                // 글 작성 버튼
                SpeedDialOption(
                    icon = Icons.Default.Edit,
                    label = "글 작성",
                    backgroundColor = Color(0xFFFF6B6B),
                    onClick = {
                        onAddPostClick()
                        isExpanded = false
                    }
                )

                // 지역 홍보 버튼
                SpeedDialOption(
                    icon = Icons.Default.LocationOn,
                    label = "지역 홍보",
                    backgroundColor = Color(0xFF51CF66),
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

        // 색상이 적용된 버튼
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