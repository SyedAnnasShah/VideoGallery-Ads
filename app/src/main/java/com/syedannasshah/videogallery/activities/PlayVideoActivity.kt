package com.syedannasshah.videogallery.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.syedannasshah.videogallery.data.Video
import com.syedannasshah.videogallery.databinding.ActivityPlayVideoBinding


class PlayVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayVideoBinding
    private lateinit var playerControlView: PlayerControlView

    private var player: SimpleExoPlayer? = null
    private val playerView: PlayerView? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    var mediaItems: MutableList<MediaItem>? = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaItems = createMediaItemsList(MainActivity.videoList)

        setupExoPlayer(intent.getStringExtra("videoPath"))

    }

    private fun setupExoPlayer(path: String?) {
        // Initialize ExoPlayer
        player = SimpleExoPlayer.Builder(this).build()


        // Set the player to a PlayerView (assuming you have a PlayerView in your layout)
        binding.playerView.player = player
        binding.playerControlView.player = player


        // Create a MediaItem for the video you want to play
//        val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(path))
        player?.setMediaItems(mediaItems!!)




        player?.seekToDefaultPosition(intent.getIntExtra("videoIndex",0))




        // Prepare the player asynchronously
        player?.prepare()

        player?.play()

    }

    fun createMediaItemsList(videoList: List<Video?>): MutableList<MediaItem>? {
        val mediaItems: MutableList<MediaItem> = ArrayList()
        for (video in videoList) {
            val videoUrl: String = video!!.path
            if (videoUrl != null && !videoUrl.isEmpty()) {
                val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
                mediaItems.add(mediaItem)
            }
        }
        return mediaItems
    }


    // Function to play the next item in the playlist
    fun playNextItem() {
        val nextWindowIndex = player!!.nextWindowIndex
        if (nextWindowIndex != C.INDEX_UNSET) {
            player!!.seekTo(nextWindowIndex, 0)
            player!!.playWhenReady = true
        }
    }

    // Function to play the previous item in the playlist
    fun playPreviousItem() {
        val previousWindowIndex = player!!.previousWindowIndex
        if (previousWindowIndex != C.INDEX_UNSET) {
            player!!.seekTo(previousWindowIndex, 0)
            player!!.playWhenReady = true
        }
    }


    override fun onStart() {
        super.onStart()
        if (player == null) {
            player = SimpleExoPlayer.Builder(this).build()
            player?.setPlayWhenReady(playWhenReady)
            player?.seekTo(currentWindow, playbackPosition)
            player?.prepare()
        }
        playerView?.setPlayer(player)
    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            player?.setPlayWhenReady(playWhenReady)
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            playWhenReady = player?.playWhenReady == true
            playbackPosition = player?.currentPosition!!
            currentWindow = player?.getCurrentWindowIndex()!!
            player?.playWhenReady = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player?.release()
            player = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.release()
            player = null
        }
    }
}