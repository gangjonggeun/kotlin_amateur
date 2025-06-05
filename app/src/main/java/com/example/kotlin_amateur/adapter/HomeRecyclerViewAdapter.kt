package com.example.kotlin_amateur.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.remote.response.PostListResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class HomeRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // 프로필 영역
    val profileImage: ShapeableImageView = view.findViewById(R.id.profile_image)
    val authorNickname: TextView = view.findViewById(R.id.author_nickname)
    val timestamp: TextView = view.findViewById(R.id.timestamp)

    // 메인 컨텐츠
    val titleText: TextView = view.findViewById(R.id.title_text)  // 제목 추가
    val mainImage: ShapeableImageView = view.findViewById(R.id.main_image)
    val contentText: TextView = view.findViewById(R.id.content_text)

    // 액션 버튼들
    val likeButton: MaterialButton = view.findViewById(R.id.like_button)
    val commentButton: MaterialButton = view.findViewById(R.id.comment_button)
    val shareButton: MaterialButton = view.findViewById(R.id.share_button)
    val bookmarkButton: MaterialButton = view.findViewById(R.id.bookmark_button)
}
class HomeRecyclerViewAdapter(
    private val postList: ArrayList<PostListResponse>,
    private val onItemClick: (PostListResponse) -> Unit,
    private val onLikeClick: ((PostListResponse, Int) -> Unit)? = null,
    private val onCommentClick: ((PostListResponse) -> Unit)? = null,
    private val onShareClick: ((PostListResponse) -> Unit)? = null,
    private val onBookmarkClick: ((PostListResponse) -> Unit)? = null
) : RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    // postList에 접근할 수 있도록 public 프로퍼티 추가
    val posts: ArrayList<PostListResponse> get() = postList

    // 또는 특정 아이템 업데이트 메서드 제공
    fun updatePostAtPosition(position: Int, updatedPost: PostListResponse) {
        if (position in 0 until postList.size) {
            postList[position] = updatedPost
            notifyItemChanged(position)
        }
    }

    fun getPostAtPosition(position: Int): PostListResponse? {
        return if (position in 0 until postList.size) {
            postList[position]
        } else null
    }

    // 기존 메서드들...
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return HomeRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val post = postList[position]
        val context = holder.itemView.context

        // 프로필 정보
        holder.authorNickname.text = post.authorNickname
        holder.timestamp.text = post.formattedTime

        // 프로필 이미지 처리 (기본 이미지 또는 실제 프로필 이미지)
        setupProfileImage(holder, post, context)

        // 제목과 내용
        holder.titleText.text = post.postTitle  // 제목 추가
        holder.contentText.text = post.displayContent

        // 메인 이미지 처리
        setupMainImage(holder, post, context)

        // 좋아요 버튼 상태 및 클릭 리스너
        setupLikeButton(holder, post, position)

        // 나머지 버튼들...
        setupOtherButtons(holder, post)

        // 전체 아이템 클릭 리스너
        holder.itemView.setOnClickListener {
            onItemClick(post)
        }
    }

    private fun setupLikeButton(holder: HomeRecyclerViewHolder, post: PostListResponse, position: Int) {
        val context = holder.itemView.context

        // 좋아요 개수 표시
        holder.likeButton.text = post.likeCount.toString()

        // 좋아요 상태에 따른 UI 변경
        if (post.isLikedByCurrentUser) {
            holder.likeButton.setIconResource(R.drawable.ic_heart_filled)
            holder.likeButton.setIconTintResource(R.color.like_active)
            holder.likeButton.setTextColor(ContextCompat.getColor(context, R.color.like_active))
        } else {
            holder.likeButton.setIconResource(R.drawable.ic_heart_outline)
            holder.likeButton.setIconTintResource(R.color.gray)
            holder.likeButton.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // 좋아요 버튼 클릭 리스너
        holder.likeButton.setOnClickListener {
            onLikeClick?.invoke(post, position)
        }
    }
    private fun setupProfileImage(holder: HomeRecyclerViewHolder, post: PostListResponse, context: Context) {
        // 프로필 이미지 URL이 있다면 (추후 PostListResponse에 profileImageUrl 필드 추가)
        val profileImageUrl = post.authorProfileImageUrl // 이 필드를 PostListResponse에 추가해야 함

        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(profileImageUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .centerCrop())
                .into(holder.profileImage)
        } else {
            // 기본 프로필 이미지
            holder.profileImage.setImageResource(R.drawable.ic_default_profile)
        }
    }

    private fun setupMainImage(holder: HomeRecyclerViewHolder, post: PostListResponse, context: Context) {
        if (post.hasImage) {
            holder.mainImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.representativeImageUrl)
                .apply(RequestOptions()
                    .placeholder(R.color.light_gray)
                    .error(R.color.light_gray)
                    .centerCrop())
                .into(holder.mainImage)
        } else {
            holder.mainImage.visibility = View.GONE
        }
    }

    private fun setupOtherButtons(holder: HomeRecyclerViewHolder, post: PostListResponse) {
        // 댓글 버튼
        holder.commentButton.text = post.commentCount.toString()
        holder.commentButton.setOnClickListener {
            onCommentClick?.invoke(post) ?: onItemClick(post)
        }

        // 공유 버튼
        holder.shareButton.setOnClickListener {
            onShareClick?.invoke(post)
        }

        // 북마크 버튼
        holder.bookmarkButton.setOnClickListener {
            onBookmarkClick?.invoke(post)
        }
    }
    override fun getItemCount(): Int = postList.size

    fun updateList(newList: ArrayList<PostListResponse>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}

