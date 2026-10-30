package app.qrcache

import android.app.Application

class QRCacheApplication : Application() {
    val container by lazy { AppContainer(this) }
}
