package com.example.kotlin_amateur.post.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.kotlin_amateur.model.Comment
import com.example.kotlin_amateur.model.PostDetail
import com.example.kotlin_amateur.model.Reply
import com.example.kotlin_amateur.ui.icon.CustomIcons
import com.example.kotlin_amateur.viewmodel.PostDetailViewModel

/**
 * 🔥 메모리 최적화된 AsyncImage 컴포넌트
 */
@Composable
fun OptimizedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .size(size.value.toInt()) // 🔥 크기 제한 필수!
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 🔥 큰 이미지용 최적화된 AsyncImage
 */
@Composable
fun OptimizedAsyncImageLarge(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxWidth: Int = 800,
    maxHeight: Int = 600,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .size(maxWidth, maxHeight) // 🔥 최대 크기 제한
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * PostDetailComposeScreen - 메모리 최적화 버전
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailComposeScreen(
    postId: String,
    viewModel: PostDetailViewModel,
    onBackPressed: () -> Unit,
    onShowToast: (String) -> Unit,
    onProfileClick: (String) -> Unit = {}
) {

    // ViewModel에서 상태 수집
    val postDetail by viewModel.postDetail.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 🆕 바텀시트 상태 관리
    val replyBottomSheetState = rememberModalBottomSheetState()
    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyTargetComment by remember { mutableStateOf<Comment?>(null) }

    // 🆕 댓글 섹션 확장/축소 상태
    var isCommentSectionExpanded by remember { mutableStateOf(true) }

    // 🔥 메모리 최적화: 한 번만 로드되도록 제어
    LaunchedEffect(postId) {
        if (viewModel.postDetail.value?.id != postId) {
            viewModel.loadPostDetail(postId)
            viewModel.loadComments(postId)
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            onShowToast(errorMessage)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            // 1. 이미지 섹션
            item(key = "images") {
                ImageSection(
                    images = postDetail?.images ?: emptyList(),
                    onBackClick = onBackPressed,
                    onLikeClick = { viewModel.toggleLike() },
                    isLiked = postDetail?.isLiked ?: false
                )
            }

            // 2. 작성자 정보 섹션
            item(key = "author_info") {
                AuthorInfoSection(
                    post = postDetail,
                    modifier = Modifier.padding(16.dp),
                    onProfileClick = { authorUserId ->
                        onProfileClick(authorUserId)
                    }
                )
            }

            // 3. 구분선
            item(key = "divider_1") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEEEEEE),
                    thickness = 3.dp
                )
            }

            // 4. 게시글 제목 + 내용 섹션
            item(key = "post_content") {
                PostContentSection(
                    post = postDetail,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 5. 구분선
            item(key = "divider_2") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEEEEEE),
                    thickness = 3.dp
                )
            }

            // 6. 댓글 헤더
            item(key = "comment_header") {
                CommentHeaderClickable(
                    commentCount = comments.size,
                    isExpanded = isCommentSectionExpanded,
                    onToggleExpansion = { isCommentSectionExpanded = !isCommentSectionExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // 7. 댓글 리스트 (확장된 경우만 표시)
            if (isCommentSectionExpanded) {
                items(
                    items = comments,
                    key = { comment -> comment.id }
                ) { comment ->
                    CommentItemWithBottomSheet(
                        comment = comment,
                        onReplyClick = {
                            replyTargetComment = comment
                            showReplyBottomSheet = true
                        },
                        onToggleReplies = { viewModel.toggleRepliesVisibility(comment.id) },
                        onProfileClick = { commentAuthorUserId ->
                            onProfileClick(commentAuthorUserId)
                        }
                    )
                }
            }

            // 8. 게시글 추천 섹션
            item(key = "recommended_posts") {
                RecommendedPostsSection(
                    onPostClick = { recommendedPostId ->
                        onShowToast("추천 게시글 기능 구현 예정: $recommendedPostId")
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        CommentInputBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onSubmit = { content ->
                viewModel.submitComment(content)
                onShowToast("댓글이 작성되었습니다")
            }
        )

        // 답글 작성 바텀시트
        if (showReplyBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showReplyBottomSheet = false
                    replyTargetComment = null
                },
                sheetState = replyBottomSheetState
            ) {
                ReplyBottomSheetContent(
                    targetComment = replyTargetComment,
                    onSubmitReply = { content ->
                        replyTargetComment?.let { comment ->
                            viewModel.submitReply(comment.id, content)
                            showReplyBottomSheet = false
                            replyTargetComment = null
                            onShowToast("답글이 작성되었습니다")
                        }
                    },
                    onCancel = {
                        showReplyBottomSheet = false
                        replyTargetComment = null
                    }
                )
            }
        }
    }
}

/**
 * 🔥 메모리 최적화된 AuthorInfoSection
 */
@Composable
fun AuthorInfoSection(
    post: PostDetail?,
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit = {}
) {
    if (post == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                post.authorUserId?.let { userId ->
                    onProfileClick(userId)
                } ?: run {
                    println("⚠️ [AuthorInfo] authorUserId가 없어서 프로필 클릭 불가")
                }
            }
    ) {
        OptimizedAsyncImage(
            model = post.authorProfileImage,
            contentDescription = "${post.authorNickname} 프로필 이미지",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.authorNickname,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "프로필 보기",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF666666)
                )
            }
            Text(
                text = post.createdAt,
                fontSize = 13.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
fun CommentInputBar(
    modifier: Modifier = Modifier,
    onSubmit: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "댓글을 입력하세요...",
                        fontSize = 15.sp,
                        color = Color(0xFF999999)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFFDDDDDD),
                    focusedContainerColor = Color(0xFFFAFAFA),
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(24.dp),
                textStyle = TextStyle(fontSize = 15.sp),
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSubmit(commentText)
                        commentText = ""
                    }
                },
                enabled = commentText.isNotBlank(),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "댓글 전송",
                    tint = if (commentText.isNotBlank())
                        Color(0xFF4285F4) else Color(0xFFCCCCCC)
                )
            }
        }
    }
}

