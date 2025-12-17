package com.tech.young.ui.stream_screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.tech.MediaCapturer
import com.tech.buckChats.ui.media_soup_webtrc.consumerTransport
import com.tech.buckChats.ui.media_soup_webtrc.consumerTransportAudio
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.showToast
import com.tech.young.data.model.ConsumerJoinModel
import com.tech.young.data.model.ConsumerModel
import com.tech.young.data.model.CreateTransportModel
import com.tech.young.data.model.ReceivedData
import com.tech.young.databinding.FragmentConsumerStreamBinding
import com.tech.young.databinding.ItemLayoutLiveCommentsBinding
import com.tech.young.databinding.ItemLayoutStreamEndBinding
import com.tech.young.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Device
import org.mediasoup.droid.MediasoupClient
import org.mediasoup.droid.RecvTransport
import org.mediasoup.droid.Transport
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import kotlin.coroutines.resume


@AndroidEntryPoint
class ConsumerStreamFragment : BaseFragment<FragmentConsumerStreamBinding>() , BaseCustomDialog.Listener {

    private val viewModel : StreamVM by viewModels()
    private lateinit var mediaSoupDevice: Device
    private var joinRoomModel: ConsumerJoinModel? = null
    private var createTransportModel: CreateTransportModel? = null
    private var cconsumerTransport: consumerTransport? = null
    private var cconsumerTransportAudio: consumerTransportAudio? = null
    private lateinit var mediaCapture: MediaCapturer
    private lateinit var eglBaseContext: EglBase.Context
    private lateinit var audioTrack2: AudioTrack
    private var videoProducerId: String? = null
    private var audioProducerId: String? = null

    private lateinit var endStreamPopup : BaseCustomDialog<ItemLayoutStreamEndBinding>
    private lateinit var commentAdapter : SimpleRecyclerViewAdapter<ReceivedData, ItemLayoutLiveCommentsBinding>




    var listData = ArrayList<String>()
    private var roomId : String ? = null
    private lateinit var mSocket: Socket

    var consumerModel: ConsumerModel? = null


