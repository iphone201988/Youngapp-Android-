package com.tech.young.ui.inbox.view_message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.base.utils.BindingUtils
import com.tech.young.data.model.GetChatMessageApiResponse
import com.tech.young.databinding.ChatLeftBubbleBinding
import com.tech.young.databinding.ChatRightBubbleBinding
import com.tech.young.databinding.DayItemViewBinding

class ChatAdapter  (val context: Context,
                    var list: MutableList<ChatModel>,
                    var myId: String,
                    var recyclerView: RecyclerView,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var lastDate: String? = null
    companion object {
//        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVE = 2
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
//            VIEW_TYPE_DATE_HEADER -> {
//                ChatDateViewHolder(
//                    ItemDayViewBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    )
//                )
//            }
            VIEW_TYPE_SENDER->{
                ConversationRightViewHolder(ChatRightBubbleBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            }
            VIEW_TYPE_RECEIVE->{
                ConversationLeftViewHolder(ChatLeftBubbleBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {

//            is ChatModel.ConversationDate -> (holder as ChatDateViewHolder).binding.apply {
//                if (position == 0) {
//                    // Show date for the first item
//                    tvDay.visibility = View.VISIBLE
//                    tvDay.text = item.date
//                    lastDate = item.date
//                } else {
//                    val previousItem = list[position - 1]
//                    val previousDate = when (previousItem) {
//                        is ChatModel.ConversationDate -> previousItem.date
//                        is ChatModel.ConversationData -> previousItem.data
//                    }
//                    if (previousDate == item.date) {
//                        tvDay.visibility = View.GONE
//                    } else {
//                        tvDay.visibility = View.VISIBLE
//                        tvDay.text = item.date
//                        lastDate = item.date
//                    }
//                }
//            }
            is ChatModel.ConversationData->
                if (item.data.senderId?._id==myId) {
                    (holder as ConversationRightViewHolder).binding.apply {
                        tvRight.text=item.data.message
                        val formattedDate= BindingUtils.formatDateTimeForChat(item.data.createdAt!!)
                        tvRightDate.text=formattedDate
                    }
                }
                else{
                    (holder as ConversationLeftViewHolder).binding.apply {
                        tvLeft.text=item.data.message
                        val formattedDate= BindingUtils.formatDateTimeForChat(item.data.createdAt!!)
                        tvLeftDate.text=formattedDate
                    }
                }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]){
//            is ChatModel.ConversationDate -> 0
            is ChatModel.ConversationData -> {
                if ((list[position] as ChatModel.ConversationData).data.senderId?._id==myId) {
                    VIEW_TYPE_SENDER
                } else {
                    VIEW_TYPE_RECEIVE
                }
            }
        }
    }

//    class ChatDateViewHolder(itemView: DayItemViewBinding) :
//        RecyclerView.ViewHolder(itemView.root) {
//        val binding = itemView
//    }

    class ConversationRightViewHolder(itemView: ChatRightBubbleBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    class ConversationLeftViewHolder(itemView: ChatLeftBubbleBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }


    fun addData(data: GetChatMessageApiResponse.Data.Messages) {
        val positionStart = list.size
        list.add(ChatModel.ConversationData(data))
        notifyItemInserted(positionStart)
    }


}