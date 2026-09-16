package com.example.myandroid.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SecureClipboard {
    private const val CLEAR_DELAY_MILLIS = 15_000L

    fun copy(
        context: Context,
        label: String,
        value: String,
        scope: CoroutineScope
    ) {
        if (value.isBlank()) return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        scope.launch {
            delay(CLEAR_DELAY_MILLIS)
            if (!clipboard.hasPrimaryClip()) return@launch

            val current = clipboard.primaryClip
            val currentValue = current?.getItemAt(0)?.text?.toString()
            val currentLabel = current?.description?.label?.toString()
            if (currentValue == value && currentLabel == label) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
        }
    }
}
