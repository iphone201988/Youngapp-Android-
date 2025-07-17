package com.tech.young.ui.inbox.view_message

import com.tech.young.data.model.GetChatMessageApiResponse

sealed class ChatModel {
//    data class ConversationDate(val date :String) : ChatModel()
    data class ConversationData(val data: GetChatMessageApiResponse.Data.Messages) : ChatModel()
}