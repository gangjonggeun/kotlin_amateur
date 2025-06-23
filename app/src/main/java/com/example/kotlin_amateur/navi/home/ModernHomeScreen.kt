package com.example.kotlin_amateur.navi.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.viewmodel.PostListViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Scale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.viewmodel.HomeViewModel

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
    postListType: PostListType = PostListType.HOME, // 🎯 타입 매개변수 추가
    onBackClick: (() -> Unit)? = null, // 🔙 뒤로가기 콜백 추가
    homeViewModel: PostListViewModel? = null, // 🏠 홈용 ViewModel (선택적)
    profileViewModel: com.example.kotlin_amateur.viewmodel.ProfilePostViewModel? = null // 📝 프로필용 ViewModel (선택적)
) {
    // 🎯 PostListType에 따라 적절한 ViewModel 선택
    val isProfileType = postListType in listOf(
        PostListType.MY_POSTS, 
        PostListType.LIKED_POSTS, 
        PostListType.RECENT_VIEWED
    )
    
    // ViewModel 자동 선택 및 생성 - 우선순위: 전달받은 ViewModel > 새로 생성
    val currentHomeViewModel: PostListViewModel = homeViewModel ?: hiltViewModel()
    val currentProfileViewModel: com.example.kotlin_amateur.viewmodel.ProfilePostViewModel? = 
        when {
            // 1. ProfileViewModel이 명시적으로 전달된 경우 우선 사용
            profileViewModel != null -> {
                android.util.Log.d("ModernHomeScreen", "✅ ProfileViewModel 전달받음: $profileViewModel")
                profileViewModel
            }
            // 2. Profile 타입이지만 전달받지 못한 경우 새로 생성
            isProfileType -> {
                android.util.Log.d("ModernHomeScreen", "🔄 ProfileViewModel 새로 생성")
                hiltViewModel()
            }
            // 3. Profile 타입이 아닌 경우 null
            else -> {
                android.util.Log.d("ModernHomeScreen", "🏠 HomeViewModel 사용 (Profile 타입 아님)")
                null
            }
        }
    
    // 🎯 HomeViewModel도 필요 (검색 기능용)
    val homeViewModelForSearch: HomeViewModel = hiltViewModel()
    // 🔥 타입 설정
    LaunchedEffect(postListType) {
        if (isProfileType) {
            currentProfileViewModel?.setPostListType(postListType)
        } else {
            currentHomeViewModel.setPostListType(postListType)
        }
    }

    // 🔥 새로운 Paging3 StateFlow 상태 수집 (타입별 ViewModel 분기)
    val searchQuery by if (isProfileType) {
        // 프로필 타입에서는 검색 비활성화
        remember { mutableStateOf("") }
    } else {
        homeViewModelForSearch.searchQuery.collectAsStateWithLifecycle(initialValue = "")
    }



    val postsPagingItems = if (isProfileType) {
        // 📝 프로필 ViewModel 사용
        currentProfileViewModel!!.profilePostsPagingFlow.collectAsLazyPagingItems()
    } else {
        // 🏠 홈 ViewModel 사용
        currentHomeViewModel.postsPagingFlow.collectAsLazyPagingItems()
    }
    
    val currentPostListType by if (isProfileType) {
        currentProfileViewModel!!.postListType.collectAsStateWithLifecycle()
    } else {
        currentHomeViewModel.postListType.collectAsStateWithLifecycle()
    }

    // UI 상태
    var isSearchActive by remember { mutableStateOf(false) }
    var showSpeedDial by remember { mutableStateOf(currentPostListType == PostListType.HOME) } // 홈에서만 표시

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = PostListType.getGradientColors(currentPostListType) // 🎯 타입별 배경색
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🎯 모던한 상단 바 (타입별 타이틀)
            ModernTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = homeViewModelForSearch::updateSearchQuery,
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                postListType = currentPostListType, // 🎯 타입 전달
                onBackClick = onBackClick, // 🔙 뒤로가기 전달
                viewModel = homeViewModelForSearch,
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
                            postsPagingItems.peek(index)?.postId ?: "item_$index"
                        }
                    ) { index ->
                        val post = postsPagingItems[index]
                        
                        if (post != null) {
                            ModernPostCard(
                                post = post,
                                onPostClick = {
                                    android.util.Log.d("ModernHomeScreen", "🎯 게시글 클릭: postId=${post.postId}, title=${post.postTitle}")
                                    onNavigateToPostDetail(post.postId, post.postTitle)
                                },
                                onLikeClick = {
                                    // 🔥 PostDetailRepository를 사용한 간단한 토글
                                    if (isProfileType) {
                                        // 프로필에서는 좋아요 기능 비활성화 또는 별도 처리
                                        android.util.Log.d("ModernHomeScreen", "프로필에서 좋아요 클릭: ${post.postId}")
                                    } else {
                                        // 🎨 간단한 토글 방식
                                        currentHomeViewModel.toggleLike(post.postId) { success ->
                                            if (!success) {
                                                android.util.Log.e("ModernHomeScreen", "좋아요 실패: ${post.postId}")
                                            } else {
                                                android.util.Log.d("ModernHomeScreen", "좋아요 성공: ${post.postId}")
                                                // 성공 시 페이지 새로고침으로 UI 업데이트
                                                postsPagingItems.refresh()
                                            }
                                        }
                                    }
                                },
                                onProfileClick = { userId ->
                                    println("프로필 클릭: $userId")
                                },
                                postListType = currentPostListType // 🎯 타입 전달
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
                    println("가게 홍보 기능")
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
    postListType: PostListType, // 🎯 타입 추가
    onBackClick: (() -> Unit)? = null, // 🔙 뒤로가기 콜백 추가
    viewModel: HomeViewModel, // ✅ 추가
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
                viewModel = viewModel, // ✅ 추가
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 🔙 뒤로가기 버튼 (내 게시글/좋아요한 글/최근 본 글에서만 표시)
                    if (onBackClick != null && postListType != PostListType.HOME) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .background(
                                    BrandColors.Primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = BrandColors.Primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    // 타이틀
                    Column {
                        // 🎯 타입별 인사말과 타이틀 표시
                        if (postListType == PostListType.HOME) {
                            Text(
                                text = "안녕하세요! 👋",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = postListType.displayName, // 🎯 타입별 타이틀
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.OnSurface
                        )
                    }
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
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    
    Column {
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
                // ✅ X 버튼 하나만 (우측)
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "검색어 지우기",
                            tint = Color.Gray
                        )
                    }
                } else {
                    // 빈 공간일 때는 닫기 버튼
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
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary,
                cursorColor = BrandColors.Primary
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            // 🎯 키보드 액션 추가 (엔터키로 검색)
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { 
                    viewModel.performManualSearch() // 🎯 엔터키로 명시적 검색
                }
            )
        )
        
        // ✅ 최근 검색어 표시
        if (recentSearches.isNotEmpty() && query.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "최근 검색어",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandColors.OnSurface
                        )
                        
                        TextButton(
                            onClick = { viewModel.clearAllSearchHistory() }
                        ) {
                            Text(
                                text = "전체 삭제",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    recentSearches.forEach { searchHistory ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onRecentSearchClick(searchHistory)
                                    onSearchActiveChange(false)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = searchHistory.query,
                                fontSize = 14.sp,
                                color = BrandColors.OnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { 
                                    viewModel.deleteSearchHistory(searchHistory.query)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "삭제",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPostCard(
    post: PostListResponse,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit, // 🔄 단순하게 변경
    onProfileClick: (String) -> Unit,
    postListType: PostListType = PostListType.HOME, // 🎯 타입 추가
    modifier: Modifier = Modifier
) {
    // 🔄 더보기 메뉴 상태 관리
    var showMoreMenu by remember { mutableStateOf(false) }
    
    // ✅ Context를 Composable 영역에서 미리 가져오기
    val context = LocalContext.current
    
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
                            .crossfade(200) // 빠른 전환
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .size(120, 120) // 프로필 이미지 최적화 (3배 해상도)
                            .scale(Scale.FILL)
                            .allowHardware(true) // 하드웨어 가속 활성화
                            .allowRgb565(true) // ✅ 작은 이미지 메모리 절약
                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565) // ✅ 프로필은 품질 낮춰도 OK
                            .transformations(
                                // ✅ 원형 이미진 미리 처리
                                coil.transform.CircleCropTransformation()
                            )
                            .build(),
                        contentDescription = "${post.authorNickname} 프로필",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick(post.authorUserId) },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.image_loading_placeholder), // 로딩 전용
                        error = painterResource(id = R.drawable.image_error_placeholder) // 에러 전용
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

                // 🎯 타입별 더보기 버튼
                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = Color.Gray
                        )
                    }
                    
                    // 🔄 타입별 드롭다운 메뉴
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        when (postListType) {
                            PostListType.MY_POSTS -> {
                                // 📝 내 게시글: 수정하기, 삭제하기
                                DropdownMenuItem(
                                    text = { Text("수정하기") },
                                    onClick = {
                                        showMoreMenu = false
                                        // TODO: 수정 기능 구현
                                        android.widget.Toast.makeText(
                                            context, // ✅ 미리 가져온 context 사용
                                            "수정 기능은 추후 구현 예정입니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.Blue
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("삭제하기") },
                                    onClick = {
                                        showMoreMenu = false
                                        // TODO: 삭제 기능 구현
                                        android.widget.Toast.makeText(
                                            context, // ✅ 미리 가져온 context 사용
                                            "삭제 기능은 추후 구현 예정입니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.Red
                                        )
                                    }
                                )
                            }
                            
                            else -> {
                                // 🏠 홈, 좋아요한 글, 최근 본 글: 신고하기, 북마크 저장
                                DropdownMenuItem(
                                    text = { Text("신고하기") },
                                    onClick = {
                                        showMoreMenu = false
                                        // TODO: 신고 기능 구현
                                        android.widget.Toast.makeText(
                                            context, // ✅ 미리 가져온 context 사용
                                            "신고 기능은 추후 구현 예정입니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.Red
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("북마크 저장") },
                                    onClick = {
                                        showMoreMenu = false
                                        // TODO: 북마크 기능 구현
                                        android.widget.Toast.makeText(
                                            context, // ✅ 미리 가져온 context 사용
                                            "북마크 기능은 추후 구현 예정입니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = Color.Cyan
                                        )
                                    }
                                )
                            }
                        }
                    }
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
                            .crossfade(300) // 부드러운 전환
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .size(800, 600) // 원본 크기 유지 (압축용)
                            .scale(Scale.FILL) // 전체 채우기
                            .allowHardware(true) // 하드웨어 가속 활성화
                            .allowRgb565(true) // ✅ 메모리 절약 (16비트 대신 32비트)
                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565) // ✅ 색상 품질 자동 조절
                            .transformations(
                                // ✅ 서버 전송 전 압축 (메모리 절약)
                                coil.transform.RoundedCornersTransformation(12.dp.value)
                            )
                            .build(),
                        contentDescription = "게시글 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.image_loading_placeholder), // 로딩 전용
                        error = painterResource(id = R.drawable.image_error_placeholder) // 에러 전용
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

                // 가게 홍보 버튼
                SpeedDialOption(
                    icon = Icons.Default.LocationOn,
                    label = "가게 홍보",
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
                containerColor = Color.White.copy(alpha = 0.95f) // ✅ 밝은 희색 배경
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 그림자 추가
        ) {
            Text(
                text = label,
                color = Color.Black, // ✅ 검은색 텍스트
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium, // 좀 더 두껋게
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