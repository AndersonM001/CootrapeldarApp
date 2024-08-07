package com.jaisoft.cootrapeldarapp

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jaisoft.cootrapeldarapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var loginReceiver: LoginReceiver
    lateinit var binding: ActivityLoginBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize animations

        var fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)

        // Setting the bottom animation on top layout

        binding.topLinearLayout.animation = bottom_down
        var handler = Handler()
        var runnable = Runnable {
            binding.welcomeText.animation = fade_in
            binding.loginCardView.animation = fade_in
            binding.registerButton.animation = fade_in
            binding.logo.animation = fade_in
            binding.welcomeText.visibility = View.VISIBLE
            binding.loginCardView.visibility = View.VISIBLE
            binding.registerButton.visibility = View.VISIBLE
            binding.logo.visibility = View.VISIBLE
        }
        
        handler.postDelayed(runnable, 1000)

        binding.togglePasswordVisibility.setOnClickListener {
            if (binding.password.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                binding.password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.togglePasswordVisibility.setImageResource(R.drawable.outline_visibility_off_24) // Change icon to "hide password"
            } else {
                binding.password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.togglePasswordVisibility.setImageResource(R.drawable.outline_visibility_24) // Change icon to "show password"
            }
            // Move the cursor to the end of the text
            binding.password.setSelection(binding.password.text.length)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString().lowercase()
            val password = binding.password.text.toString()

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
