package com.jaisoft.cootrapeldarapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        checkPermissions() // Verificar permisos

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

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, puedes proceder con la descarga
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show()
            }
        }
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

        // Configurar el DownloadListener
        binding.visor.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
                if (url != null) {
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setTitle("Descargando archivo")
                    request.setDescription("Descargando desde la aplicación")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_file") // Cambia el nombre según sea necesario

                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.enqueue(request)
                } else {
                    Toast.makeText(this@MainActivity, "URL de descarga no válida", Toast.LENGTH_SHORT).show()
                }
            }
        })
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

                    // Establecer un tiempo de espera para la redirección
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
