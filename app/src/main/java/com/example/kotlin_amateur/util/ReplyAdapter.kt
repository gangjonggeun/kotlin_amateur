package com.example.kotlin_amateur.util


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.model.ReplyModel

class ReplyAdapter(private var replyList: List<ReplyModel>) :
    RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    class ReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val writerId: TextView = view.findViewById(R.id.reply_writer_id)
        val timestamp: TextView = view.findViewById(R.id.reply_timestamp)
        val content: TextView = view.findViewById(R.id.reply_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val item = replyList[position]
        holder.writerId.text = item.replyId
        holder.timestamp.text = item.replyTimestamp
        holder.content.text = item.replyContent
    }

    override fun getItemCount(): Int = replyList.size

    fun updateList(newList: List<ReplyModel>) {
        replyList = newList
        notifyDataSetChanged()
    }
}
