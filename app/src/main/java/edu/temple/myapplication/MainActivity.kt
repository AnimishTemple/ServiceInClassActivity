package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false
    var isPaused = false
    var isRunning = false // New flag to track if timer is running

    val timerHandler = Handler(Looper.getMainLooper()) {
        timerTextView.text = it.what.toString()
        true
    }

    val servConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)

        // Bind to the TimerService
        bindService(
            Intent(this, TimerService::class.java),
            servConnection,
            BIND_AUTO_CREATE
        )

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (isConnected) {
                if (isRunning) {
                    // If timer is running, toggle pause/resume
                    if (isPaused) {
                        timerBinder.pause() // Resumes timer
                        isPaused = false
                    } else {
                        timerBinder.pause() // Pauses timer
                        isPaused = true
                    }
                } else {
                    // Start from 100 if timer was fully stopped
                    timerBinder.start(100)
                    isRunning = true
                    isPaused = false
                }
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                isRunning = false // Reset flags when fully stopped
                isPaused = false
            }
        }
    }

    override fun onDestroy() {
        unbindService(servConnection)
        super.onDestroy()
    }
}
