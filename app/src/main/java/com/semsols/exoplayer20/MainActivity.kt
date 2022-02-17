package com.semsols.exoplayer20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var player: SimpleExoPlayer? = null
    private var playOnReady = true
    private var currentWindow = 0
    private var playBackPostion : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPlayer()

    }

    private fun initPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        video_view.player = player

        var videoUrl = "https://www.youtube.com/watch?v=Ysojv6Z0Tao&list=RDYsojv6Z0Tao&start_radio=1"

        object : YouTubeExtractor(this){
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                videoMeta: VideoMeta?
            ) {
                if (ytFiles != null)
                {
                    val itag = 137 //1080p
                    val audioTag = 140
                    val videoUrl = ytFiles[itag].url
                    val audioUrl = ytFiles[audioTag].url

                    val audioSource : MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(audioUrl))

                    val videoSource : MediaSource = ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(videoUrl))

                    player!!.setMediaSource(MergingMediaSource(true,videoSource,audioSource),true)
                    player!!.prepare()
                    player!!.playWhenReady = playOnReady
                    player!!.seekTo(currentWindow,playBackPostion)

                }
            }


        }.extract(videoUrl,false,true)
    }

    override fun onStart() {
        super.onStart()

        if(Util.SDK_INT >= 24){
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()

        if(Util.SDK_INT >= 24 && player == null){
            initPlayer()
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {

        actionBar?.hide()

        video_view.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
    }

    override fun onStop() {
        if(Util.SDK_INT >= 24) releasePlayer()
        super.onStop()

    }

    private fun releasePlayer() {

        if (player != null){

            playOnReady = player!!.playWhenReady
            playBackPostion = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null

        }

    }

}