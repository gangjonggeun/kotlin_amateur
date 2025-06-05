//package com.example.kotlin_amateur.adapter
//
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.kotlin_amateur.R
//
//
//// ViewPager2 Adapter
//class PostDetailimageAdapter(private val images: List<String>) : RecyclerView.Adapter<PostDetailimageAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val imageView: androidx.appcompat.widget.AppCompatImageView = view.findViewById(R.id.imageView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_detail_image, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = images.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val imageUrl = images[position]
//        Glide.with(holder.itemView)
//            .load(imageUrl)
//            .centerCrop()
//            .into(holder.imageView)
//    }
//
//
//}