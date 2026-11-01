package com.brunoshiroma.url_expander

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

private object IosActions : PlatformActions {
    override fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    override fun share(text: String) {
        val controller = UIActivityViewController(listOf(text), null)
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(controller, animated = true, completion = null)
    }

    override fun openExternal(url: String) {
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

fun MainViewController() = ComposeUIViewController { App(sharedText = null, actions = IosActions) }
