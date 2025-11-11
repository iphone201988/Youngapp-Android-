package com.tech.young.ui.stream_screen

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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.tech.MediaCapturer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.showToast
import com.tech.young.data.model.CreateTransportModel
import com.tech.young.data.model.JoinRoomModel
import com.tech.young.data.model.ReceivedData
import com.tech.young.data.model.transprtProduceModel
import com.tech.young.databinding.FragmentLiveStreamBinding
import com.tech.young.databinding.ItemLayoutLiveCommentsBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.databinding.ItemLayoutStreamEndBinding
import com.tech.young.databinding.ItemLayoutStreamPopupBinding
import com.tech.young.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONObject
import org.mediasoup.droid.Device
import org.mediasoup.droid.MediasoupClient
import org.mediasoup.droid.Producer
import org.mediasoup.droid.SendTransport
import org.mediasoup.droid.Transport
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.util.concurrent.CompletableFuture

@AndroidEntryPoint
class LiveStreamFragment : BaseFragment<FragmentLiveStreamBinding>() ,BaseCustomDialog.Listener {

    private var roomId : String ? = null
    private val viewModel: StreamVM by viewModels()
    private lateinit var mediaSoupDevice: Device
    private lateinit var mediaCapture: MediaCapturer
    private lateinit var eglBaseContext: EglBase.Context
    private lateinit var videoTrack: VideoTrack

    private lateinit var commentAdapter : SimpleRecyclerViewAdapter<ReceivedData, ItemLayoutLiveCommentsBinding>

    private var joinRoomModel: JoinRoomModel? = null
    private var createTransportModel: CreateTransportModel? = null
    private var producerAudio: Producer? = null
    private var producerVideo: Producer? = null
    private lateinit var mSocket: Socket
    private lateinit var endStreamPopup : BaseCustomDialog<ItemLayoutStreamEndBinding>


    override fun onCreateView(view: View) {
        initView()
        initPopup()
        initAdapter()
        Handler(Looper.getMainLooper()).post{
            socketHandler()
            Handler(Looper.getMainLooper()).postDelayed({
                MediasoupClient.initialize(requireContext())
                mediaSoupDevice = Device()
                initOnClick()
                initializeTheLocalView()
                socketJoin()


                receiveMessage()
            },200)
        }
    }

