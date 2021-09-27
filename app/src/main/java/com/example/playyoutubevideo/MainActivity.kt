package com.example.playyoutubevideo

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.playyoutubevideo.databinding.ActivityMainBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    private var youtube_id : String = ""
    private var myMap : MutableMap<String, String> = mutableMapOf()
    private val task = ExtractSubtitleTask(this@MainActivity, binding?.testSub, myMap = myMap)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding?.youtubePlayerView?.let { lifecycle.addObserver(it) }

        playVideo()
    }
    private fun playVideo(){
        try {
            binding?.youtubePlayerView?.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    binding?.btnPlayVideo?.setOnClickListener(View.OnClickListener {
                        binding?.edtPasteLink?.isEnabled = false
                        youtube_id = binding?.edtPasteLink?.text.toString().trim()
                        youTubePlayer.loadVideo(convertYoutubeLink(youtube_id), 0f)
                        task.execute(convertYoutubeLink(youtube_id))
                    })
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    val tracker = YouTubePlayerTracker()
                    youTubePlayer.addListener(tracker)
                    val startArrange = second.times(1000).roundToInt() - 150
                    val endArrange = second.times(1000).roundToInt() + 150
                    if (task.myMap.isNotEmpty()) {
                        for (sub in task.myMap) {
                            if (sub.key.toInt() in startArrange..endArrange) {
                                binding?.tvSub?.text = sub.value
                            }
                        }
                    }
                }
            })
        }catch (ex: Exception){

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        binding?.youtubePlayerView?.release()
    }


    private fun convertYoutubeLink(youtubeLink : String) : String{
        if (youtubeLink.contains("youtu.be")){
            return youtubeLink.removePrefix("https://youtu.be/")
        }
        return youtubeLink.removePrefix("https://www.youtube.com/watch?v=")
    }
}