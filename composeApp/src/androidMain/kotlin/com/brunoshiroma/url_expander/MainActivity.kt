package com.brunoshiroma.url_expander

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    private var sharedText by mutableStateOf<String?>(null)

    private val actions = object : PlatformActions {
        override fun copyToClipboard(text: String) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("url", text))
        }

        override fun share(text: String) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(send, "Open with"))
        }

        override fun openExternal(url: String) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSendText(intent)
        setContent { App(sharedText, actions) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSendText(intent)
    }

    // The app is transient: leave as soon as the user moves away (as the Flutter version did).
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) finishAndRemoveTask()
    }

    private fun handleSendText(intent: Intent) {
        if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        }
    }
}
