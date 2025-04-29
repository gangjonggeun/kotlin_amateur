package com.example.kotlin_amateur.util

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.model.DataModel
import java.net.URLDecoder

//class RecyclerViewData(
//    val image: String,
//    val title: String,
//    val content: String,
//    val num_likes: Int,
//    val num_comments: Int
//)

class HomeRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val iv_image: ImageView = view.findViewById(R.id.costum_list_imgView)
    val tv_title: TextView = view.findViewById(R.id.costum_list_title)
    val tv_content: TextView = view.findViewById(R.id.costum_list_content)
    val tv_likes: TextView = view.findViewById(R.id.heart_count_tv)
    val tv_conmment: TextView = view.findViewById(R.id.commment_count_tv)
}

class HomeRecyclerViewAdapter(private var dataList: List<DataModel>,  private val onItemClick: (DataModel) -> Unit) :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.costum_list, parent, false)
        return HomeRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = dataList[position]

        // 첫 번째 이미지 URL만 사용
        val rawUri = item.images.firstOrNull() // image: String (content://... 형태)
        val firstImageUrl = Uri.parse(URLDecoder.decode(rawUri, "UTF-8"))

    //    val resId = context.resources.getIdentifier(item.images.firstOrNull(), "drawable", context.packageName)

        Glide.with(context)
            .load(firstImageUrl)
            .placeholder(R.drawable.loading_placeholder)
            .error(R.drawable.image_error_placeholder)
            .into(holder.iv_image)



        holder.tv_title.text = item.title
        holder.tv_content.text = item.content
        holder.tv_likes.text = item.likes.toString()
        holder.tv_conmment.text = item.comments.toString()

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

    }

    // 외부에서 데이터 갱신할 때 사용
    fun updateList(newList: List<DataModel>) {
        dataList = newList
        notifyDataSetChanged()
    }
}