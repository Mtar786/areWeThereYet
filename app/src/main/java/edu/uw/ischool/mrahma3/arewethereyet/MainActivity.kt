package edu.uw.ischool.mrahma3.arewethereyet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button

    private var isNaggingStarted = false
    private var executor: ScheduledExecutorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        intervalEditText = findViewById(R.id.intervalEditText)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            if (!isNaggingStarted) {
                if (validateInputs()) {
                    startNagging()
                } else {
                    Toast.makeText(this, "Please fill out all fields with valid values.", Toast.LENGTH_SHORT).show()
                }
            } else {
                stopNagging()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val message = messageEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val intervalText = intervalEditText.text.toString()

        return message.isNotBlank() && intervalText.isNotBlank() && intervalText.toIntOrNull()?.let {
            it > 0
        } ?: false
    }

    private fun startNagging() {
        if (!validateInputs()) {
            // Show a toast message indicating the reason for failure
            Toast.makeText(this, "Please fill out all fields with valid values.", Toast.LENGTH_SHORT).show()
            return
        }

        val interval = intervalEditText.text.toString().toLong() // Minutes interval
        val message = messageEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        Log.d("MainActivity", "Starting nagging service with interval $interval minutes")
        executor = Executors.newSingleThreadScheduledExecutor()
        executor?.scheduleAtFixedRate({
            Log.d("MainActivity", "Sending message: $phoneNumber: $message")
            showToast("$phoneNumber: $message")
        }, 0, interval, TimeUnit.MINUTES)
        isNaggingStarted = true
        startButton.text = "Stop"
    }

    private fun stopNagging() {
        executor?.shutdown()
        isNaggingStarted = false
        startButton.text = "Start"
        Log.d("MainActivity", "Nagging service stopped")
    }

    private fun showToast(message: String) {
        runOnUiThread {
            val phoneNumber = phoneNumberEditText.text.toString()
            val customToast = Toast.makeText(this, "Texting $phoneNumber: $message", Toast.LENGTH_SHORT)
            customToast.show()
        }
    }
}





