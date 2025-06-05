package com.example.kotlin_amateur.post
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kotlin_amateur.model.PostSummary
import com.example.kotlin_amateur.model.UserProfile

/**
 * UserProfileDialog - 프로필 클릭 기능이 추가된 다이얼로그
 *
 * 🆕 추가된 기능:
 * - 프로필 스택 정보 표시
 * - 중첩 프로필 클릭 지원
 * - 깊이 제한 시각적 표시
 */
@Composable
fun UserProfileDialog(
    userProfile: UserProfile,
    userPosts: List<PostSummary>,
    onDismiss: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    // 🆕 추가된 파라미터들
    onProfileClick: ((String) -> Unit)? = null, // 프로필 클릭 콜백 (선택사항)
    currentDepth: Int = 1, // 현재 스택 깊이
    maxDepth: Int = 5, // 최대 스택 깊이
    showStackInfo: Boolean = true // 스택 정보 표시 여부
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔥 스택 깊이 표시 (2단계 이상일 때만)
                if (showStackInfo && currentDepth > 1) {
                    Text(
                        text = "$currentDepth/$maxDepth",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${userProfile.nickname}님의 프로필",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // 🔥 깊이 경고 표시 (최대 깊이 근접 시)
                if (showStackInfo && currentDepth >= maxDepth - 1) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "최대 깊이 근접",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // 프로필 섹션
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = userProfile.profileImageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = userProfile.nickname,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "게시글 ${userPosts.size}개",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "가입일: ${userProfile.joinDate}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // 액션 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("팔로우")
                    }
                    OutlinedButton(
                        onClick = onMessageClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("메시지")
                    }
                }

                // 구분선
                HorizontalDivider(
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp
                )

                // 최근 게시글
                Text(
                    text = "최근 게시글",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userPosts.take(3)) { post ->
                        PostSummaryItem(
                            post = post,
                            onClick = { onPostClick(post.id) },
                            onProfileClick = onProfileClick, // 🆕 프로필 클릭 콜백 전달
                            canClickProfile = onProfileClick != null && currentDepth < maxDepth // 🔥 클릭 가능 여부
                        )
                    }
                }

                // 🔥 더 많은 게시글이 있다면 안내
                if (userPosts.size > 3) {
                    Text(
                        text = "외 ${userPosts.size - 3}개 게시글 더보기",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    )
}

/**
 * PostSummaryItem - 프로필 클릭 기능이 추가된 게시글 요약 아이템
 */
@Composable
fun PostSummaryItem(
    post: PostSummary,
    onClick: () -> Unit,
    onProfileClick: ((String) -> Unit)? = null, // 🆕 프로필 클릭 콜백
    canClickProfile: Boolean = true // 🆕 프로필 클릭 가능 여부
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.firstImage,
                contentDescription = "게시글 이미지",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = post.content.take(30) + if (post.content.length > 30) "..." else "",
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 🔥 작성자 정보 (클릭 가능/불가능에 따라 다르게 표시)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "작성자: ${post.authorNickname}",
                        fontSize = 11.sp,
                        color = if (canClickProfile && onProfileClick != null) {
                            Color(0xFF4285F4) // 클릭 가능하면 파란색
                        } else {
                            Color.Gray // 클릭 불가능하면 회색
                        },
                        modifier = if (canClickProfile && onProfileClick != null) {
                            Modifier.clickable {
                                onProfileClick?.invoke(post.authorUserId) // 🔥 작성자 프로필 클릭
                            }
                        } else {
                            Modifier
                        }
                    )

                    // 클릭 불가능할 때 제한 표시
                    if (!canClickProfile && onProfileClick != null) {
                        Text(
                            text = " (제한)",
                            fontSize = 10.sp,
                            color = Color.Red
                        )
                    }
                }

                Text(
                    text = "${post.likeCount}개 좋아요 • ${post.createdAt}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}