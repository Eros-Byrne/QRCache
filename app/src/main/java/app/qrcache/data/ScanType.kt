package app.qrcache.data

import app.qrcache.scan.ScanContent

/** Stored type of a scan. Saved by name, so never rename entries. */
enum class ScanType { LINK, TEXT, WIFI, CONTACT, EMAIL, PHONE, SMS, GEO, EVENT }

val ScanContent.scanType: ScanType
    get() = when (this) {
        is ScanContent.Link -> ScanType.LINK
        is ScanContent.Text -> ScanType.TEXT
        is ScanContent.Wifi -> ScanType.WIFI
        is ScanContent.Contact -> ScanType.CONTACT
        is ScanContent.Email -> ScanType.EMAIL
        is ScanContent.Phone -> ScanType.PHONE
        is ScanContent.Sms -> ScanType.SMS
        is ScanContent.Geo -> ScanType.GEO
        is ScanContent.Event -> ScanType.EVENT
    }
