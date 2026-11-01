package com.brunoshiroma.url_expander

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Platform specific operations used by the shared UI. */
interface PlatformActions {
    fun copyToClipboard(text: String)
    fun share(text: String)
    fun openExternal(url: String)
}

@Composable
expect fun PlatformWebView(url: String, modifier: Modifier)
