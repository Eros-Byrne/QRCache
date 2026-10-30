package app.qrcache.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTitleTest {

    @Test fun prefersOgTitle() {
        val html = """<html><head><title>Menu | Grand Hotel</title>
            <meta property="og:title" content="Breakfast Menu"></head></html>"""
        assertEquals("Breakfast Menu", extractPageTitle(html))
    }

    @Test fun ogTitleAttributeOrderAndQuotes() {
        assertEquals("Spa", extractPageTitle("<meta content='Spa' name='og:title'/>"))
    }

    @Test fun fallsBackToTitleTag() {
        assertEquals("Grand Hotel – Breakfast", extractPageTitle("<TITLE data-x=1>\n  Grand Hotel &ndash; Breakfast\n</TITLE>"))
    }

    @Test fun blankOgFallsBackToTitle() {
        assertEquals("Menu", extractPageTitle("""<meta property="og:title" content="  "><title>Menu</title>"""))
    }

    @Test fun noTitle() {
        assertNull(extractPageTitle("<html><body>hi</body></html>"))
        assertNull(extractPageTitle("<title>   </title>"))
    }

    @Test fun entities() {
        assertEquals("Tom & Jerry's \"Café\" ©", decodeEntities("Tom &amp; Jerry&#39;s &quot;Caf&#xE9;&quot; &copy;"))
        assertEquals("&bogus; &#0; &#99999999;", decodeEntities("&bogus; &#0; &#99999999;"))
    }

    @Test fun longTitleIsCut() {
        val title = extractPageTitle("<title>${"a".repeat(300)}</title>")!!
        assertEquals(100, title.length)
        assertTrue(title.endsWith("…"))
    }

    @Test fun onlyPublicHttps() {
        assertTrue(isSafeToFetch("https://menu.hotel.com/breakfast"))
        assertTrue(isSafeToFetch("HTTPS://Example.com"))
        assertTrue(isSafeToFetch("https://8.8.8.8/"))
        assertFalse(isSafeToFetch("http://menu.hotel.com"))
        assertFalse(isSafeToFetch("ftp://x.com"))
        assertFalse(isSafeToFetch("https://localhost/x"))
        assertFalse(isSafeToFetch("https://printer.local/"))
        assertFalse(isSafeToFetch("https://router.lan/"))
        assertFalse(isSafeToFetch("https://192.168.1.1/reboot"))
        assertFalse(isSafeToFetch("https://10.0.0.5/"))
        assertFalse(isSafeToFetch("https://172.20.1.1/"))
        assertFalse(isSafeToFetch("https://127.0.0.1:8443/"))
        assertFalse(isSafeToFetch("https://169.254.169.254/latest"))
        assertFalse(isSafeToFetch("https://100.64.0.1/"))
        assertFalse(isSafeToFetch("https://[::1]/"))
        assertFalse(isSafeToFetch("https://[fd00::1]/"))
        assertFalse(isSafeToFetch("not a url"))
        assertTrue(isSafeToFetch("https://172.32.0.1/"))
    }
}
