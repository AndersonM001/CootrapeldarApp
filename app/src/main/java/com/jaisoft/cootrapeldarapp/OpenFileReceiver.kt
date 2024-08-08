package com.jaisoft.cootrapeldarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class OpenFileReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fileUri = intent.getParcelableExtra<Uri>("fileUri")
        val fileMimeType = intent.getStringExtra("fileMimeType")

        fileUri?.let {
            val openIntent = Intent(Intent.ACTION_VIEW)
            openIntent.setDataAndType(it, fileMimeType)
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(openIntent)
        }
    }
}
