package app.qrcache.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanDebouncerTest {

    @Test fun firstValueIsNew() {
        assertTrue(ScanDebouncer().accept("a", now = 0))
    }

    @Test fun sameValueHeldInViewIsIgnored() {
        val d = ScanDebouncer(windowMillis = 2_000)
        d.accept("a", 0)
        assertFalse(d.accept("a", 500))
        assertFalse(d.accept("a", 2_400)) // still in view, seen 1.9s ago
    }

    @Test fun sameValueAfterGapIsNew() {
        val d = ScanDebouncer(windowMillis = 2_000)
        d.accept("a", 0)
        assertTrue(d.accept("a", 2_001))
    }

    @Test fun differentValueIsNew() {
        val d = ScanDebouncer()
        d.accept("a", 0)
        assertTrue(d.accept("b", 10))
        assertTrue(d.accept("a", 20))
    }
}
