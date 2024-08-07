package com.jaisoft.cootrapeldarapp

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var loginReceiver: LoginReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().lowercase()
            val password = passwordInput.text.toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }

        // Initialize the receiver
        loginReceiver = LoginReceiver()

        val filter = IntentFilter().apply {
            addAction("com.jaisoft.cootrapeldarapp.LOGIN_SUCCESS")
            addAction("com.jaisoft.cootrapeldarapp.LOGIN_FAILED")
        }
        registerReceiver(loginReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(loginReceiver)
    }
}
