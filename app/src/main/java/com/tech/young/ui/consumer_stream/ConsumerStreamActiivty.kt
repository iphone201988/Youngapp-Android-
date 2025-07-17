package com.tech.young.ui.consumer_stream

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.tech.MediaCapturer
import com.tech.buckChats.ui.media_soup_webtrc.consumerTransport
import com.tech.buckChats.ui.media_soup_webtrc.consumerTransportAudio
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.data.model.ConsumerJoinModel
import com.tech.young.data.model.ConsumerModel
import com.tech.young.data.model.CreateTransportModel
import com.tech.young.data.model.JoinRoomModel
import com.tech.young.data.model.ProdcerModel
import com.tech.young.databinding.ActivityConsumerStreamActiivtyBinding
import com.tech.young.ui.streaming_activity.StreamActivityVm
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
class ConsumerStreamActiivty : BaseActivity<ActivityConsumerStreamActiivtyBinding>() {

    private val viewModel: StreamActivityVm by viewModels()
    private lateinit var mediaSoupDevice: Device
    private var joinRoomModel: ConsumerJoinModel? = null
    private var createTransportModel: CreateTransportModel? = null
    private var cconsumerTransport: consumerTransport? = null
    private var cconsumerTransportAudio: consumerTransportAudio? = null
    private lateinit var mediaCapture: MediaCapturer
    private lateinit var eglBaseContext: EglBase.Context
    private lateinit var audioTrack2: AudioTrack
    var listData = ArrayList<String>()

    var consumerModel: ConsumerModel? = null


    override fun getLayoutResource(): Int {
        return R.layout.activity_consumer_stream_actiivty
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }
    private lateinit var mSocket: Socket

