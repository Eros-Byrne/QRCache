package app.qrcache.scan

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import app.qrcache.R

/** Icon and label for each content type (see DESIGN.md → Icons). */
val ScanContent.icon: ImageVector
    get() = when (this) {
        is ScanContent.Link -> Icons.Rounded.Link
        is ScanContent.Wifi -> Icons.Rounded.Wifi
        is ScanContent.Contact -> Icons.Rounded.Person
        is ScanContent.Email -> Icons.Rounded.Mail
        is ScanContent.Phone -> Icons.Rounded.Call
        is ScanContent.Sms -> Icons.Rounded.Sms
        is ScanContent.Geo -> Icons.Rounded.LocationOn
        is ScanContent.Event -> Icons.Rounded.Event
        is ScanContent.Text -> Icons.AutoMirrored.Rounded.Notes
    }

@get:StringRes
val ScanContent.typeLabel: Int
    get() = when (this) {
        is ScanContent.Link -> R.string.type_link
        is ScanContent.Wifi -> R.string.type_wifi
        is ScanContent.Contact -> R.string.type_contact
        is ScanContent.Email -> R.string.type_email
        is ScanContent.Phone -> R.string.type_phone
        is ScanContent.Sms -> R.string.type_sms
        is ScanContent.Geo -> R.string.type_geo
        is ScanContent.Event -> R.string.type_event
        is ScanContent.Text -> R.string.type_text
    }

/** What to show under the title: the link, the network, the number… */
val ScanContent.displayValue: String
    get() = when (this) {
        is ScanContent.Link -> url
        is ScanContent.Wifi -> listOfNotNull(ssid, security).joinToString(" · ")
        is ScanContent.Contact -> listOfNotNull(phone, email, organization).joinToString("\n").ifEmpty { raw }
        is ScanContent.Email -> listOfNotNull(address, subject).joinToString("\n")
        is ScanContent.Phone -> number
        is ScanContent.Sms -> listOfNotNull(number, message).joinToString("\n")
        is ScanContent.Geo -> "$latitude, $longitude"
        is ScanContent.Event -> listOfNotNull(location, start).joinToString("\n").ifEmpty { raw }
        is ScanContent.Text -> raw
    }
