package app.qrcache.scan

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import app.qrcache.R

/** The main button on the result sheet for each type of content. */
@StringRes
fun ScanContent.primaryActionLabel(): Int = when (this) {
    is ScanContent.Link -> R.string.action_open_link
    is ScanContent.Wifi -> when {
        canAddWifiDirectly() -> R.string.action_join_wifi
        password != null -> R.string.action_copy_password_wifi
        else -> R.string.action_open_wifi_settings
    }
    is ScanContent.Contact -> R.string.action_add_contact
    is ScanContent.Email -> R.string.action_send_email
    is ScanContent.Phone -> R.string.action_call
    is ScanContent.Sms -> R.string.action_send_sms
    is ScanContent.Geo -> R.string.action_open_maps
    is ScanContent.Event -> R.string.action_add_event
    is ScanContent.Text -> R.string.action_copy_text
}

fun ScanContent.runPrimaryAction(context: Context, inAppBrowser: Boolean = true) {
    when (this) {
        is ScanContent.Link -> openLink(context, url, inAppBrowser)
        is ScanContent.Wifi -> joinWifi(context, this)
        is ScanContent.Contact -> start(
            context,
            Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                organization?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
            },
        )
        is ScanContent.Email -> start(
            context,
            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Uri.encode(address, "@"))).apply {
                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            },
        )
        is ScanContent.Phone -> start(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
        is ScanContent.Sms -> start(
            context,
            Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)).apply {
                message?.let { putExtra("sms_body", it) }
            },
        )
        is ScanContent.Geo -> start(
            context,
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:$latitude,$longitude?q=" + Uri.encode(query ?: "$latitude,$longitude")),
            ),
        )
        is ScanContent.Event -> start(
            context,
            Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                summary?.let { putExtra(CalendarContract.Events.TITLE, it) }
                location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
                start?.let(::icsDateToEpochMillis)?.let { putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
                end?.let(::icsDateToEpochMillis)?.let { putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it) }
                if (start != null && 'T' !in start) putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            },
        )
        is ScanContent.Text -> copyToClipboard(context, raw)
    }
}

/** The most useful text to copy: the URL, the password, the number. Falls back to the raw text. */
fun ScanContent.copyValue(): String = when (this) {
    is ScanContent.Link -> url
    is ScanContent.Wifi -> password ?: ssid
    is ScanContent.Phone -> number
    is ScanContent.Email -> address.ifBlank { raw }
    else -> raw
}

fun share(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    start(context, Intent.createChooser(send, null))
}

fun copyToClipboard(context: Context, text: String, label: String = "QRCache") {
    context.getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(label, text))
    // Android 13+ shows its own confirmation.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
    }
}

fun openLink(context: Context, url: String, inAppBrowser: Boolean = true) {
    val uri = Uri.parse(url)
    if (!inAppBrowser) {
        start(context, Intent(Intent.ACTION_VIEW, uri))
        return
    }
    try {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
    } catch (_: ActivityNotFoundException) {
        start(context, Intent(Intent.ACTION_VIEW, uri))
    }
}

/** Android 11+ can add a network in one tap through the system "Save network?" dialog. WEP isn't supported there. */
private fun ScanContent.Wifi.canAddWifiDirectly(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !security.equals("WEP", ignoreCase = true)

private fun joinWifi(context: Context, wifi: ScanContent.Wifi) {
    if (wifi.canAddWifiDirectly() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val suggestion = wifiSuggestion(wifi)
        if (suggestion != null) {
            val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
                putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(suggestion))
            }
            if (start(context, intent, showError = false)) return
        }
    }
    wifi.password?.let { copyToClipboard(context, it, context.getString(R.string.wifi_password_label)) }
    start(context, Intent(Settings.ACTION_WIFI_SETTINGS))
}

/** Null if the system rejects the details (for example a password outside 8–63 characters). */
@RequiresApi(Build.VERSION_CODES.R)
private fun wifiSuggestion(wifi: ScanContent.Wifi): WifiNetworkSuggestion? = runCatching {
    WifiNetworkSuggestion.Builder().apply {
        setSsid(wifi.ssid)
        setIsHiddenSsid(wifi.hidden)
        val password = wifi.password
        if (password != null) {
            when {
                wifi.security.equals("SAE", ignoreCase = true) ||
                    wifi.security.equals("WPA3", ignoreCase = true) -> setWpa3Passphrase(password)
                else -> setWpa2Passphrase(password)
            }
        }
    }.build()
}.getOrNull()

/** Starts [intent], showing a short message if no app can handle it. Returns whether it started. */
private fun start(context: Context, intent: Intent, showError: Boolean = true): Boolean =
    try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        if (showError) Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        false
    }
