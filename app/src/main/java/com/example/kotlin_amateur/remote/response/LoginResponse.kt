package com.example.kotlin_amateur.remote.response

import java.util.UUID


/**
 * 유저들
 *
 * */
data class LoginResponse(

    val id: Long = 0, // 서버 내에서 사용하는 DB 고유 ID (PK)

    val name: String,
    val email: String = "",

    var isNewUser: Boolean = true,
    /**
     * @param googleSub
     * jwt sub 해당유저 구글 고유 식별자
     * */
    val googleSub: String = "",  // 🔸 JWT의 sub (Google 고유 식별자)
    val nickname: String = "",

    val profileImageUrl: String? = null,

    val posts: List<PostResponse> = emptyList(),

    val comments: List<CommentResponse> = emptyList(),

    val replies: List<ReplyResponse> = emptyList(),


    val accessToken: String = UUID.randomUUID().toString()
)

