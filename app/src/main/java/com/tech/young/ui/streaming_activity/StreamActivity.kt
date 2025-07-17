package com.tech.young.ui.streaming_activity

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
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.gson.Gson
import com.tech.MediaCapturer
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.data.model.CreateTransportModel
import com.tech.young.data.model.JoinRoomModel
import com.tech.young.data.model.transprtProduceModel
import com.tech.young.databinding.ActivityStreamBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject
import org.mediasoup.droid.Device
import org.mediasoup.droid.MediasoupClient
import org.mediasoup.droid.Producer
import org.mediasoup.droid.SendTransport
import org.mediasoup.droid.Transport
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.util.Timer
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.log

@AndroidEntryPoint
class StreamActivity : BaseActivity<ActivityStreamBinding>() {

    private val viewModel : StreamActivityVm by viewModels()
    private lateinit var mediaSoupDevice: Device
    private lateinit var mediaCapture: MediaCapturer
    private lateinit var eglBaseContext: EglBase.Context
    private lateinit var videoTrack: VideoTrack

    private var joinRoomModel: JoinRoomModel? = null
    private var createTransportModel: CreateTransportModel? = null
    private var producerAudio: Producer? = null
    private var producerVideo: Producer? = null
    private lateinit var mSocket: Socket


    override fun getLayoutResource(): Int {
        return R.layout.activity_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
       initializePlayer()

    }

    /**** video play function ***/
    private fun initializePlayer() {
      var   player = ExoPlayer.Builder(this) // <- context
            .build()
        // create a media item.
        val mediaItem =
            MediaItem.Builder().setUri("https://youngappbucket.s3.us-east-2.amazonaws.com/recordings/recording_6874e4e0b21418a1842b42ee_qrQMbTYHckGS9CWGAAAF_2025-07-14T11-07-15-591Z.mkv").setMimeType(MimeTypes.APPLICATION_MP4).build()
        // Create a media source and pass the media item
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(this) // <- context
        ).createMediaSource(mediaItem)
        // Finally assign this media source to the player
        player!!.apply {
            setMediaSource(mediaSource)
            playWhenReady = true // start playing when the exoplayer has setup
            seekTo(0, 0L) // Start from the beginning
            prepare()
            play()// Change the state from idle.
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    /*       if (isPlaying) {
                               hideLoading()
                           } else {
                               if (stopProgress == 1) {
                                   hideLoading()
                                   stopProgress = 0
                               } else {
                                   showLoading()
                               }
                           }*/
                    hideLoading()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (playWhenReady) {
                        // Video is playing, hide the loading indicator
                        hideLoading()
                    } else {

                        // Video is paused, show the loading indicator
                        hideLoading()
                    }
                }
            })
        }.also {
            // Do not forget to attach the player to the view
            binding.playerView.player = it
        }

    }

    /**** on stop methid call ***/
    override fun onStop() {
        super.onStop()
//        player!!.release()
//        player = null
    }


}