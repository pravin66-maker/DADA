package com.family.bhajanalarm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.family.bhajanalarm.databinding.ActivityNowPlayingBinding

class NowPlayingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNowPlayingBinding

    private var hasSeenPlaying = false

    private val listener: (PlaybackState, String) -> Unit = { state, name ->
        runOnUiThread { render(state, name) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNowPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra(PlaybackService.EXTRA_NAME)
            ?: PlaybackStatus.songName
        binding.songNameText.text = name

        binding.btnPlayPause.setOnClickListener {
            val action = if (PlaybackStatus.state == PlaybackState.PLAYING) {
                PlaybackService.ACTION_PAUSE
            } else {
                PlaybackService.ACTION_RESUME
            }
            sendServiceAction(action)
        }

        binding.btnStop.setOnClickListener {
            sendServiceAction(PlaybackService.ACTION_STOP)
            finish()
        }

        render(PlaybackStatus.state, PlaybackStatus.songName.ifEmpty { name ?: "" })
    }

    override fun onStart() {
        super.onStart()
        PlaybackStatus.addListener(listener)
    }

    override fun onStop() {
        super.onStop()
        PlaybackStatus.removeListener(listener)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(this, PlaybackService::class.java).apply { this.action = action }
        startForegroundService(intent)
    }

    private fun render(state: PlaybackState, name: String) {
        if (name.isNotEmpty()) binding.songNameText.text = name
        when (state) {
            PlaybackState.PLAYING -> {
                hasSeenPlaying = true
                binding.btnPlayPause.setText(R.string.pause)
            }
            PlaybackState.PAUSED -> binding.btnPlayPause.setText(R.string.play)
            PlaybackState.STOPPED -> {
                binding.btnPlayPause.setText(R.string.play)
                if (hasSeenPlaying) finish()
            }
        }
    }
}
