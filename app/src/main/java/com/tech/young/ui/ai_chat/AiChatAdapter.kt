package com.tech.young.ui.ai_chat

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.tech.young.R
import com.tech.young.data.AiChatModel


class AiChatAdapter(private val list: MutableList<AiChatModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val LEFT = 1
        const val RIGHT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isSender) RIGHT else LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == RIGHT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout_ai_right_chat, parent, false)
            RightVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout_ai_left_chat, parent, false)
            LeftVH(view)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is RightVH) {
            holder.tv.text = item.message
        } else if (holder is LeftVH) {
            if (item.message == "typing...") {
                holder.tv.visibility = View.GONE
                holder.typing.visibility = View.VISIBLE
                holder.typing.playAnimation()
            } else {
                holder.tv.visibility = View.VISIBLE
                holder.typing.visibility = View.GONE
                holder.typing.cancelAnimation()
                
                val formattedMessage = item.message
                    .replace("<strong>", "<b><font color='#000000'>")
                    .replace("</strong>", "</font></b>")
                    .replace("%<br><br>", "%<br><br><br>") 

                holder.tv.text = HtmlCompat.fromHtml(formattedMessage, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    fun addMessage(chat: AiChatModel) {
        list.add(chat)
        notifyItemInserted(list.size - 1)
    }

    fun removeTyping() {
        val index = list.indexOfLast { it.message == "typing..." }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class LeftVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tvLeftChat)
        val typing: LottieAnimationView = itemView.findViewById(R.id.typingAnimation)
    }

    class RightVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tvRightChat)
    }
}
