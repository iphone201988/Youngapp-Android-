package com.tech.young.ui.inbox.view_message

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.UserData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetChatMessageApiResponse
import com.tech.young.data.model.GetChatMessageApiResponse.Data.Messages
import com.tech.young.data.model.ReceivedMessageApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentViewMessageBinding
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject


@AndroidEntryPoint
class ViewMessageFragment : BaseFragment<FragmentViewMessageBinding>() {
    private val viewModel: ViewMessageVM by viewModels()

    private var threadId : String ? = null
    private var recId : String ? = null
    private var userId : String ? = null
    private var userData : UserData ? = null
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var chatAdapter: ChatAdapter
    private var chatList: ArrayList<ChatModel> = ArrayList()
    ////Socket
    private lateinit var handler: Handler
    private lateinit var mSocket: Socket
    override fun onCreateView(view: View) {
        handler = Handler(Looper.getMainLooper())
        socketHandler()

        receivedMessage()

        /**Socket **/
//        try {
//            // Initialize the socket connection
//            mSocket = IO.socket("http://3.148.147.103:8000")
//            mSocket.connect()
//            mSocket.on(Socket.EVENT_CONNECT, onConnect)
//            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
//            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            mSocket.on("sendMessage",onSendMessage)
//            mSocket.on("newMessage",onNewMessage)
//
//        } catch (e: Exception) {
//        }
        // view


        initView()


        // click
        initOnClick()
        // observer
        initObserver()
    }

