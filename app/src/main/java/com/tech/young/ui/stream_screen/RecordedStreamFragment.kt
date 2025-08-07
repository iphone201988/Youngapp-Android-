package com.tech.young.ui.stream_screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

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

            val fullUrl = Constants.BASE_URL_IMAGE + (streamUrl ?: "")

        if (fullUrl.isNotEmpty()){
            val playerView = binding.playerView
            playerView.visibility = View.VISIBLE

            player = ExoPlayer.Builder(requireContext()).build()
            playerView.player = player

            val videoUrl =fullUrl
            val mediaItem = MediaItem.fromUri(videoUrl)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }

}