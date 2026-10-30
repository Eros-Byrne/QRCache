package app.qrcache.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentParserTest {

    private inline fun <reified T : ScanContent> parse(raw: String): T {
        val result = ContentParser.parse(raw)
        assertTrue("Expected ${T::class.simpleName} for '$raw' but got $result", result is T)
        return result as T
    }

    // Links

    @Test fun httpsLink() {
        val link = parse<ScanContent.Link>("https://www.menu.hotel.com/breakfast?day=1")
        assertEquals("https://www.menu.hotel.com/breakfast?day=1", link.url)
        assertEquals("menu.hotel.com", link.defaultTitle)
    }

    @Test fun uppercaseSchemeAndWhitespace() {
        val link = parse<ScanContent.Link>("  HTTP://Example.com  ")
        assertEquals("HTTP://Example.com", link.url)
        assertEquals("Example.com", link.defaultTitle)
    }

    @Test fun bareDomainGetsHttps() {
        val link = parse<ScanContent.Link>("www.example.org/menu")
        assertEquals("https://www.example.org/menu", link.url)
        assertEquals("example.org", link.defaultTitle)
    }

    @Test fun linkWithPortAndUserInfo() {
        assertEquals("host.local", parse<ScanContent.Link>("http://user@host.local:8080/x").defaultTitle)
    }

    @Test fun sentenceWithDotIsText() {
        parse<ScanContent.Text>("Welcome to the hotel. Enjoy!")
    }

    @Test fun versionNumberIsText() {
        parse<ScanContent.Text>("v1.2.3")
    }

    // Wi-Fi

    @Test fun wifiWpa() {
        val wifi = parse<ScanContent.Wifi>("WIFI:T:WPA;S:Hotel Guest;P:sunrise123;H:false;;")
        assertEquals("Hotel Guest", wifi.ssid)
        assertEquals("sunrise123", wifi.password)
        assertEquals("WPA", wifi.security)
        assertFalse(wifi.hidden)
        assertEquals("Hotel Guest", wifi.defaultTitle)
    }

    @Test fun wifiFieldOrderAndEscapes() {
        val wifi = parse<ScanContent.Wifi>("""WIFI:S:My\;Net\:5G;P:pa\\ss\;word;T:WPA2;H:true;;""")
        assertEquals("My;Net:5G", wifi.ssid)
        assertEquals("pa\\ss;word", wifi.password)
        assertTrue(wifi.hidden)
    }

    @Test fun wifiOpenNetwork() {
        val wifi = parse<ScanContent.Wifi>("WIFI:T:nopass;S:Lobby;;")
        assertNull(wifi.password)
        assertNull(wifi.security)
    }

    @Test fun wifiMissingTrailingSemicolons() {
        assertEquals("Cafe", parse<ScanContent.Wifi>("wifi:S:Cafe;P:x").ssid)
    }

    // Contacts

    @Test fun vCard() {
        val contact = parse<ScanContent.Contact>(
            "BEGIN:VCARD\r\nVERSION:3.0\r\nN:Doe;Jane;;;\r\nFN:Jane Doe\r\nORG:Grand Hotel;Front Desk\r\n" +
                "TEL;TYPE=CELL:+1 555 0100\r\nEMAIL:jane@hotel.com\r\nEND:VCARD",
        )
        assertEquals("Jane Doe", contact.name)
        assertEquals("+1 555 0100", contact.phone)
        assertEquals("jane@hotel.com", contact.email)
        assertEquals("Grand Hotel", contact.organization)
        assertEquals("Jane Doe", contact.defaultTitle)
    }

    @Test fun vCardWithoutFnUsesN() {
        assertEquals("Jane Doe", parse<ScanContent.Contact>("BEGIN:VCARD\nN:Doe;Jane\nEND:VCARD").name)
    }

    @Test fun vCardFoldedLine() {
        val contact = parse<ScanContent.Contact>("BEGIN:VCARD\nFN:Jane\n  Doe\nEND:VCARD")
        assertEquals("Jane Doe", contact.name)
    }

    @Test fun meCard() {
        val contact = parse<ScanContent.Contact>("MECARD:N:Doe,Jane;TEL:5550100;EMAIL:jane@hotel.com;;")
        assertEquals("Jane Doe", contact.name)
        assertEquals("5550100", contact.phone)
    }

    @Test fun contactWithOnlyPhoneTitle() {
        assertEquals("5550100", parse<ScanContent.Contact>("MECARD:TEL:5550100;;").defaultTitle)
    }

    // Email

    @Test fun mailto() {
        val email = parse<ScanContent.Email>("mailto:desk@hotel.com?subject=Late%20checkout&body=Room+12")
        assertEquals("desk@hotel.com", email.address)
        assertEquals("Late checkout", email.subject)
        assertEquals("Room 12", email.body)
    }

    @Test fun matmsg() {
        val email = parse<ScanContent.Email>("MATMSG:TO:desk@hotel.com;SUB:Hi;BODY:Hello;;")
        assertEquals("desk@hotel.com", email.address)
        assertEquals("Hi", email.subject)
    }

    // Phone and SMS

    @Test fun tel() {
        assertEquals("+15550100", parse<ScanContent.Phone>("tel:+15550100").number)
    }

    @Test fun smsto() {
        val sms = parse<ScanContent.Sms>("SMSTO:+15550100:Table for two")
        assertEquals("+15550100", sms.number)
        assertEquals("Table for two", sms.message)
    }

    @Test fun smsWithBodyQuery() {
        val sms = parse<ScanContent.Sms>("sms:+15550100?body=Hi%20there")
        assertEquals("+15550100", sms.number)
        assertEquals("Hi there", sms.message)
    }

    @Test fun smsNoMessage() {
        assertNull(parse<ScanContent.Sms>("smsto:5550100").message)
    }

    // Geo

    @Test fun geo() {
        val geo = parse<ScanContent.Geo>("geo:40.7128,-74.0060?q=Grand%20Hotel")
        assertEquals(40.7128, geo.latitude, 1e-9)
        assertEquals(-74.006, geo.longitude, 1e-9)
        assertEquals("Grand Hotel", geo.defaultTitle)
    }

    @Test fun geoWithoutQueryTitle() {
        assertEquals("1.5, 2.5", parse<ScanContent.Geo>("geo:1.5,2.5").defaultTitle)
    }

    @Test fun invalidGeoIsText() {
        parse<ScanContent.Text>("geo:abc,def")
        parse<ScanContent.Text>("geo:95,10")
    }

    // Events

    @Test fun vEvent() {
        val event = parse<ScanContent.Event>(
            "BEGIN:VEVENT\nSUMMARY:Breakfast buffet\nLOCATION:Terrace\\, floor 2\n" +
                "DTSTART:20261030T070000Z\nDTEND:20261030T100000Z\nEND:VEVENT",
        )
        assertEquals("Breakfast buffet", event.summary)
        assertEquals("Terrace, floor 2", event.location)
        assertEquals("20261030T070000Z", event.start)
        assertEquals("20261030T100000Z", event.end)
    }

    @Test fun vCalendarWrapper() {
        val event = parse<ScanContent.Event>("BEGIN:VCALENDAR\nBEGIN:VEVENT\nSUMMARY:Spa\nEND:VEVENT\nEND:VCALENDAR")
        assertEquals("Spa", event.defaultTitle)
    }

    @Test fun eventWithTzidParameter() {
        val event = parse<ScanContent.Event>("BEGIN:VEVENT\nDTSTART;TZID=Europe/Paris:20261030T090000\nEND:VEVENT")
        assertEquals("20261030T090000", event.start)
        assertEquals("Event", event.defaultTitle)
    }

    // Text

    @Test fun textTitleIsFirstNonBlankLine() {
        assertEquals("Room 12", parse<ScanContent.Text>("\n  \nRoom 12\nSecond line").defaultTitle)
    }

    @Test fun longTextTitleIsTruncated() {
        val title = parse<ScanContent.Text>("a".repeat(200)).defaultTitle
        assertEquals(60, title.length)
        assertTrue(title.endsWith("…"))
    }

    @Test fun emptyText() {
        assertEquals("Text", parse<ScanContent.Text>("").defaultTitle)
    }

    @Test fun rawIsKeptExactly() {
        val raw = "  https://example.com  "
        assertEquals(raw, ContentParser.parse(raw).raw)
    }

    // Helpers

    @Test fun percentDecodeHandlesMalformedAndUnicode() {
        assertEquals("100%", ContentParser.percentDecode("100%"))
        assertEquals("%zz", ContentParser.percentDecode("%zz"))
        assertEquals("%-1", ContentParser.percentDecode("%-1"))
        assertEquals("café", ContentParser.percentDecode("caf%C3%A9"))
        assertEquals("+1 👋", ContentParser.percentDecode("+1%20👋"))
    }
}