/**
 * 🔥 클릭으로 열고닫기 가능한 CommentHeader
 */
@Composable
fun CommentHeaderClickable(
    commentCount: Int,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpansion() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "댓글",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$commentCount",
                fontSize = 15.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "댓글 접기" else "댓글 펼치기",
            tint = Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 🔥 메모리 최적화된 CommentItemWithBottomSheet
 */
@Composable
fun CommentItemWithBottomSheet(
    comment: Comment,
    onReplyClick: () -> Unit,
    onToggleReplies: () -> Unit,
    onProfileClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CommentContentImproved(
            profileImage = comment.authorProfileImage,
            nickname = comment.authorNickname,
            content = comment.content,
            timestamp = comment.createdAt,
            hasReplies = comment.replyCount > 0,
            replyCount = comment.replyCount,
            isRepliesVisible = comment.isRepliesVisible,
            onReplyClick = onReplyClick,
            onToggleReplies = onToggleReplies,
            onProfileClick = {
                comment.authorUserId?.let { userId ->
                    onProfileClick(userId)
                } ?: run {
                    println("⚠️ [CommentItem] authorUserId가 없어서 프로필 클릭 불가")
                }
            }
        )

        // 답글 리스트
        AnimatedVisibility(
            visible = comment.isRepliesVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                comment.replies.forEachIndexed { index, reply ->
                    ReplyItemWithLShape(
                        reply = reply,
                        isLastReply = index == comment.replies.size - 1,
                        onProfileClick = {
                            reply.authorUserId?.let { userId ->
                                onProfileClick(userId)
                            } ?: run {
                                println("⚠️ [ReplyItem] authorUserId가 없어서 프로필 클릭 불가")
                            }
                        }
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}

@Composable
fun CommentContentImproved(
    profileImage: String?,
    nickname: String,
    content: String,
    timestamp: String,
    hasReplies: Boolean,
    replyCount: Int,
    isRepliesVisible: Boolean,
    onReplyClick: () -> Unit,
    onToggleReplies: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 🔥 메모리 최적화된 프로필 이미지
            OptimizedAsyncImage(
                model = profileImage,
                contentDescription = "$nickname 프로필 이미지",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                size = 40.dp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nickname,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.clickable { onProfileClick() }
                    )
                    Text(
                        text = timestamp,
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }

                Text(
                    text = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    fontSize = 16.sp,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasReplies) {
                TextButton(
                    onClick = onToggleReplies,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isRepliesVisible)
                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF666666)
                    )
                    Text(
                        text = if (isRepliesVisible)
                            "답글 접기" else "답글 ${replyCount}개 보기",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            TextButton(
                onClick = onReplyClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "\uD83D\uDCAC 답글 달기",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ReplyItemWithLShape(
    reply: Reply,
    isLastReply: Boolean,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .offset(x = 5.dp)
                    .background(
                        Color(0xFFDDDDDD),
                        RoundedCornerShape(0.5.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(1.dp)
                    .offset(x = 5.dp, y = 16.dp)
                    .background(
                        Color(0xFFDDDDDD),
                        RoundedCornerShape(0.5.dp)
                    )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reply.authorNickname,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.clickable { onProfileClick() }
                )
                Text(
                    text = reply.createdAt,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            Text(
                text = reply.content,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                fontSize = 15.sp,
                color = Color(0xFF444444),
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 🔥 메모리 최적화된 답글 작성 바텀시트
 */
@Composable
fun ReplyBottomSheetContent(
    targetComment: Comment?,
    onSubmitReply: (String) -> Unit,
    onCancel: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${targetComment?.authorNickname}님에게 답글",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onCancel) {
                Text("취소")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        targetComment?.let { comment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OptimizedAsyncImage(
                            model = comment.authorProfileImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            size = 24.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comment.authorNickname,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = comment.content.take(100) + if (comment.content.length > 100) "..." else "",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp),
            placeholder = {
                Text("답글을 입력하세요...")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4285F4),
                unfocusedBorderColor = Color(0xFFDDDDDD)
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (replyText.isNotBlank()) {
                    onSubmitReply(replyText.trim())
                    replyText = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = replyText.isNotBlank()
        ) {
            Text("답글 작성")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 🔥 메모리 최적화된 게시글 추천 섹션
 */
@Composable
fun RecommendedPostsSection(
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "추천 게시글",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            TextButton(onClick = { /* TODO: 더보기 페이지 */ }) {
                Text("더보기")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val dummyRecommendedPosts = listOf(
            DummyPost("1", "맛집 추천해요!", "https://picsum.photos/300/240?random=1"),
            DummyPost("2", "오늘 날씨 너무 좋네요", "https://picsum.photos/300/240?random=2"),
            DummyPost("3", "새로운 카페 발견!", "https://picsum.photos/300/240?random=3")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyRecommendedPosts) { post ->
                RecommendedPostItem(
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
            }
        }
    }
}

/**
 * 🔥 메모리 최적화된 추천 게시글 아이템
 */
@Composable
fun RecommendedPostItem(
    post: DummyPost,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            OptimizedAsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                size = 300.dp, // 작은 크기로 제한
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = post.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "// TODO: 구현 예정",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

data class DummyPost(
    val id: String,
    val title: String,
    val imageUrl: String
)

/**
 * 🔥 메모리 최적화된 ImageSection
 */
/**
 * 🔥 메모리 최적화된 ImageSection
 */
@Composable
fun ImageSection(
    images: List<String>,
    onBackClick: () -> Unit,
    onLikeClick: () -> Unit,
    isLiked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        if (images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { images.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                OptimizedAsyncImageLarge(
                    model = images[page],
                    contentDescription = "게시글 이미지 ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    maxWidth = 800,
                    maxHeight = 600,
                    contentScale = ContentScale.Crop
                )
            }

            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (pagerState.currentPage == index)
                                        Color.White else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
                    tint = if (isLiked) Color.Red else Color.White
                )
            }
        }
    }
}

@Composable
fun PostContentSection(
    post: PostDetail?,
    modifier: Modifier = Modifier
) {
    if (post == null) return

    Column(modifier = modifier) {
        // 게시글 제목
        Text(
            text = post.title ?: "제목 없음",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222),
            lineHeight = 28.sp
        )

        // 게시글 내용
        if (post.content.isNotBlank()) {
            Text(
                text = post.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                fontSize = 19.sp,
                color = Color(0xFF333333),
                lineHeight = 26.sp
            )
        }

        // 좋아요 수
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${post.likeCount}",
                modifier = Modifier.padding(start = 6.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
        }
    }
}