package com.tech.young.ui.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.databinding.NewsItemViewBinding

class NewsCustomAdapter(val context: Context,val newsList: List<String>) :
    RecyclerView.Adapter<NewsCustomAdapter.NewsViewHolder>() {
    inner class NewsViewHolder(val binding: NewsItemViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding =
            NewsItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsList[position]
        if (item==""){
            holder.binding.image.visibility= View.INVISIBLE
            holder.binding.view.visibility= View.INVISIBLE
            holder.binding.tvTitle.visibility= View.INVISIBLE
            holder.binding.ivArrow.visibility= View.INVISIBLE
        }
        else{
            holder.binding.image.visibility= View.VISIBLE
            holder.binding.view.visibility= View.VISIBLE
            holder.binding.tvTitle.visibility= View.VISIBLE
            holder.binding.ivArrow.visibility= View.VISIBLE
        }


//        val imageHeight = when (position) {
//            1, 3, 5 -> dpToPx(holder.itemView.context, 88)
//            else -> dpToPx(holder.itemView.context, 150)
//        }
//
//        holder.binding.image.apply {
//            layoutParams = layoutParams.apply {
//                height = imageHeight
//            }
//            invalidateOutline()
//            requestLayout()
//        }
//
//        holder.binding.view.layoutParams = holder.binding.view.layoutParams.apply {
//            height = imageHeight
//        }
        holder.binding.tvTitle.text = item
        holder.binding.image.setOnClickListener {
            Log.d("position", "${holder.adapterPosition}")
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}