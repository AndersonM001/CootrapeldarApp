package com.jaisoft.cootrapeldarapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jaisoft.cootrapeldarapp.databinding.ActivityMainBinding
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.transition.Visibility


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var loginAttempted = false
    private var loginSuccessful = false
    private val handler = Handler(Looper.getMainLooper())
    private var isDialogVisible = false
    private lateinit var dialog: Dialog
    private lateinit var cookieHandler: CookieHandler

    private var email: String? = null
    private var password: String? = null
    private var biometrica: String? = null

    companion object {
        private const val REQUEST_WRITE_STORAGE = 112
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, puedes proceder con la descarga
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show()
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
        binding.visor.webViewClient = WebViewClient()
        binding.visor.webChromeClient = WebChromeClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.visor, true)

        // Configurar el DownloadListener
        binding.visor.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
                if (url != null) {
                    val request = DownloadManager.Request(Uri.parse(url))
                    val cookies = CookieManager.getInstance().getCookie(url)
                    request.addRequestHeader("cookie", cookies)
                    request.addRequestHeader("User-Agent", userAgent)
                    request.setDescription("Descargando archivo...")
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                    request.allowScanningByMediaScanner()
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
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
        loginIntent.putExtra("email", email)
        loginIntent.putExtra("password", password)
        biometrica = intent.getStringExtra("biometrica")
        sendBroadcast(loginIntent)
        Toast.makeText(this, biometrica.toString(), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
        if (biometrica == "true") {
            Toast.makeText(this, "Credenciales guardadas", Toast.LENGTH_SHORT).show()
            showSaveCredentialsDialog(email!!, password!!)
        }
        Visibility()
    }

    private fun notifyLoginFailure(message: String) {
        val loginIntent = Intent("com.jaisoft.cootrapeldarapp.LOGIN_FAILED")
        sendBroadcast(loginIntent)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
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

            findViewById<Button>(R.id.btnDialogAcept).setOnClickListener { finishAffinity() }
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

    override fun onDestroy() {
        super.onDestroy()
    }

    fun Visibility(){
        binding.visor.visibility = View.VISIBLE
    }

    fun showSaveCredentialsDialog(email: String, password: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Guardar credenciales")
        builder.setMessage("¿Desea guardar sus credenciales para usar la autenticación biométrica?")
        builder.setPositiveButton("Sí") { dialog, which ->
            val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", email)
            editor.putString("password", password)
            editor.apply()
        }
        builder.setNegativeButton("No") { dialog, which ->
            // No hacer nada
        }
        builder.show()
    }
}
