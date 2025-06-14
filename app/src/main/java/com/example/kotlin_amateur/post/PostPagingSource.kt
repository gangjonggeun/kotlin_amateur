package com.example.kotlin_amateur.post

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kotlin_amateur.core.auth.TokenStore
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.example.kotlin_amateur.repository.PostRepository
import com.example.kotlin_amateur.state.ApiResult

class PostPagingSource(
    private val context: Context,
    private val postRepository: PostRepository,
    private val query: String? = null // null이면 전체 목록, 값이 있으면 검색
) : PagingSource<Int, PostListResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostListResponse> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            Log.d("PostPagingSource", "🔥 데이터 로딩 시작 - 페이지: $page, 크기: $pageSize, 검색어: $query")

            // 🌐 API 호출 (검색 여부에 따라 분기)
            val result = if (query.isNullOrEmpty()) {
                // 전체 게시글 목록
                postRepository.getPostList(context, page, pageSize)
            } else {
                // 검색 결과
                postRepository.searchPosts(context, query, page, pageSize)
            }

            when (result) {
                is ApiResult.Success -> {
                    val posts = result.data
                    Log.d("PostPagingSource", "✅ 로딩 성공 - ${posts.size}개 아이템")

                    LoadResult.Page(
                        data = posts,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (posts.isEmpty() || posts.size < pageSize) null else page + 1
                    )
                }
                is ApiResult.Error -> {
                    Log.e("PostPagingSource", "❌ 로딩 실패 - ${result.message}")
                    LoadResult.Error(Exception(result.message))
                }
                is ApiResult.Loading -> {
                    Log.w("PostPagingSource", "⚠️ 로딩 상태 오류")
                    LoadResult.Error(Exception("로딩 상태 오류"))
                }
            }

        } catch (e: Exception) {
            Log.e("PostPagingSource", "❌ 로딩 오류", e)
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
