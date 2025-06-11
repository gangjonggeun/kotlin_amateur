package com.example.kotlin_amateur.navi.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.viewmodel.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun RightSheetMenu(
    onEditProfileClick: () -> Unit,
    onEditNicknameClick: () -> Unit,
    onMyPostsClick: () -> Unit,
    onLikedPostsClick: () -> Unit,
    onMyCommentsClick: () -> Unit,
    onRecentViewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val context = LocalContext.current

    // ✅ StateFlow로 변경된 부분 - collectAsState() 사용
    val userInfo by profileViewModel.userInfo.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    // ✅ 에러 처리 개선
    LaunchedEffect(Unit) {
        profileViewModel.fetchMyProfile(context)
    }
    // ✅ DisposableEffect로 메모리 누수 방지
    DisposableEffect(Unit) {
        profileViewModel.fetchMyProfile(context)

        onDispose {
            // 필요시 리소스 정리
            profileViewModel.clearErrorMessage()
        }
    }
    // ✅ 에러 표시 (옵션)
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.e("RightSheetMenu", "❌ 프로필 로드 에러: $error")
            // 필요시 Toast 표시나 스낵바 처리 가능
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(screenWidth * 0.7f)
                .align(Alignment.CenterEnd),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {

                // ✅ 로딩 중 표시
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // ✅ 프로필 이미지 - null 안전성 개선
                    userInfo?.profileImageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "프로필 이미지",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_default_profile),
                            error = painterResource(id = R.drawable.ic_default_profile),
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Red, CircleShape)
                                .clickable { onEditProfileClick() }
                                .align(Alignment.CenterHorizontally),
                            onSuccess = {
                                Log.d("AsyncImage", "✅ 이미지 로드 성공: $imageUrl")
                            },
                            onError = { error ->
                                Log.e("AsyncImage", "❌ 이미지 로드 실패: $imageUrl", error.result.throwable)
                            },
                            onLoading = {
                                Log.d("AsyncImage", "⏳ 이미지 로딩 중: $imageUrl")
                            }
                        )
                    } ?: run {
                        // ✅ 기본 이미지 처리 개선
                        Image(
                            painter = painterResource(id = R.drawable.ic_default_profile),
                            contentDescription = "기본 프로필 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { onEditProfileClick() }
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                // ✅ 닉네임 - null 안전성 개선
                Text(
                    text = if (isLoading) "로딩 중..." else (userInfo?.nickname ?: "닉네임 없음"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isLoading) Color.Gray else Color.Black,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable(enabled = !isLoading) { onEditNicknameClick() }
                        .align(Alignment.CenterHorizontally)
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ✅ 메뉴 항목 - 로딩 중 비활성화
                Column {
                    MenuItem(
                        text = "내 게시글",
                        iconResId = R.drawable.ic_post,
                        enabled = !isLoading,
                        onClick = onMyPostsClick
                    )
                    MenuItem(
                        text = "좋아요한 글",
                        iconResId = R.drawable.ic_like,
                        enabled = !isLoading,
                        onClick = onLikedPostsClick
                    )
                    MenuItem(
                        text = "내 댓글 보기",
                        iconResId = R.drawable.ic_comment,
                        enabled = !isLoading,
                        onClick = onMyCommentsClick
                    )
                    MenuItem(
                        text = "최근 본 글",
                        iconResId = R.drawable.ic_recent,
                        enabled = !isLoading,
                        onClick = onRecentViewsClick
                    )
                    MenuItem(
                        text = "설정",
                        iconResId = R.drawable.ic_settings,
                        enabled = !isLoading,
                        onClick = onSettingsClick
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ 로그아웃
                Text(
                    text = "로그아웃",
                    color = if (isLoading) Color.Gray else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                        .clickable(enabled = !isLoading) { onLogoutClick() }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    iconResId: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) Color.Unspecified else Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (enabled) Color.Black else Color.Gray
        )
    }
}