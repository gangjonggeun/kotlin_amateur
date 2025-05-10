package com.example.kotlin_amateur.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.model.CommentModel
import com.example.kotlin_amateur.model.DataModel

class CommentAdapter(
    private val context: Context,
    private var comments: List<CommentModel>,
    private val userPost: DataModel,
    private val onReplySubmit: (String, String) -> Unit,
    private var likeCount: Int = 0
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_COMMENT = 1
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val userIdText: TextView = view.findViewById(R.id.userIdText)
        val tvDetailTitle: TextView = view.findViewById(R.id.tv_detailTitle)
        val tvDetailContent: TextView = view.findViewById(R.id.tv_detailContent)
        val commentCountText: TextView = view.findViewById(R.id.commentCountText)
        val likeCountText: TextView = view.findViewById(R.id.likeCountText)
        val timeStampText : TextView = view.findViewById(R.id.postTimestampText)
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val writerId: TextView = view.findViewById(R.id.comment_writer_id)
        val timestamp: TextView = view.findViewById(R.id.comment_timestamp)
        val content: TextView = view.findViewById(R.id.comment_content)
        val replyRecycler: RecyclerView = view.findViewById(R.id.reply_recycler)
        val replyInputLayout: View = view.findViewById(R.id.reply_input_layout)
        val replyEditText: EditText = view.findViewById(R.id.reply_edit_text)
        val replySendButton: ImageButton = view.findViewById(R.id.reply_send_button)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_comment_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
            CommentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.likeCountText.text = likeCount.toString()
            holder.userIdText.text = userPost.id
            holder.tvDetailTitle.text = userPost.title
            holder.tvDetailContent.text = userPost.content
            holder.commentCountText.text = comments.size.toString()
            holder.timeStampText.text = userPost.timestamp
        } else if (holder is CommentViewHolder) {
            val comment = comments[position - 1]
            holder.writerId.text = comment.commentId
            holder.timestamp.text = comment.commentTimestamp
            holder.content.text = comment.commentContent

            holder.replyRecycler.layoutManager = LinearLayoutManager(context)
            holder.replyRecycler.adapter = ReplyAdapter(comment.replies)

            holder.itemView.setOnClickListener {
                holder.replyInputLayout.visibility =
                    if (holder.replyInputLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            holder.replySendButton.setOnClickListener {
                val replyText = holder.replyEditText.text.toString()
                if (replyText.isNotBlank()) {
                    onReplySubmit(comment.commentId, replyText)
                    holder.replyEditText.text.clear()
                    holder.replyInputLayout.visibility = View.GONE
                }
            }
        }
    }
    fun updateLikeCount(newCount: Int) {
        this.likeCount = newCount
        notifyItemChanged(0) // 헤더만 갱신
    }
    override fun getItemCount(): Int = comments.size + 1

    fun updateList(newList: List<CommentModel>) {
        comments = newList
        notifyDataSetChanged()
    }
}