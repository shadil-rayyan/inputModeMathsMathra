package com.example.codecompass.inputmodemathra.utils.common

import android.content.Context
import android.text.SpannableString
import android.text.style.LocaleSpan
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper
import java.util.*

object AccessibilityLocaleWrapper {

    fun setLocalizedContentDescription(context: Context, view: View, text: String) {
        val langCode = LocaleHelper.getLanguage(context)
        val locale = Locale(langCode)  // ✅ Declare locale here so it’s accessible below

        view.contentDescription = text

        view.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)

                val spannable = SpannableString(text).apply {
                    setSpan(LocaleSpan(locale), 0, text.length, 0)
                }

                info.text = spannable

                // This line may not be necessary; not all Android versions use it
                // info.locale = locale  // Optional: Can be removed if causing issue
            }
        }
    }
}