    private fun initAdapter() {
        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_live_comments, BR.bean){v,m, pos ->

        }
        binding.rvChats.adapter = commentAdapter

    }

    private fun initPopup() {
        endStreamPopup = BaseCustomDialog(requireContext(),R.layout.item_layout_stream_end, this )
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
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer observe@{
            when(it?.id){
                R.id.tvEndStream ->{
                    endStreamPopup.show()
                }

                R.id.ivBack ->{
                    // Disconnect socket
                    endStreamPopup.show()
                //    SocketManager.mSocket?.disconnect()

                    // Go back
              //      requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.ivSendChat->{
                    if (binding.etChat.text.toString().trim().isNullOrEmpty()){
                        showToast("Please enter message first")
                        return@observe
                    }
                    sendMessageSocket()
                }
            }
        })
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
    private fun initView() {
        roomId = arguments?.getString("room_id")
        Log.i("dsdsa", "initView: $roomId")
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

    override fun getLayoutResource(): Int {
        return R.layout.fragment_live_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initializeTheLocalView() {
        mediaCapture = MediaCapturer()
        mediaCapture.initCamera(requireContext())
        eglBaseContext = EglBase.create().eglBaseContext
        binding.publisherContainer.init(eglBaseContext, null)
        videoTrack = mediaCapture.createVideoTrack(requireContext(), binding.publisherContainer, eglBaseContext)
        videoTrack.addSink(binding.publisherContainer)
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isMicrophoneMute = false
        audioManager.isSpeakerphoneOn = true
    }

    private fun socketJoin() {
        if (roomId != null){
            SocketManager.mSocket?.emit("joinRoom", JSONObject().put(
                "roomName", roomId
            ), Ack { args ->
                Log.i("dasdas", "socketJoin: ${(args[0] as JSONObject).toString()}")

                joinRoomModel =
                    BindingUtils.parseJson((args[0] as JSONObject).toString())
                if (joinRoomModel != null){
                    mediaSoupDevice.load(
                        (args[0] as JSONObject).getJSONObject("rtpCapabilities")
                            .toString(), null
                    )
                    createTrasport()
                }

            })
        }

    }

    fun produceTransport(
        kind: String,
        rtpParameters: String, // this is a JSON string
        appData: String
    ): CompletableFuture<transprtProduceModel> {

        val completableFuture = CompletableFuture<transprtProduceModel>()

        try {
            val rtpJson = JSONObject(rtpParameters) // ✅ convert string to JSONObject
            val appDataJson = JSONObject(appData)   // ✅ if appData is also JSON string

            val messageTemp = JSONObject().apply {
                put("kind", kind)
                put("rtpParameters", rtpJson) // ✅ send as JSON object
                put("appData", appDataJson)
            }


            SocketManager.mSocket?.emit("transport-produce", messageTemp, Ack { args ->
                val responseJson = args[0] as JSONObject
                val transportProduceModel: transprtProduceModel? =
                    BindingUtils.parseJson(responseJson.toString())
                completableFuture.complete(transportProduceModel)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            completableFuture.completeExceptionally(e)
        }

        return completableFuture
    }


    fun produceTransport2(
        transport: Transport,
        sctpStreamParameters: String,
        label: String,
        protocol: String,
        appData: String,
    ): CompletableFuture<transprtProduceModel> {
        val completableFuture = CompletableFuture<transprtProduceModel>()

        val messageTemp = JSONObject().apply {
            put("transport", transport)
            put("sctpStreamParameters", sctpStreamParameters)
            put("label", label)
            put("protocol", protocol)
            put("appData", appData)
        }

        SocketManager.mSocket?.emit("produceData", messageTemp, Ack { _ ->
//            val responseJson = args[0] as JSONObject
//            try {
//                val responseJson = args[0] as JSONObject
//                val transportProduceModel: transprtProduceModel? =
//                    ImageViewBindingUtils.parseJson(responseJson.toString())
//                completableFuture.complete(transportProduceModel)
//            } catch (e: Exception) {

////                completableFuture.completeExceptionally(e)
//            }
        })

        return completableFuture
    }




    private fun createTrasport() {
        Log.i("WebRTC", "Emitting createWebRtcTransport...")

        SocketManager.mSocket?.emit("createWebRtcTransport",
            JSONObject().put(
                "consumer", false
            ),
            Ack { argsProducer ->

                Log.i("WebRTC", "Received response for createWebRtcTransport")


                createTransportModel =
                    BindingUtils.parseJson((argsProducer[0] as JSONObject).toString())
                Log.i("dasdas", "CreateTransport: ${(argsProducer[0] as JSONObject).toString()}")
                Log.i("WebRTC", "Parsed createTransportModel: $createTransportModel")

                if (createTransportModel != null){
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            Log.i("WebRTC", "Starting SendTransport creation")

                            val listener: SendTransport.Listener =
                                object : SendTransport.Listener {
                                    override fun onConnect(
                                        transport: Transport,
                                        dtlsParameters: String
                                    ) {
                                        Log.i("WebRTC", "onConnect called with DTLS parameters")

                                        try {
                                            val dtlsJson = JSONObject(dtlsParameters) // ✅ convert to JSON
                                            val connectPayload = JSONObject().apply {
                                                put("dtlsParameters", dtlsJson) // ✅ send as nested object
                                            }

                                            SocketManager.mSocket?.emit("transport-connect", connectPayload)
                                            Log.i("WebRTC", "Sent transport-connect: $connectPayload")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Log.e("WebRTC", "Failed to send dtlsParameters", e)
                                        }
                                    }


                                    override fun onProduce(
                                        transport: Transport,
                                        kind: String,
                                        rtpParameters: String,
                                        appData: String
                                    ): String {

                                        Log.i("WebRTC", "onProduce triggered for kind: $kind")
                                        return produceTransport(
                                            kind,
                                            rtpParameters ,
                                            appData
//
                                        ).get().id

                                    }

                                    override fun onProduceData(
                                        transport: Transport?,
                                        sctpStreamParameters: String?,
                                        label: String?,
                                        protocol: String?,
                                        appData: String?
                                    ): String {
                                        Log.i("WebRTC", "onProduceData triggered")

                                        return produceTransport2(
                                            transport!!,
                                            sctpStreamParameters!!,
                                            label!!,
                                            protocol!!,
                                            appData!!
                                        ).get().id
                                    }

                                    override fun onConnectionStateChange(
                                        transport: Transport,
                                        newState: String
                                    ) {
                                        Log.i("WebRTC", "Connection state changed: $newState")

                                    }
                                }

                            val mSendTransport =
                                createTransportModel!!.params?.id?.let {
                                    Log.i("WebRTC", "Calling createSendTransport...")

                                    mediaSoupDevice.createSendTransport(
                                        listener,
                                        it,
                                        Gson().toJson(
                                            createTransportModel!!.params?.iceParameters
                                        ).toString(),
                                        Gson().toJson(
                                            createTransportModel!!.params?.iceCandidates
                                        ).toString(),
                                        Gson().toJson(
                                            createTransportModel!!.params?.dtlsParameters
                                        ).toString(),
                                    )
                                }

                            Log.i("WebRTC", "Producing audio...")

                            producerAudio = mSendTransport?.produce(
                                { _ ->

                                },
                                mediaCapture.createAudioTrack(),
                                null,
                                null,
                                null,
                            )
                            Log.i("WebRTC", "Producing video...")

                            producerVideo = mSendTransport?.produce(
                                { _ ->

                                },
                                videoTrack,

                                null,
                                null,
                                null,
                            )
                            Log.i("WebRTC", "Transport and producers setup complete")

//                            val timer = Timer()
//                            timer.scheduleAtFixedRate(0, 5000) {
//                                Log.i("fdsfds", "Task executed at: ${producerVideo?.stats}")
//                            }




////




                        }, 0
                    )
                }

            })
    }

    override fun onViewClick(view: View?) {
        when (view?.id) {
            R.id.tvYes -> {
                // ✅ Emit event to notify server
                //    SocketManager.mSocket?.emit("admin-disconnected")

                // Disconnect socket
                SocketManager.mSocket?.disconnect()

                // Go back
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)

                // Close dialog
                endStreamPopup.dismiss()
            }
        }

    }
}