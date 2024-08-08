package com.jaisoft.cootrapeldarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class LoginReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.jaisoft.cootrapeldarapp.LOGIN_SUCCESS" -> {
                if (context is LoginActivity) {
                    val email = intent.getStringExtra("email") ?: ""
                    val password = intent.getStringExtra("password") ?: ""
                    context.handleLoginSuccess(email, password)
                }
            }
            "com.jaisoft.cootrapeldarapp.LOGIN_FAILED" -> {
                Toast.makeText(context, "Inicio de sesión fallido. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
