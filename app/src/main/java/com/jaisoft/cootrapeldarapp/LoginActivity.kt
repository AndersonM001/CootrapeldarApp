package com.jaisoft.cootrapeldarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.jaisoft.cootrapeldarapp.databinding.ActivityLoginBinding
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var loginReceiver: LoginReceiver
    private lateinit var finishReceiver: BroadcastReceiver
    lateinit var binding: ActivityLoginBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize animations
        val fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)

        // Setting the bottom animation on top layout
        binding.topLinearLayout.animation = bottom_down
        val handler = Handler()
        val runnable = Runnable {
            binding.welcomeText.animation = fade_in
            binding.registerButton.animation = fade_in
            binding.welcomeText.visibility = View.VISIBLE
            binding.registerButton.visibility = View.VISIBLE
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
            val biometrica = "true"

            if (isEmailValid(email) && isPasswordValid(password)) {
                // Clear any previously saved credentials
                clearSavedCredentials()
                Toast.makeText(this, "Por aqui", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("biometrica", biometrica)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
            } else {
                // Muestra un mensaje de error
                showError("Por favor, verifica tu correo y contraseña.")
            }
        }

        // Initialize the receivers
        loginReceiver = LoginReceiver()
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                finish()
            }
        }

        val filter = IntentFilter().apply {
            addAction("com.jaisoft.cootrapeldarapp.LOGIN_SUCCESS")
            addAction("com.jaisoft.cootrapeldarapp.LOGIN_FAILED")
        }
        registerReceiver(loginReceiver, filter, RECEIVER_NOT_EXPORTED)

        val finishFilter = IntentFilter("com.jaisoft.cootrapeldarapp.FINISH_LOGIN_ACTIVITY")
        registerReceiver(finishReceiver, finishFilter, RECEIVER_NOT_EXPORTED)

        // Setup biometric login
        setupBiometricLogin()
    }

    // Función para validar el correo electrónico
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Función para validar la contraseña
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    // Función para mostrar un mensaje de error
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupBiometricLogin() {
        val biometricManager = BiometricManager.from(this)
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device can authenticate with biometrics
                binding.fingerprintButton.visibility = View.VISIBLE
                binding.fingerprintButton.setOnClickListener { showBiometricPrompt() }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // No biometric hardware available
                binding.fingerprintButton.visibility = View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Biometric hardware is currently unavailable
                binding.fingerprintButton.visibility = View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // No biometric data enrolled
                binding.fingerprintButton.visibility = View.GONE
            }
        }

        if (savedEmail != null && savedPassword != null) {
            binding.fingerprintButton.visibility = View.VISIBLE
        } else {
            binding.fingerprintButton.visibility = View.GONE
        }

    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Handle error case
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Handle success case
                val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                val email = sharedPreferences.getString("email", null)
                val password = sharedPreferences.getString("password", null)
                if (email != null && password != null) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("password", password)
                    startActivity(intent)
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Handle failure case
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Login using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun clearSavedCredentials() {
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.remove("password")
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(loginReceiver)
        unregisterReceiver(finishReceiver)
    }



    // Método para manejar el inicio de sesión exitoso
    fun handleLoginSuccess(email: String, password: String) {
        //showSaveCredentialsDialog(email, password)
        // Aquí puedes cerrar LoginActivity si es necesario
        finish()
    }
}
