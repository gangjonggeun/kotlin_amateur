package com.example.kotlin_amateur.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlin_amateur.R

class FloatingAddImageAdapter(
    private val images: MutableList<Uri>,  // 수정 가능한 리스트
    private val onDeleteClick: (Int) -> Unit  // 삭제 콜백
) : RecyclerView.Adapter<FloatingAddImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.showImageView)
        val closeButton: ImageButton = view.findViewById(R.id.closeButton)
        val representativeLabel: View = view.findViewById(R.id.representativeLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_floating_add_image, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = images[position]

        Glide.with(holder.imageView.context)
            .load(uri)
            .override(100, 100) // 이미지 크기 제한
            .centerCrop()
            .into(holder.imageView)

        holder.closeButton.setOnClickListener {
            onDeleteClick(position)
        }

        // 대표 사진 레이블 처리
        if (position == 0) {
            holder.representativeLabel.visibility = View.VISIBLE
        } else {
            holder.representativeLabel.visibility = View.GONE
        }
    }

    override fun getItemCount() = images.size

    fun updateList(newList: List<Uri>) {
        images.clear()
        images.addAll(newList)
        notifyDataSetChanged()
    }
}