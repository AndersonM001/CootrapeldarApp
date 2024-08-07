package com.jaisoft.cootrapeldarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jaisoft.cootrapeldarapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var loginAttempted = false
    private var loginSuccessful = false
    private val handler = Handler(Looper.getMainLooper())

    private var email: String? = null
    private var password: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureWebView()
        attemptLogin()
    }

    private fun redirectToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val webSettings = binding.visor.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.visor, true)
    }

    private fun attemptLogin() {
        val loginUrl = "https://app.cootrapeldar.coop/web/login"

        binding.visor.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url == loginUrl && !loginAttempted) {
                    val jsCode = """
                        document.getElementById('login').value = '$email';
                        document.getElementById('password').value = '$password';
                        document.forms[0].submit();
                    """.trimIndent()
                    binding.visor.evaluateJavascript(jsCode, null)
                    loginAttempted = true

                    // Set a timeout for redirection
                    handler.postDelayed({
                        if (!loginSuccessful) {
                            notifyLoginFailure("Credenciales incorrectas.")
                        }
                    }, 5000)
                } else if (url == "https://app.cootrapeldar.coop/my") {
                    loginSuccessful = true
                    notifyLoginSuccess()
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                notifyLoginFailure("No tiene internet.")
            }
        }

        binding.visor.loadUrl(loginUrl)
    }

    private fun notifyLoginSuccess() {
        val loginIntent = Intent("com.jaisoft.cootrapeldarapp.LOGIN_SUCCESS")
        sendBroadcast(loginIntent)
        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
        setContentView(binding.root)
    }

    private fun notifyLoginFailure(message: String) {
        val loginIntent = Intent("com.jaisoft.cootrapeldarapp.LOGIN_FAILED")
        sendBroadcast(loginIntent)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
