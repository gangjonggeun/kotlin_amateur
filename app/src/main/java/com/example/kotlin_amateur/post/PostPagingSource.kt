package com.example.kotlin_amateur.post

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kotlin_amateur.core.PostListType
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.PostRepository
import com.example.kotlin_amateur.repository.ProfilePostRepository
import com.example.kotlin_amateur.state.ApiResult

class PostPagingSource(
    private val context: Context,
    private val postRepository: PostRepository? = null, // 홍 화면용 (null 가능)
    private val profilePostRepository: ProfilePostRepository? = null, // 프로필 API용 (null 가능)
    private val query: String? = null, // null이면 전체 목록, 값이 있으면 검색
    private val postListType: PostListType = PostListType.HOME // 🎯 게시글 타입 추가
) : PagingSource<Int, PostListResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostListResponse> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            Log.d("PostPagingSource", "🔥 데이터 로딩 시작 - 타입: ${postListType.displayName}, 페이지: $page, 크기: $pageSize, 검색어: $query")

            // 🌐 API 호출 (타입별 분기 처리)
            val result = when (postListType) {
                PostListType.HOME -> {
                    if (postRepository != null) {
                        if (query.isNullOrEmpty()) {
                            // 전체 게시글 목록
                            postRepository.getPostList(context, page, pageSize)
                        } else {
                            // 검색 결과
                            postRepository.searchPosts(context, query, page, pageSize)
                        }
                    } else {
                        Log.e("PostPagingSource", "❌ HOME 타입인데 PostRepository가 null")
                        ApiResult.Error(500, "PostRepository가 설정되지 않았습니다")
                    }
                }
                
                PostListType.MY_POSTS -> {
                    // 📝 내 게시글 (프로필 API 사용)
                    if (profilePostRepository != null) {
                        Log.d("PostPagingSource", "📝 내 게시글 로딩 (ProfilePostRepository)")
                        profilePostRepository.getMyPosts(context, page, pageSize)
                    } else {
                        Log.w("PostPagingSource", "⚠️ ProfilePostRepository가 null - 빈 데이터 반환")
                        ApiResult.Success(emptyList<PostListResponse>())
                    }
                }
                
                PostListType.LIKED_POSTS -> {
                    // ❤️ 좋아요한 글 (프로필 API 사용)
                    if (profilePostRepository != null) {
                        Log.d("PostPagingSource", "❤️ 좋아요한 글 로딩 (ProfilePostRepository)")
                        profilePostRepository.getLikedPosts(context, page, pageSize)
                    } else {
                        Log.w("PostPagingSource", "⚠️ ProfilePostRepository가 null - 빈 데이터 반환")
                        ApiResult.Success(emptyList<PostListResponse>())
                    }
                }
                
                PostListType.RECENT_VIEWED -> {
                    // 👀 최근 본 글 (프로필 API 사용)
                    if (profilePostRepository != null) {
                        Log.d("PostPagingSource", "👀 최근 본 글 로딩 (ProfilePostRepository)")
                        profilePostRepository.getRecentViewedPosts(context, page, pageSize)
                    } else {
                        Log.w("PostPagingSource", "⚠️ ProfilePostRepository가 null - 빈 데이터 반환")
                        ApiResult.Success(emptyList<PostListResponse>())
                    }
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    val posts = result.data
                    Log.d("PostPagingSource", "✅ 로딩 성공 - ${posts.size}개 아이템 (${postListType.displayName})")

                    LoadResult.Page(
                        data = posts,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (posts.isEmpty() || posts.size < pageSize) null else page + 1
                    )
                }
                is ApiResult.Error -> {
                    Log.e("PostPagingSource", "❌ 로딩 실패 - ${result.message} (${postListType.displayName})")
                    LoadResult.Error(Exception(result.message))
                }
                is ApiResult.Loading -> {
                    Log.w("PostPagingSource", "⚠️ 로딩 상태 오류 (${postListType.displayName})")
                    LoadResult.Error(Exception("로딩 상태 오류"))
                }
            }

        } catch (e: Exception) {
            Log.e("PostPagingSource", "❌ 로딩 오류 (${postListType.displayName})", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostListResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val TAG = "PostPagingSource"
    }
}
