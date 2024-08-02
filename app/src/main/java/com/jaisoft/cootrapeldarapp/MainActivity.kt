package com.jaisoft.cootrapeldarapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jaisoft.cootrapeldarapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var loginAttempted = false
    private lateinit var binding: ActivityMainBinding
    private var isDialogVisible = false
    private lateinit var dialog: Dialog
    private lateinit var cookieHandler: CookieHandler
    private var loginSuccessful = false

    private var email: String? = null
    private var password: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            // If email or password is not provided, redirect to LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cookieHandler = CookieHandler(this)
        val cookieManager = CookieManager.getInstance()

        configureWebView(cookieManager)
        configureBackNavigation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(cookieManager: CookieManager) {
        val webSettings = binding.visor.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.visor, true)

        binding.visor.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookies = cookieManager.getCookie(url)
                if (cookies != null) {
                    cookieHandler.saveCookies(url!!, cookies)
                }

                sendBroadcast(Intent("com.jaisoft.cootrapeldarapp.MAIN_ACTIVITY_LOADED"))

                if (url == "https://app.cootrapeldar.coop/web/login" && loginAttempted) {
                    Toast.makeText(this@MainActivity, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show()
                } else if (url == "https://app.cootrapeldar.coop" && !loginSuccessful) {
                    Toast.makeText(this@MainActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    loginSuccessful = true
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url == "https://app.cootrapeldar.coop/my") {
                    view?.loadUrl("https://app.cootrapeldar.coop")
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                val errorMessage = "Error al cargar la página: ${error?.description}"
                showToast(errorMessage)
            }
        }

        val loginUrl = "https://app.cootrapeldar.coop/web/login"
        val cookies = cookieHandler.getCookies(loginUrl)
        if (cookies != null) {
            cookieManager.setCookie(loginUrl, cookies)
            binding.visor.loadUrl("https://app.cootrapeldar.coop")
        } else {
            binding.visor.loadUrl(loginUrl)
            binding.visor.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (!loginAttempted) {
                        val jsCode = """
                            document.getElementById('login').value = '$email';
                            document.getElementById('password').value = '$password';
                            document.forms[0].submit();
                        """.trimIndent()
                        binding.visor.evaluateJavascript(jsCode, null)
                        loginAttempted = true
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun configureBackNavigation() {
        binding.visor.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (binding.visor.url.equals("https://app.cootrapeldar.coop/") || binding.visor.url.equals("https://app.cootrapeldar.coop/web/login")) {
                    showDialog()
                } else if (binding.visor.canGoBack()) {
                    binding.visor.goBack()
                } else {
                    if (isDialogVisible) {
                        hideDialog()
                    } else {
                        showDialog()
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }

    private fun handleBackPress() {
        if (isDialogVisible) {
            hideDialog()
        } else if (binding.visor.canGoBack()) {
            binding.visor.goBack()
        } else {
            showDialog()
        }
    }

    private fun showDialog() {
        dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.custom_dialog_box)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<Button>(R.id.btnDialogAcept).setOnClickListener { finish() }
            findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
                dismiss()
                isDialogVisible = false
            }

            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    hideDialog()
                    true
                } else {
                    false
                }
            }
        }

        isDialogVisible = true
        dialog.show()
    }

    private fun hideDialog() {
        dialog.dismiss()
        isDialogVisible = false
    }
}