    override fun onCreateView(view: View) {
        initView()
        initAdapter()
        initPopup()

        Handler(Looper.getMainLooper()).post{
            socketHandler()
            Handler(Looper.getMainLooper()).postDelayed({
                MediasoupClient.initialize(requireContext())
                mediaSoupDevice = Device()
                eglBaseContext = EglBase.create().eglBaseContext
                mediaCapture = MediaCapturer()
                val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isMicrophoneMute = false
                audioManager.isSpeakerphoneOn = true
                mediaCapture.initCamera(requireContext())
                joinRoom()

                receiveMessage()
                setupSocketListeners(requireContext())
            },200)


            initOnClick()
        }
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner){
            when(it?.id){
                R.id.ivBack , R.id.tvEndStream ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    SocketManager.mSocket?.disconnect()
                }

                R.id.ivSendChat->{
                    if (binding.etChat.text.toString().trim().isNullOrEmpty()){
                        showToast("Please enter message first")
                        return@observe
                    }
                    sendMessageSocket()
                }

                R.id.ivDropComment -> {

                    val isVisible = binding.rvChats.visibility == View.VISIBLE

                    // Toggle RecyclerView visibility
                    binding.rvChats.visibility = if (isVisible) View.GONE else View.VISIBLE

                    // Rotate icon
                    binding.ivDropComment.animate()
                        .rotation(if (isVisible) 0f else 180f)
                        .setDuration(200)
                        .start()
                }


            }
        }
    }

    private fun initAdapter() {
        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_live_comments, BR.bean){v,m, pos ->

        }
        binding.rvChats.adapter = commentAdapter

    }

    private fun sendMessageSocket(){
        val message = binding.etChat.text?.trim().toString()
        Log.d("SocketMessage", "Attempting to send message: $message")

        if (message.isEmpty()) {
            Log.w("SocketMessage", "Message is empty, not sending.")
            return
        }

        try {
            val jsonData = JSONObject()
            var userId  = sharedPrefManager.getUserId()

            if (roomId != null && userId != null) {
                jsonData.put("message", message)
                jsonData.put("userId", userId)
                jsonData.put("roomName", roomId)



            }

            Log.d("SocketMessage", "Constructed JSON: $jsonData")

            if (::mSocket.isInitialized && mSocket.connected()) {
                mSocket.emit("messageInLiveStreaming", jsonData)
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
    private fun receiveMessage(){
        mSocket.on("receiveMessageInLiveStreaming") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    Log.d("SocketMessage", "Received data: $data")

                    // ✅ Convert JSON to model using Gson
                    val receivedData = Gson().fromJson(data.toString(), ReceivedData::class.java)

                    requireActivity().runOnUiThread {
                        // ✅ Add the new message to your RecyclerView adapter
                        commentAdapter.addData(receivedData)
                        binding.rvChats.scrollToPosition(commentAdapter.itemCount - 1)
                    }

                } catch (e: Exception) {
                    Log.e("SocketMessage", "Error parsing message: ${e.localizedMessage}")
                    e.printStackTrace()
                }
            }
        }


    }



    private fun initPopup() {
        endStreamPopup = BaseCustomDialog(requireContext(),R.layout.item_layout_comsumer_popup, this )

    }

    private fun initView() {
        roomId = arguments?.getString("streamId")
        Log.i("dsdsa", "initView: $roomId")
    }
    private fun setupSocketListeners(context: Context) {
        SocketManager.mSocket?.on("admin-disconnected") {
            Log.w("SocketManager", "Received admin-disconnected")

            Handler(Looper.getMainLooper()).post {

                endStreamPopup.show()


                // Optional: Exit screen or show dialog
                (context as? Activity)?.finish()
            }
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_consumer_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun socketHandler() {
        val token = sharedPrefManager.getLoginData()?.token
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


//    private fun joinRoom() {
//        if (roomId != null){
//            SocketManager.mSocket?.emit("joinRoom", JSONObject().put(
//                "roomName", roomId
//            ), Ack { args ->
//                Log.i("dasdas", "mSocketJoin: ${(args[0] as JSONObject).toString()}")
//
//                joinRoomModel =
//                    BindingUtils.parseJson((args[0] as JSONObject).toString())
//                if (joinRoomModel != null) {
//                    mediaSoupDevice.load(
//                        (args[0] as JSONObject).getJSONObject("rtpCapabilities")
//                            .toString(), null
//                    )
//                    joinRoomModel?.roomProducers?.videoProducer?.let { listData.add(it) }
//                    joinRoomModel?.roomProducers?.audioProducer?.let { listData.add(it) }
//
//                    Log.i("fdsfsd", "joinRoom: $listData")
//
//                    CoroutineScope(Dispatchers.IO).launch {
//                        for (i in listData.indices) {
//                            createWebRtcTransportAndConsume(i)
//                        }
//
//                    }
//                }
//
//            })
//        }
//
//    }



    private fun joinRoom() {
        if (roomId != null) {
            SocketManager.mSocket?.emit("joinRoom", JSONObject().put("roomName", roomId), Ack { args ->
                Log.i("SocketEvent", "JoinRoom Ack Received: ${(args[0] as JSONObject).toString()}")

                joinRoomModel = BindingUtils.parseJson((args[0] as JSONObject).toString())
                if (joinRoomModel != null) {
                    mediaSoupDevice.load(
                        (args[0] as JSONObject).getJSONObject("rtpCapabilities").toString(), null
                    )

                    // Store producer IDs
                    videoProducerId = joinRoomModel?.roomProducers?.videoProducer
                    audioProducerId = joinRoomModel?.roomProducers?.audioProducer

                    Log.i("ProducerID", "Video: $videoProducerId")
                    Log.i("ProducerID", "Audio: $audioProducerId")

                    CoroutineScope(Dispatchers.IO).launch {
                        videoProducerId?.let {
                            Log.i("ConsumeFlow", "🔴 Starting Video Consumption")
                            createWebRtcTransportAndConsume(it)
                        }

                        delay(10_000)

                        audioProducerId?.let {
                            Log.i("ConsumeFlow", "🔊 Starting Audio Consumption")
                            createWebRtcTransportAndConsume(it)
                        }
                    }
                } else {
                    Log.e("JoinRoom", "❌ Failed to parse joinRoomModel")
                }
            })
        } else {
            Log.e("JoinRoom", "❌ roomId is null, can't join room")
        }
    }





//    private suspend fun createWebRtcTransportAndConsume(producerId: String): ConsumerModel? {
//        return suspendCancellableCoroutine { continuation ->
//            SocketManager.mSocket?.emit(
//                "createWebRtcTransport",
//                JSONObject().put("consumer", true),
//                Ack { args ->
//                    Log.i("adadasds", "mSocketJoin: ${(args[0] as JSONObject).toString()}")
//                    if (args.isNotEmpty()) {
//                        consumerModel =
//                            BindingUtils.parseJson((args[0] as JSONObject).toString())
//                        if (consumerModel != null) {
//                            val listener: RecvTransport.Listener = object : RecvTransport.Listener {
//                                override fun onConnect(transport: Transport, dtlsParameters: String) {
//                                    try {
//                                        val jsonObject33 = JSONObject().apply {
//                                            put("dtlsParameters", JSONObject(dtlsParameters)) // ✅ convert string to object
//                                            put("serverConsumerTransportId", consumerModel?.params?.id)
//                                        }
//
//                                        Log.i("daata", "onConnect: $jsonObject33")
//
//                                        SocketManager.mSocket!!.emit("transport-recv-connect", jsonObject33)
//
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                        Log.e("WebRTC", "Failed to send dtlsParameters", e)
//                                    }
//                                }
//
//
//                                override fun onConnectionStateChange(
//                                    transport: Transport, newState: String
//                                ) {
//                                    // Handle connection state change if necessary
//                                }
//                            }
//
//
//
//                            val jsonObject32223 = JSONObject().apply {
//                                put(
//                                    "rtpCapabilities", mediaSoupDevice.rtpCapabilities
//                                )
//                                put("remoteProducerId", producerId)
//                                put(
//                                    "serverConsumerTransportId", consumerModel?.params?.id
//                                )
//                            }
//
//                            Log.i("daata", "jsonObject32223: $jsonObject32223")
//
//                            Log.i("RecvConnect", "serverConsumerTransportId: ${consumerModel?.params?.id}")
//
//                            Log.i("Computer2222222", "consume1")
//                            // Emit the "consume" event and handle the result
//
//                            // Create the receiving transport
//                            val mRecvTransport = mediaSoupDevice.createRecvTransport(
//                                listener,
//                                consumerModel?.params?.id!!,
//                                Gson().toJson(consumerModel!!.params?.iceParameters).toString(),
//                                Gson().toJson(consumerModel!!.params?.iceCandidates).toString(),
//                                Gson().toJson(consumerModel!!.params?.dtlsParameters).toString()
//                            )
//
//
//                            try {
//                                SocketManager.mSocket!!.emit("consume", jsonObject32223, Ack { args22 ->
//                                    Log.i("dasdas", "mSocketJoinss: ${(args22[0] as JSONObject).toString()}")
//                                    if (args22.isNotEmpty()) {
//                                        val jsonObject =
//                                            JSONObject((args22[0] as JSONObject).toString())
//                                        val kind = jsonObject.getJSONObject(
//                                            "params"
//                                        ).getString("kind")
//
//
//                                        // Parse consumer model based on media kind
//                                        if (kind == "video") {
//                                            cconsumerTransport = BindingUtils.parseJson(
//                                                (args22[0] as JSONObject).toString()
//                                            )
//                                        } else {
//                                            cconsumerTransportAudio = BindingUtils.parseJson(
//                                                (args22[0] as JSONObject).toString()
//                                            )
//                                        }
//
////                                    val listener = Consumer.Listener { consumer: Consumer? ->
////
////                                    }
//
//                                        // Process media based on the type (video/audio)
//                                        if (kind == "video") {
//                                            if (cconsumerTransport != null) {
//                                                val kindConsumer = mRecvTransport.consume(
//                                                    { _: Consumer? ->
//
//                                                    },
//                                                    cconsumerTransport?.params?.id,
//                                                    cconsumerTransport?.params?.producerId,
//                                                    cconsumerTransport?.params?.kind,
//                                                    Gson().toJson(
//                                                        cconsumerTransport?.params?.rtpParameters
//                                                    ).toString()
//                                                )
////                                        consumerLIstData.add(kindConsumer)
//
//                                                Handler(Looper.getMainLooper()).post {
//                                                    Log.i("Computer2222222", "ADDDFUNTION2")
////                                            consumerList.add(cconsumerTransport?.params?.producerId!!)
////                                            chatAdapter.addData(
////                                                ViewForMyLiveSubscription2(
////                                                    kindConsumer,
////                                                    eglBaseContext,
////                                                    mRecvTransport
////                                                )
////                                            )
//
//                                                    binding.publisherContainer.post {
//                                                        binding.publisherContainer.init(
//                                                            eglBaseContext,
//                                                            null
//                                                        )
//                                                        val videoTrack =
//                                                            kindConsumer.getTrack() as VideoTrack
//                                                        videoTrack.setEnabled(true)
//                                                        videoTrack.addSink(binding.publisherContainer)
//
//                                                    }
//
//                                                }
//
//                                                val jsonObject333 = JSONObject().apply {
//                                                    put(
//                                                        "serverConsumerId",
//                                                        cconsumerTransport?.params?.serverConsumerId
//                                                    )
//                                                }
//
//                                                Log.i("daata", "jsonObject333: $jsonObject333")
//
//                                                SocketManager.mSocket!!.emit(
//                                                    "consumer-resume", jsonObject333
//                                                )
//                                            }
//
//
//                                        } else {
//                                            if (cconsumerTransportAudio != null) {
//                                                val kindConsumer = mRecvTransport.consume(
//                                                    { _: Consumer? ->
//
//                                                    },
//                                                    cconsumerTransportAudio?.params?.id,
//                                                    cconsumerTransportAudio?.params?.producerId,
//                                                    cconsumerTransportAudio?.params?.kind,
//                                                    Gson().toJson(
//                                                        cconsumerTransportAudio?.params?.rtpParameters
//                                                    ).toString()
//                                                )
//                                                //   consumerLIstData.add(kindConsumer)
//                                                val jsonObject333 = JSONObject().apply {
//                                                    put(
//                                                        "serverConsumerId",
//                                                        cconsumerTransportAudio?.params?.serverConsumerId
//                                                    )
//                                                }
//
//                                                SocketManager.mSocket!!.emit(
//                                                    "consumer-resume", jsonObject333
//                                                )
//
//                                                audioTrack2 = kindConsumer.track as AudioTrack
//                                                audioTrack2.setEnabled(
//                                                    true
//                                                )
//                                            }
//
//                                        }
//                                        continuation.resume(
//                                            consumerModel
//                                        )
//                                    }
//
//                                })
//                            }catch (e : Exception){
//                                e.printStackTrace()
//                                Log.i("sadasdas", "createWebRtcTransportAndConsume: ${e.printStackTrace()}")
//                            }
//
//                        }
//
//
//                    }else{
//                        showToast("args is empty")
//                    }
//
//                })
//        }
//    }




    private suspend fun createWebRtcTransportAndConsume(producerId: String): ConsumerModel? {
        return suspendCancellableCoroutine { continuation ->
            Log.i("WebRTC", "🔹 Starting createWebRtcTransportAndConsume for producerId: $producerId")

            // Step 1: Ask backend to create consumer recv transport
            SocketManager.mSocket?.emit(
                "createWebRtcTransport",
                JSONObject().put("consumer", true),
                Ack { args ->
                    Log.i("WebRTC", "🟢 Received createWebRtcTransport response: $args")

                    if (args.isNullOrEmpty()) {
                        showToast("Failed to create transport: no response")
                        Log.e("WebRTC", "❌ createWebRtcTransport: args is empty")
                        continuation.resume(null)
                        return@Ack
                    }

                    // Step 2: Parse transport info
                    consumerModel = BindingUtils.parseJson((args[0] as JSONObject).toString())
                    Log.i("WebRTC", "✅ Parsed consumerModel: $consumerModel")

                    if (consumerModel == null) {
                        showToast("Failed to parse transport info")
                        Log.e("WebRTC", "❌ consumerModel is null")
                        continuation.resume(null)
                        return@Ack
                    }

                    // Step 3: Create recv transport listener
                    val listener = object : RecvTransport.Listener {
                        override fun onConnect(transport: Transport, dtlsParameters: String) {
                            try {
                                Log.i("WebRTC", "🔗 onConnect called with DTLS: $dtlsParameters")
                                val dtlsJson = JSONObject().apply {
                                    put("dtlsParameters", JSONObject(dtlsParameters))
                                    put("serverConsumerTransportId", consumerModel?.params?.id)
                                }

                                // Step 4: Send DTLS info to backend
                                Log.i("WebRTC", "📡 Emitting 'transport-recv-connect' → $dtlsJson")
                                SocketManager.mSocket?.emit("transport-recv-connect", dtlsJson)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("WebRTC", "❌ onConnect exception: ${e.message}")
                            }
                        }

                        override fun onConnectionStateChange(transport: Transport, newState: String) {
                            Log.i("WebRTC", "🔄 RecvTransport state changed: $newState")
                        }
                    }

                    // Step 5: Create RecvTransport
                    val recvTransport = try {
                        Log.i("WebRTC", "🛠️ Creating recvTransport with ID: ${consumerModel?.params?.id}")
                        mediaSoupDevice.createRecvTransport(
                            listener,
                            consumerModel?.params?.id!!,
                            Gson().toJson(consumerModel!!.params?.iceParameters),
                            Gson().toJson(consumerModel!!.params?.iceCandidates),
                            Gson().toJson(consumerModel!!.params?.dtlsParameters)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("WebRTC", "❌ Failed to create RecvTransport: ${e.message}")
                        continuation.resume(null)
                        return@Ack
                    }

                    Log.i("fdsfsdfsd", "createWebRtcTransportAndConsume: ${mediaSoupDevice.rtpCapabilities}")


                    // Step 6: Emit consume request
                    val consumePayload = JSONObject().apply {
                        put("rtpCapabilities", JSONObject(mediaSoupDevice.rtpCapabilities)) // ✅ parse into object
                        put("remoteProducerId", producerId)
                        put("serverConsumerTransportId", consumerModel?.params?.id)
                    }

                    Log.i("WebRTC", "📡 Emitting 'consume' → $consumePayload")
                    SocketManager.mSocket?.emit("consume", consumePayload, Ack { consumeArgs ->
                        Log.i("WebRTC", "🟢 Received consume response: $consumeArgs")

                        if (consumeArgs.isNullOrEmpty()) {
                            showToast("Consume failed: empty response")
                            Log.e("WebRTC", "❌ Consume ack is empty")
                            continuation.resume(null)
                            return@Ack
                        }

                        val consumeResponse = JSONObject(consumeArgs[0].toString())
                        val kind = consumeResponse.getJSONObject("params").getString("kind")
                        Log.i("WebRTC", "📦 Consuming kind: $kind")

                        val consumer = recvTransport.consume(
                            { _: Consumer? -> },
                            consumeResponse.getJSONObject("params").getString("id"),
                            consumeResponse.getJSONObject("params").getString("producerId"),
                            kind,
                            consumeResponse.getJSONObject("params").getJSONObject("rtpParameters").toString()
                        )

                        // Step 7: Handle video/audio track
                        if (kind == "video") {
                            cconsumerTransport = BindingUtils.parseJson(consumeResponse.toString())
                            Log.i("WebRTC", "🎥 Handling video consumer: ${cconsumerTransport?.params?.id}")

                            Handler(Looper.getMainLooper()).post {
                                binding.publisherContainer.post {
                                    binding.publisherContainer.init(eglBaseContext, null)
                                    val videoTrack = consumer.track as VideoTrack
                                    videoTrack.setEnabled(true)
                                    videoTrack.addSink(binding.publisherContainer)
                                }
                            }

                            val resumeJson = JSONObject().put("serverConsumerId", cconsumerTransport?.params?.serverConsumerId)
                            Log.i("WebRTC", "📡 Emitting 'consumer-resume' → $resumeJson")
                            SocketManager.mSocket?.emit("consumer-resume", resumeJson)

                        } else if (kind == "audio") {
                            cconsumerTransportAudio = BindingUtils.parseJson(consumeResponse.toString())
                            Log.i("WebRTC", "🎧 Handling audio consumer: ${cconsumerTransportAudio?.params?.id}")

                            audioTrack2 = consumer.track as AudioTrack
                            audioTrack2.setEnabled(true)

                            val resumeJson = JSONObject().put("serverConsumerId", cconsumerTransportAudio?.params?.serverConsumerId)
                            Log.i("WebRTC", "📡 Emitting 'consumer-resume' → $resumeJson")
                            SocketManager.mSocket?.emit("consumer-resume", resumeJson)
                        }

                        Log.i("WebRTC", "✅ Consumer successfully created and resumed")
                        continuation.resume(consumerModel)
                    })
                }
            )
        }
    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvEnded ->{
                endStreamPopup.dismiss()
                // Disconnect socket
                SocketManager.mSocket?.disconnect()

                // Go back
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)

            }
        }
    }


}