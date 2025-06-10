package com.example.kotlin_amateur.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_amateur.model.Comment
import com.example.kotlin_amateur.model.PostDetail
import com.example.kotlin_amateur.model.Reply
import com.example.kotlin_amateur.repository.PostDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "🔥PostDetailViewModel"
    }

    // 🔧 개선 1: postId를 private val로 명확히 관리
    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val _postDetail = MutableStateFlow<PostDetail?>(null)
    val postDetail = _postDetail.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // 🔧 개선 2: init 블록에서 자동 로드 (선택사항)
    init {
        Log.d(TAG, "✅ ViewModel 초기화 - postId: '$postId'")
        if (postId != "") {
            Log.d(TAG, "📥 자동 로드 시작")
            loadPostDetail(postId)
            loadComments(postId)
        } else {
            Log.w(TAG, "⚠️ postId가 비어있습니다!")
        }
    }

    fun loadPostDetail(postId: String) {
        Log.d(TAG, "🔍 포스트 상세 로드 시작 - postId: '$postId'")
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "⏳ 로딩 상태 true")

            repository.getPostDetail(postId)
                .onSuccess { response ->
                    Log.d(TAG, "✅ 포스트 상세 로드 성공")
                    Log.d(TAG, "📋 응답 데이터: id=${response.id}, content=${response.content.take(50)}...")

                    _postDetail.value = PostDetail(
                        id = response.id,
                        title = response.title,
                        content = response.content,
                        images = response.images,
                        authorNickname = response.authorNickname,
                        authorUserId = response.authorUserId,
                        authorProfileImage = response.authorProfileImage,
                        createdAt = response.createdAt,
                        likeCount = response.likeCount,
                        isLiked = response.isLiked
                    )
                    Log.d(TAG, "💾 PostDetail StateFlow 업데이트 완료")
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 포스트 상세 로드 실패: ${exception.message}")
                    Log.e(TAG, "❌ 스택 트레이스: ", exception)
                    _error.value = exception.message
                }

            _isLoading.value = false
            Log.d(TAG, "⏳ 로딩 상태 false")
        }
    }

    fun loadComments(postId: String) {
        Log.d(TAG, "💬 댓글 로드 시작 - postId: '$postId'")
        viewModelScope.launch {
            repository.getComments(postId)
                .onSuccess { response ->
                    Log.d(TAG, "✅ 댓글 로드 성공 - 댓글 수: ${response.size}")

                    // 🔥 서버 응답 원본 데이터 로깅
                    response.forEachIndexed { index, commentResponse ->
                        Log.d(TAG, "📝 댓글 [$index] 원본 데이터:")
                        Log.d(TAG, "  - id: ${commentResponse.id}")
                        Log.d(TAG, "  - authorNickname: '${commentResponse.authorNickname}'")
                        Log.d(TAG, "  - authorUserId: '${commentResponse.authorUserId}'")
                        Log.d(TAG, "  - authorUserId 타입: ${commentResponse.authorUserId?.javaClass?.simpleName}")
                        Log.d(TAG, "  - authorUserId null?: ${commentResponse.authorUserId == null}")
                        Log.d(TAG, "  - authorUserId 빈값?: ${commentResponse.authorUserId?.isEmpty()}")
                    }

                    _comments.value = response.map { commentResponse ->
                        try {
                            Comment(
                                id = commentResponse.id,
                                content = commentResponse.content,
                                authorNickname = commentResponse.authorNickname,
                                authorUserId = commentResponse.authorUserId, // 🔥 여기서 에러 발생 확인
                                authorProfileImage = commentResponse.authorProfileImage,
                                createdAt = commentResponse.createdAt,
                                replyCount = commentResponse.replies.size,
                                replies = commentResponse.replies.map { replyResponse ->
                                    Reply(
                                        id = replyResponse.id,
                                        content = replyResponse.content,
                                        authorNickname = replyResponse.authorNickname,
                                        authorUserId = replyResponse.authorUserId, // 🔥 여기서도 에러 발생 확인
                                        authorProfileImage = replyResponse.authorProfileImage,
                                        createdAt = replyResponse.createdAt,
                                        commentId = replyResponse.commentId
                                    )
                                }
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ 댓글 변환 중 에러: ${e.message}")
                            Log.e(TAG, "❌ 문제 댓글 데이터: $commentResponse")
                            throw e
                        }
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 댓글 로드 실패: ${exception.message}")
                    _error.value = exception.message
                }
        }
    }

    fun submitComment(content: String) {
        Log.d(TAG, "✏️ 댓글 작성 시작 - content: '${content.take(30)}...'")
        viewModelScope.launch {
            repository.createComment(postId, content)
                .onSuccess { newComment ->
                    Log.d(TAG, "✅ 댓글 작성 성공 - id: ${newComment.id}")

                    // 새 댓글을 리스트에 추가
                    val currentComments = _comments.value.toMutableList()
                    val newCommentModel = Comment(
                        id = newComment.id,
                        content = newComment.content,
                        authorNickname = newComment.authorNickname,
                        authorUserId = newComment.authorUserId,
                        authorProfileImage = newComment.authorProfileImage,
                        createdAt = newComment.createdAt,
                        replyCount = 0,
                        replies = emptyList()
                    )
                    currentComments.add(0, newCommentModel)
                    _comments.value = currentComments

                    Log.d(TAG, "💾 새 댓글 추가 완료 - 총 댓글 수: ${_comments.value.size}")
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 댓글 작성 실패: ${exception.message}")
                    _error.value = exception.message
                }
        }
    }

    fun submitReply(commentId: String, content: String) {
        Log.d(TAG, "↩️ 답글 작성 시작 - commentId: '$commentId', content: '${content.take(30)}...'")
        viewModelScope.launch {
            repository.createReply(commentId, content)
                .onSuccess { newReply ->
                    Log.d(TAG, "✅ 답글 작성 성공 - id: ${newReply.id}")

                    // 해당 댓글에 답글 추가
                    _comments.value = _comments.value.map { comment ->
                        if (comment.id == commentId) {
                            Log.d(TAG, "🎯 댓글 찾음: ${comment.id}, 기존 답글 수: ${comment.replies.size}")
                            val updatedReplies = comment.replies.toMutableList()
                            updatedReplies.add(Reply(
                                id = newReply.id,
                                content = newReply.content,
                                authorNickname = newReply.authorNickname,
                                authorUserId = newReply.authorUserId,
                                authorProfileImage = newReply.authorProfileImage,
                                createdAt = newReply.createdAt,
                                commentId = newReply.commentId
                            ))
                            Log.d(TAG, "💾 답글 추가 완료 - 새 답글 수: ${updatedReplies.size}")
                            comment.copy(
                                replies = updatedReplies,
                                replyCount = updatedReplies.size,
                                isReplyInputVisible = false // 답글 입력창 닫기
                            )
                        } else {
                            comment
                        }
                    }
                    Log.d(TAG, "💾 Comments StateFlow 업데이트 완료")
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 답글 작성 실패: ${exception.message}")
                    _error.value = exception.message
                }
        }
    }

    fun toggleLike() {
        Log.d(TAG, "❤️ 좋아요 토글 시작")
        viewModelScope.launch {
            val currentPost = _postDetail.value ?: run {
                Log.w(TAG, "⚠️ currentPost가 null입니다")
                return@launch
            }

            Log.d(TAG, "📊 현재 좋아요 상태: ${currentPost.isLiked}, 좋아요 수: ${currentPost.likeCount}")

            repository.toggleLike(currentPost.id)
                .onSuccess {
                    Log.d(TAG, "✅ 좋아요 토글 성공")
                    _postDetail.value = currentPost.copy(
                        isLiked = !currentPost.isLiked,
                        likeCount = if (currentPost.isLiked)
                            currentPost.likeCount - 1 else currentPost.likeCount + 1
                    )
                    Log.d(TAG, "💾 새 좋아요 상태: ${!currentPost.isLiked}, 새 좋아요 수: ${if (currentPost.isLiked) currentPost.likeCount - 1 else currentPost.likeCount + 1}")
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 좋아요 토글 실패: ${exception.message}")
                    _error.value = exception.message
                }
        }
    }

    fun toggleReplyInput(commentId: String) {
        Log.d(TAG, "📝 답글 입력창 토글 - commentId: '$commentId'")
        _comments.value = _comments.value.map { comment ->
            if (comment.id == commentId) {
                Log.d(TAG, "🎯 댓글 찾음: ${comment.id}, 현재 입력창 상태: ${comment.isReplyInputVisible}")
                comment.copy(isReplyInputVisible = !comment.isReplyInputVisible)
            } else {
                comment.copy(isReplyInputVisible = false) // 다른 댓글의 답글 입력창은 닫기
            }
        }
        Log.d(TAG, "💾 답글 입력창 상태 업데이트 완료")
    }

    fun toggleRepliesVisibility(commentId: String) {
        Log.d(TAG, "👁️ 답글 표시/숨김 토글 - commentId: '$commentId'")
        _comments.value = _comments.value.map { comment ->
            if (comment.id == commentId) {
                Log.d(TAG, "🎯 댓글 찾음: ${comment.id}, 현재 답글 표시 상태: ${comment.isRepliesVisible}")
                comment.copy(isRepliesVisible = !comment.isRepliesVisible)
            } else {
                comment
            }
        }
        Log.d(TAG, "💾 답글 표시 상태 업데이트 완료")
    }

    fun clearError() {
        Log.d(TAG, "🧹 에러 상태 클리어")
        _error.value = null
    }

    // 🔧 개선 3: 새로고침 기능 추가 (선택사항)
    fun refresh() {
        Log.d(TAG, "🔄 새로고침 시작")
        if (postId != "") {
            loadPostDetail(postId)
            loadComments(postId)
        } else {
            Log.w(TAG, "⚠️ postId가 비어있어서 새로고침 불가")
        }
    }
    override fun onCleared() {
        super.onCleared()
        // 🧹 메모리 정리
        _comments.value = emptyList()
        _postDetail.value = null
        Log.d("ViewModel", "PostDetailViewModel 메모리 정리 완료")
    }
}