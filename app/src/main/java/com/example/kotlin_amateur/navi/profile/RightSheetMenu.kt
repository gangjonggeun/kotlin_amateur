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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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

    val userInfo by profileViewModel.userInfo.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        profileViewModel.fetchMyProfile(context)
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
                    .statusBarsPadding()         // ✅ 상태바 영역 피하기
                   // .navigationBarsPadding()     // ✅ 하단 영역 피하기
                    .padding(24.dp)
            ) {


                // ✅ 프로필 이미지
                if (userInfo?.profileImageUrl != null) {
                    AsyncImage(
                        model = userInfo!!.profileImageUrl,
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
                            Log.d("AsyncImage", "✅ 이미지 로드 성공: ${userInfo!!.profileImageUrl}")
                        },
                        onError = {
                            Log.e("AsyncImage", "❌ 이미지 로드 실패: ${userInfo!!.profileImageUrl}")
                        },
                        onLoading = {
                            Log.d("AsyncImage", "⏳ 이미지 로딩 중: ${userInfo!!.profileImageUrl}")
                        }
                    )
                } else {
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


                // ✅ 닉네임
                Text(
                    text = userInfo?.nickname ?: "닉네임 없음",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable { onEditNicknameClick() }
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

                // ✅ 메뉴 항목
                Column {
                    MenuItem("내 게시글", R.drawable.ic_post, onMyPostsClick)
                    MenuItem("좋아요한 글", R.drawable.ic_like, onLikedPostsClick)
                    MenuItem("내 댓글 보기", R.drawable.ic_comment, onMyCommentsClick)
                    MenuItem("최근 본 글", R.drawable.ic_recent, onRecentViewsClick)
                    MenuItem("설정", R.drawable.ic_settings, onSettingsClick)
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ 로그아웃
                Text(
                    text = "로그아웃",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                        .clickable { onLogoutClick() }
                )
            }
        }
    }
}

@Composable
fun MenuItem(text: String, iconResId: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp)
    }
}