    private fun receivedMessage() {
        mSocket.on("newMessage") { data ->
            val jsonData = data[0] as JSONObject
            Log.i("newMessage", "Received: $jsonData")

            try {

                val messageData: ReceivedMessageApiResponse? = BindingUtils.parseJson(jsonData.toString())
                Log.i("newMessage", "receivedMessage: $messageData ")
                if (messageData != null && messageData.message != null) {
                    //val chatData = convertToChatHistoryApiResponseData(messageData.data)
                    // Add the new message to the chat adapter and refresh the RecyclerView
                    threadId = messageData.chatId
                    Log.i("dfdfd", "receivedMessage: ${messageData.message} ")

                    val chatData = convertToChatMessage(messageData!!)
                    requireActivity().runOnUiThread {
                        chatAdapter.addData(chatData)
                        chatAdapter.notifyDataSetChanged()
                        binding.rvMessage.scrollToPosition(chatAdapter.itemCount - 1)
                    }


                } else {
                    Log.e("newMessage", "Failed to parse message data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun socketHandler() {
        val token = sharedPrefManager.getLoginData()?.token
        Log.i("dasdasd", "socketHandler: $token")
        try {
            if (!token.isNullOrEmpty()) {
                SocketManager.setSocket(token)  // Establish socket connection with token
                SocketManager.establishConnection()
                mSocket = SocketManager.getSocket()!!
                Log.i("SocketHandler", "socketHandler: $mSocket")
                Log.e("SocketHandler", "Connection is established.")

            } else {
                Log.e("SocketHandler", "Token is missing! Cannot establish connection.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun getLayoutResource(): Int {
        return R.layout.fragment_view_message
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {

         threadId = arguments?.getString("threadId")
        userData = arguments?.getParcelable("userData")

        Log.i("fdsfsadasd", "initView: $userData")
         if (threadId != null){
             viewModel.getChatMessage(Constants.GET_CHAT_MESSAGE+threadId)
         }
        if (userData != null){
            recId = userData?._id
            binding.tvUserName.text = userData?.username
            binding.tvRole.text = userData?.role
            BindingUtils.setUserImageFromBaseUrl(binding.profileImage,userData?.profileImage)
        }

        initAdapter()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.ivSendChat -> {
                    if (binding.etChat.text.toString().trim().isNullOrEmpty()){
                        showToast("Please enter message first")
                        return@observe
                    }
                    sendMessageSocket()
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(requireActivity()){
            when(it?.status){
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "getChatMessage"->{
                            val myDataModel=BindingUtils.parseJson<GetChatMessageApiResponse>(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data!=null){
                                    if (!myDataModel.data!!.messages.isNullOrEmpty()){
                                        chatAdapter.list=groupChatByDate(myDataModel.data!!.messages) as ArrayList
                                        Log.i("dasdasd", "initObserver: ${chatAdapter.list}")
                                        if (chatAdapter.itemCount>0) {
                                            binding.rvMessage.scrollToPosition(chatAdapter.itemCount - 1)
                                        }
                                        chatAdapter.notifyDataSetChanged()
                                    }
                                }

                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
                                }
                            }
                        }

                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    try {
                        showToast(it.message.toString())
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                else -> {

                }
            }
        }

    }
//
//    fun groupChatByDate(convList: List<Messages?>?): List<ChatModel> {
//        val groupedNotifications = linkedMapOf<String, MutableList<Messages>>()
//        if (convList != null) {
//            for (convsList in convList) {
//                val date =
//                    BindingUtils.formatDate(convsList?.createdAt!!)
//                Log.e("date","$date")
//                groupedNotifications.getOrPut(date.first) { mutableListOf() }.add(convsList)
//            }
//        }
//        val items = mutableListOf<ChatModel>()
//        for ((date, dataForChat) in groupedNotifications) {
//            items.add(ChatModel.ConversationDate(date))
//            dataForChat.forEach {
//                items.add(ChatModel.ConversationData(it))
//            }
//        }
//        return items
//    }

    fun groupChatByDate(convList: List<Messages?>?): List<ChatModel> {
        val items = mutableListOf<ChatModel>()

        convList?.forEach { message ->
            message?.let {
                val date = BindingUtils.formatDate(it.createdAt)
                Log.d("ChatDate", "Message on ${date.first} at ${date.second}")
                items.add(ChatModel.ConversationData(it))
            }
        }

        return items
    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

        userId = sharedPrefManager.getUserId()
        if (userId!=null) {
            Log.i("sadasds", "initAdapter: $userId")
            chatAdapter =
                ChatAdapter(requireContext(), chatList, userId.toString(), binding.rvMessage)
            binding.rvMessage.adapter = chatAdapter
        }
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    private fun sendMessageSocket() {
        val message = binding.etChat.text?.trim().toString()
        Log.d("SocketMessage", "Attempting to send message: $message")

        if (message.isEmpty()) {
            Log.w("SocketMessage", "Message is empty, not sending.")
            return
        }

        try {
            val jsonData = JSONObject()

            if (threadId != null) {
                jsonData.put("chatId", threadId)
                jsonData.put("message", message)
            } else {
                jsonData.put("receiverId", recId)
                jsonData.put("message", message)
            }

            Log.d("SocketMessage", "Constructed JSON: $jsonData")

            if (::mSocket.isInitialized && mSocket.connected()) {
                mSocket.emit("sendMessage", jsonData)
                Log.i("SocketMessage", "Message emitted via socket: $jsonData")
                binding.etChat.setText("")
            } else {
                Log.e("SocketMessage", "Socket not connected, message not sent.")
            }

        } catch (e: Exception) {
            Log.e("SocketMessage", "Exception while sending message: ${e.localizedMessage}")
            e.printStackTrace()
        }

    }


//    private val onSendMessage = Emitter.Listener { args ->
//        handler.post {
//            try {
//                val data = args[0] as JSONObject
//                Log.e("sendMsg", Gson().toJson(data))
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private val onNewMessage = Emitter.Listener { args ->
//        handler.post {
//            try {
//                val data = args[0] as JSONObject
//                Log.e("receiveData", Gson().toJson(data))
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private val onConnect = Emitter.Listener {
//        Log.d("Socket", "Connected")
//    }
//
//    private val onDisconnect = Emitter.Listener {
//        Log.d("Socket", "Disconnected")
//    }
//
//    private val onConnectError = Emitter.Listener { args ->
//        Log.e("Socket", "Connect error: ${args.firstOrNull()}")
//    }
//
//    fun disconnectSocket() {
//        if (::mSocket.isInitialized) {
//            mSocket.off(Socket.EVENT_CONNECT, onConnect)
//            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
//            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            mSocket.off("sendMessage", onSendMessage)
//            mSocket.off("newMessage", onNewMessage)
//            mSocket.disconnect()
//        }
//    }

    fun convertToChatMessage(received: ReceivedMessageApiResponse): GetChatMessageApiResponse.Data.Messages {
        return GetChatMessageApiResponse.Data.Messages(
            _id = received._id,
            chatId = received.chatId,
            createdAt = received.createdAt,
            isRead = received.isRead,
            message = received.message,
            senderId = received.senderId?.let {
                GetChatMessageApiResponse.Data.Messages.SenderId(
                    _id = it._id,
                    profileImage = it.profileImage,
                    role = it.role,
                    username = it.username
                )
            }
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        SocketManager.closeConnection()
    }
}