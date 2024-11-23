package com.example.spaTi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.videoView)
        val videoUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.intro)
        videoView.setVideoURI(videoUri)
        videoView.start()

        videoView.setOnCompletionListener {
            onVideoFinished()
        }
    }

    private fun onVideoFinished() {
        videoView.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
