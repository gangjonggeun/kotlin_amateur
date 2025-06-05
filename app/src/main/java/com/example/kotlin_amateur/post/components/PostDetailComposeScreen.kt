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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.kotlin_amateur.model.Comment
import com.example.kotlin_amateur.model.PostDetail
import com.example.kotlin_amateur.model.Reply
import com.example.kotlin_amateur.ui.icon.CustomIcons
import com.example.kotlin_amateur.viewmodel.PostDetailViewModel

/**
 * PostDetailComposeScreen - 바텀시트 + 추천 기능 추가
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

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
        viewModel.loadComments(postId)
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

            // 2. 작성자 정보 섹션 (🔥 하드코딩 수정)
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

            // 6. 댓글 헤더 (🔥 클릭으로 열고닫기 추가)
            item(key = "comment_header") {
                CommentHeaderClickable(
                    commentCount = comments.size,
                    isExpanded = isCommentSectionExpanded,
                    onToggleExpansion = { isCommentSectionExpanded = !isCommentSectionExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // 7. 댓글 리스트 (확장된 경우만 표시, 바텀시트 답글)
            if (isCommentSectionExpanded) {
                items(
                    items = comments,
                    key = { comment -> comment.id }
                ) { comment ->
                    CommentItemWithBottomSheet(
                        comment = comment,
                        onReplyClick = {
                            // 🔥 바텀시트로 답글 입력
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

            // 8. 🆕 게시글 추천 섹션
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

        // 🔥 답글 작성 바텀시트
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
 * 🔥 하드코딩 수정된 AuthorInfoSection
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
                // 🔥 하드코딩 제거 - PostDetail에 authorUserId 필드 사용
                post.authorUserId?.let { userId ->
                    onProfileClick(userId)
                } ?: run {
                    println("⚠️ [AuthorInfo] authorUserId가 없어서 프로필 클릭 불가")
                }
            }
    ) {
        AsyncImage(
            model = post.authorProfileImage,
            contentDescription = "${post.authorNickname} 프로필 이미지",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
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

// 🔥 기존 컴포넌트들 (사용하지 않지만 호환성을 위해 유지)
@Composable
fun CommentHeader(
    commentCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
}

@Composable
fun CommentItem(
    comment: Comment,
    onReplyClick: () -> Unit,
    onReplySubmit: (String) -> Unit,
    onToggleReplies: () -> Unit,
    onProfileClick: (String) -> Unit = {}
) {
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 메인 댓글
        CommentContent(
            profileImage = comment.authorProfileImage,
            nickname = comment.authorNickname,
            content = comment.content,
            timestamp = comment.createdAt,
            onReplyClick = onReplyClick,
            onProfileClick = {
                onProfileClick("1") // 임시 하드코딩
            }
        )

        // 답글 입력창
        AnimatedVisibility(
            visible = comment.isReplyInputVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            ReplyInputSection(
                value = replyText,
                onValueChange = { replyText = it },
                onSubmit = {
                    if (replyText.isNotBlank()) {
                        onReplySubmit(replyText)
                        replyText = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp, start = 52.dp)
            )
        }

        // 답글 더보기/접기 버튼
        if (comment.replyCount > 0) {
            TextButton(
                onClick = onToggleReplies,
                modifier = Modifier.padding(start = 52.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = if (comment.isRepliesVisible)
                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF666666)
                )
                Text(
                    text = if (comment.isRepliesVisible)
                        "답글 접기" else "답글 ${comment.replyCount}개 보기",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // 답글 리스트
        AnimatedVisibility(
            visible = comment.isRepliesVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp)
            ) {
                comment.replies.forEach { reply ->
                    ReplyItem(
                        reply = reply,
                        onProfileClick = {
                            onProfileClick("1") // 임시 하드코딩
                        }
                    )
                }
            }
        }

        // 구분선
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}

@Composable
fun CommentContent(
    profileImage: String?,
    nickname: String,
    content: String,
    timestamp: String,
    onReplyClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 프로필 이미지
        AsyncImage(
            model = profileImage,
            contentDescription = "$nickname 프로필 이미지",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            // 닉네임 + 시간
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

            // 댓글 내용
            Text(
                text = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                fontSize = 16.sp,
                color = Color(0xFF444444),
                lineHeight = 22.sp
            )

            // 답글 버튼
            TextButton(
                onClick = onReplyClick,
                modifier = Modifier.padding(top = 4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "답글 달기",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun ReplyInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "답글을 입력하세요...",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                textStyle = TextStyle(fontSize = 14.sp),
                maxLines = 3
            )

            IconButton(
                onClick = onSubmit,
                enabled = value.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "답글 전송",
                    tint = if (value.isNotBlank())
                        Color(0xFF4285F4) else Color(0xFFCCCCCC)
                )
            }
        }
    }
}

@Composable
fun ReplyItem(
    reply: Reply,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // 답글 표시 아이콘
        Icon(
            imageVector = CustomIcons.Reply,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
            tint = Color(0xFF999999)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            // 닉네임 + 시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

            // 답글 내용
            Text(
                text = reply.content,
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 15.sp,
                color = Color(0xFF444444),
                lineHeight = 20.sp
            )
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

        // 확장/축소 아이콘
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "댓글 접기" else "댓글 펼치기",
            tint = Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 🔥 바텀시트 답글 입력 방식의 CommentItem
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
        // 메인 댓글 (하드코딩 수정)
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

        // 🔥 답글 리스트 (L모양 구분선과 시작선 15dp)
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

        // 댓글 하단 구분선
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
            // 프로필 이미지 (클릭 가능)
            AsyncImage(
                model = profileImage,
                contentDescription = "$nickname 프로필 이미지",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                // 닉네임 + 시간
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

                // 댓글 내용
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

        // 🔥 답글 버튼들 (같은 평행선에 배치)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp), // 프로필 이미지 + 간격과 맞춤
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 답글 더보기/접기 버튼
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
                Spacer(modifier = Modifier.width(1.dp)) // 빈 공간 유지
            }

            // 오른쪽: 답글 달기 버튼
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
        // 🔥 L모양 구분선 (얇고 가까운 버전)
        Box(
            modifier = Modifier
                .width(40.dp) // 너비 줄임 (52dp → 40dp)
                .height(36.dp) // 고정 높이
        ) {
            // 세로선 (더 얇게, 답글에 가까이)
            Box(
                modifier = Modifier
                    .width(1.dp) // 2dp → 1dp로 더 얇게
                    .height(16.dp) // 고정 길이
                    .offset(x = 5.dp) // 10dp → 8dp로 답글에 더 가까이
                    .background(
                        Color(0xFFDDDDDD),
                        RoundedCornerShape(0.5.dp)
                    )
            )

            // 가로선 (L모양 완성)
            Box(
                modifier = Modifier
                    .width(14.dp) // 18dp → 14dp로 줄임
                    .height(1.dp) // 2dp → 1dp로 더 얇게
                    .offset(x = 5.dp, y = 16.dp) // 세로선과 연결, x도 맞춤
                    .background(
                        Color(0xFFDDDDDD),
                        RoundedCornerShape(0.5.dp)
                    )
            )
        }

        // 답글 내용
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp) // 8dp → 4dp로 줄여서 더 가까이
        ) {
            // 닉네임 + 시간
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

            // 답글 내용
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
 * 🆕 답글 작성 바텀시트 컨텐츠
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
        // 바텀시트 핸들
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 헤더
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

        // 원본 댓글 미리보기
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
                        AsyncImage(
                            model = comment.authorProfileImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
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

        // 답글 입력창
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

        // 전송 버튼
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
 * 🆕 게시글 추천 섹션
 */
@Composable
fun RecommendedPostsSection(
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 섹션 헤더
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

        // TODO: 실제 추천 로직 구현 예정
        val dummyRecommendedPosts = listOf(
            DummyPost("1", "맛집 추천해요!", "https://picsum.photos/400/300?random=1"),
            DummyPost("2", "오늘 날씨 너무 좋네요", "https://picsum.photos/400/300?random=2"),
            DummyPost("3", "새로운 카페 발견!", "https://picsum.photos/400/300?random=3")
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
 * 🆕 추천 게시글 아이템
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
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
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

// 🆕 임시 데이터 클래스
data class DummyPost(
    val id: String,
    val title: String,
    val imageUrl: String
)

// 기존 컴포넌트들은 그대로 유지
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
                AsyncImage(
                    model = images[page],
                    contentDescription = "게시글 이미지 ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
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