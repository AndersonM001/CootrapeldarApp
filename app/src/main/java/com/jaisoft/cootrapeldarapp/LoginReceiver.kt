package com.jaisoft.cootrapeldarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class LoginReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.jaisoft.cootrapeldarapp.LOGIN_SUCCESS" -> {
                // Login successful, finish LoginActivity
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                (context as? LoginActivity)?.finish()
            }
            "com.jaisoft.cootrapeldarapp.LOGIN_FAILED" -> {
                // Login failed, show a toast message
                Toast.makeText(context, "Inicio de sesión fallido. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
