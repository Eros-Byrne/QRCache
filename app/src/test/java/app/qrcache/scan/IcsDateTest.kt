package app.qrcache.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId

class IcsDateTest {
    private val paris = ZoneId.of("Europe/Paris")

    @Test fun utc() {
        assertEquals(1_793_343_600_000L, icsDateToEpochMillis("20261030T070000Z", paris))
    }

    @Test fun localTimeUsesZone() {
        // 08:00 in Paris on 2026-10-30 (CET, UTC+1) is 07:00 UTC
        assertEquals(1_793_343_600_000L, icsDateToEpochMillis("20261030T080000", paris))
    }

    @Test fun dateOnlyIsStartOfDay() {
        // Midnight Paris = 23:00 UTC the day before
        assertEquals(1_793_314_800_000L, icsDateToEpochMillis("20261030", paris))
    }

    @Test fun garbageIsNull() {
        assertNull(icsDateToEpochMillis("tomorrow", paris))
        assertNull(icsDateToEpochMillis("", paris))
    }
}
