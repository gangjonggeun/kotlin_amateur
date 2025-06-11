package com.example.kotlin_amateur.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.example.kotlin_amateur.R

class FloatingAddImageAdapter(
    private val images: MutableList<Uri>,
    private val onDeleteClick: (Int) -> Unit
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

        // 🔥 Coil로 메모리 최적화 이미지 로딩
        holder.imageView.load(uri) {
            size(100, 100) // 크기 제한
            scale(Scale.FILL)
            crossfade(false) // 애니메이션 끄기 (메모리 절약)
            memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            diskCachePolicy(coil.request.CachePolicy.DISABLED) // 디스크 캐시 끄기
            allowHardware(false) // 하드웨어 가속 끄기 (메모리 절약)
            placeholder(R.drawable.ic_default_profile)
            error(R.drawable.ic_default_profile)
        }

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