package edu.uw.ischool.mrahma3.arewethereyet


import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button
    private val SEND_SMS_REQUEST_CODE = 123
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
//                    mediaPlayer.start()
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

        return phoneNumber.isNotBlank() && message.isNotBlank() && intervalText.isNotBlank()
                && intervalText.toIntOrNull()?.let {
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
            if (ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS")
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.SEND_SMS"),
                    SEND_SMS_REQUEST_CODE
                )
            } else {
                // Permission is already granted, send the SMS
                sendSMS(phoneNumber, message)
                // Send the audio via MMS
                val audioUri = Uri.parse("https://file-examples.com/storage/fe3269a6ea65d68689ae021/2017/11/file_example_MP3_700KB.mp3")
                sendAudioViaMMS(audioUri, phoneNumber)
            }
        }, 0, interval, TimeUnit.MINUTES)
        isNaggingStarted = true
        startButton.text = "Stop"
    }

    fun sendAudioViaMMS(audioUri: Uri, phoneNumber: String) {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra("sms_body", "Check out this audio!")
        sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri)
        sendIntent.type = "audio/*"
        sendIntent.putExtra("address", phoneNumber)
        sendIntent.putExtra("exit_on_sent", true) // Optional, to exit the messaging app after sending

        startActivity(Intent.createChooser(sendIntent, "Send MMS"))
    }



    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("MainActivity", "SMS sent successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error sending SMS: ${e.message}")
        }
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





