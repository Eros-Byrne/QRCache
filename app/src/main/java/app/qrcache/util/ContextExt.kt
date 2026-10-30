package app.qrcache.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The Activity behind a Compose context, if any. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