    override fun onCreateView() {
        Handler(Looper.getMainLooper()).post{
            socketHandler()
            Handler(Looper.getMainLooper()).postDelayed({
                MediasoupClient.initialize(this@ConsumerStreamActiivty)
                mediaSoupDevice = Device()
                eglBaseContext = EglBase.create().eglBaseContext
                mediaCapture = MediaCapturer()
                val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isMicrophoneMute = false
                audioManager.isSpeakerphoneOn = true
                mediaCapture.initCamera(this)
                joinRoom()
            },200)

        }
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

    private fun joinRoom() {
        SocketManager.mSocket?.emit("joinRoom", JSONObject().put(
            "roomName", "karan"
        ), Ack { args ->
            Log.i("dasdas", "mSocketJoin: ${(args[0] as JSONObject).toString()}")

            joinRoomModel =
                BindingUtils.parseJson((args[0] as JSONObject).toString())
            if (joinRoomModel != null) {
                mediaSoupDevice.load(
                    (args[0] as JSONObject).getJSONObject("rtpCapabilities")
                        .toString(), null
                )
                joinRoomModel?.roomProducers?.videoProducer?.let { listData.add(it) }
                joinRoomModel?.roomProducers?.audioProducer?.let { listData.add(it) }

                Log.i("fdsfsd", "joinRoom: $listData")

                CoroutineScope(Dispatchers.IO).launch {
                    for (i in listData.indices) {
//                        createWebRtcTransportAndConsume(i)
                    }

                }
            }

        })
    }

//    private fun createTransport( i:Int) {


//    private suspend fun createTransport(i: Int): ConsumerModel? {
//        return suspendCancellableCoroutine { continuation ->
//
//            continuation.resume(consumerModel)
//
//        }
//
//
//    }




//    private suspend fun createWebRtcTransportAndConsume(i: Int): ConsumerModel? {
//        return suspendCancellableCoroutine { continuation ->
//    SocketManager.mSocket?.emit(
//    "createWebRtcTransport",
//    JSONObject().put("consumer", true),
//    Ack { args ->
//        Log.i("adadasds", "mSocketJoin: ${(args[0] as JSONObject).toString()}")
//        if (args.isNotEmpty()) {
//            consumerModel =
//                BindingUtils.parseJson((args[0] as JSONObject).toString())
//            if (consumerModel != null) {
//                val listener: RecvTransport.Listener = object : RecvTransport.Listener {
//                    override fun onConnect(
//                        transport: Transport, dtlsParameters: String
//                    ) {
//                        val jsonObject33 = JSONObject().apply {
//                            put("dtlsParameters", dtlsParameters)
//                            put(
//                                "serverConsumerTransportId", consumerModel?.params?.id
//                            )
//                        }
//
//                        Log.i("daata", "onConnect: $jsonObject33")
//
//                        SocketManager.mSocket!!.emit(
//                            "transport-recv-connect", jsonObject33
//                        )
//                    }
//
//                    override fun onConnectionStateChange(
//                        transport: Transport, newState: String
//                    ) {
//                        // Handle connection state change if necessary
//                    }
//                }
//                // Create the receiving transport
//                val mRecvTransport = mediaSoupDevice.createRecvTransport(
//                    listener,
//                    consumerModel?.params?.id!!,
//                    Gson().toJson(consumerModel!!.params?.iceParameters).toString(),
//                    Gson().toJson(consumerModel!!.params?.iceCandidates).toString(),
//                    Gson().toJson(consumerModel!!.params?.dtlsParameters).toString()
//                )
//
//                val jsonObject32223 = JSONObject().apply {
//                    put(
//                        "rtpCapabilities", mediaSoupDevice.rtpCapabilities
//                    )
//                    put("remoteProducerId", listData[i])
//                    put(
//                        "serverConsumerTransportId", consumerModel?.params?.id
//                    )
//                }
//
//                Log.i("daata", "jsonObject32223: $jsonObject32223")
//
//
//                Log.i("Computer2222222", "consume1")
//                // Emit the "consume" event and handle the result
//                SocketManager.mSocket!!.emit("consume", jsonObject32223, Ack { args22 ->
//                    Log.i("dasdas", "mSocketJoinss: ${(args22[0] as JSONObject).toString()}")
//                    if (args22.isNotEmpty()) {
//                        val jsonObject =
//                            JSONObject((args22[0] as JSONObject).toString())
//                        val kind = jsonObject.getJSONObject(
//                            "params"
//                        ).getString("kind")
//
//
//                        // Parse consumer model based on media kind
//                        if (kind == "video") {
//                            cconsumerTransport = BindingUtils.parseJson(
//                                (args22[0] as JSONObject).toString()
//                            )
//                        } else {
//                            cconsumerTransportAudio = BindingUtils.parseJson(
//                                (args22[0] as JSONObject).toString()
//                            )
//                        }
//
////                                    val listener = Consumer.Listener { consumer: Consumer? ->
////
////                                    }
//
//                        // Process media based on the type (video/audio)
//                        if (kind == "video") {
//                            if (cconsumerTransport != null) {
//                                val kindConsumer = mRecvTransport.consume(
//                                    { _: Consumer? ->
//
//                                    },
//                                    cconsumerTransport?.params?.id,
//                                    cconsumerTransport?.params?.producerId,
//                                    cconsumerTransport?.params?.kind,
//                                    Gson().toJson(
//                                        cconsumerTransport?.params?.rtpParameters
//                                    ).toString()
//                                )
////                                        consumerLIstData.add(kindConsumer)
//
//                                Handler(Looper.getMainLooper()).post {
//                                    Log.i("Computer2222222", "ADDDFUNTION2")
////                                            consumerList.add(cconsumerTransport?.params?.producerId!!)
////                                            chatAdapter.addData(
////                                                ViewForMyLiveSubscription2(
////                                                    kindConsumer,
////                                                    eglBaseContext,
////                                                    mRecvTransport
////                                                )
////                                            )
//
//                                    binding.publisherContainer.post {
//                                        binding.publisherContainer.init(
//                                            eglBaseContext,
//                                            null
//                                        )
//                                        val videoTrack =
//                                            kindConsumer.getTrack() as VideoTrack
//                                        videoTrack.setEnabled(true)
//                                        videoTrack.addSink(binding.publisherContainer)
//
//                                    }
//
//                                }
//
//                                val jsonObject333 = JSONObject().apply {
//                                    put(
//                                        "serverConsumerId",
//                                        cconsumerTransport?.params?.serverConsumerId
//                                    )
//                                }
//
//                                Log.i("daata", "jsonObject333: $jsonObject333")
//
//                                SocketManager.mSocket!!.emit(
//                                    "consumer-resume", jsonObject333
//                                )
//                            }
//
//
//                        } else {
//                            if (cconsumerTransportAudio != null) {
//                                val kindConsumer = mRecvTransport.consume(
//                                    { _: Consumer? ->
//
//                                    },
//                                    cconsumerTransportAudio?.params?.id,
//                                    cconsumerTransportAudio?.params?.producerId,
//                                    cconsumerTransportAudio?.params?.kind,
//                                    Gson().toJson(
//                                        cconsumerTransportAudio?.params?.rtpParameters
//                                    ).toString()
//                                )
//                                //   consumerLIstData.add(kindConsumer)
//                                val jsonObject333 = JSONObject().apply {
//                                    put(
//                                        "serverConsumerId",
//                                        cconsumerTransportAudio?.params?.serverConsumerId
//                                    )
//                                }
//
//                                SocketManager.mSocket!!.emit(
//                                    "consumer-resume", jsonObject333
//                                )
//
//                                audioTrack2 = kindConsumer.track as AudioTrack
//                                audioTrack2.setEnabled(
//                                    true
//                                )
//                            }
//
//                        }
//                        continuation.resume(
//                            consumerModel
//                        )
//                    }
//
//                })
//            }
//
//
//        }else{
//            showToast("args is empty")
//        }
//
//    })
//        }
//    }
}