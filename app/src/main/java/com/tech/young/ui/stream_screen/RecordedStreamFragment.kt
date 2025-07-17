package com.tech.young.ui.stream_screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.data.api.Constants
import com.tech.young.databinding.FragmentRecordedStreamBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordedStreamFragment : BaseFragment<FragmentRecordedStreamBinding>() {

    private val viewModel : StreamVM by viewModels()

    private var streamUrl : String ? = null
    private var player: ExoPlayer? = null

    override fun onCreateView(view: View) {
        iniView()
        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun iniView() {
        streamUrl = arguments?.getString("streamUrl")
        Log.i("dsaasdas", "iniView: $streamUrl")

        if (streamUrl != null){
            initializePlayer()
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_recorded_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }




    private fun initializePlayer() {
        try {
            val fullUrl = Constants.BASE_URL_IMAGE + (streamUrl ?: "")
            if (streamUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Stream URL is empty", Toast.LENGTH_SHORT).show()
                return
            }

            val mediaItem = MediaItem.Builder()
                .setUri(fullUrl)
                .setMimeType(MimeTypes.APPLICATION_MP4) // Make sure it's actually MP4
                .build()

            player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->

                val mediaSource = ProgressiveMediaSource.Factory(
                    DefaultDataSource.Factory(requireContext())
                ).createMediaSource(mediaItem)

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.playWhenReady = true
                exoPlayer.seekTo(0, 0L)
                exoPlayer.prepare()

                exoPlayer.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        hideLoading()
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        hideLoading()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("PlayerError", "Playback failed: ${error.errorCodeName}", error)
                        Toast.makeText(requireContext(), "Playback failed", Toast.LENGTH_SHORT).show()
                    }
                })

                binding.playerView.player = exoPlayer
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to load video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }

}