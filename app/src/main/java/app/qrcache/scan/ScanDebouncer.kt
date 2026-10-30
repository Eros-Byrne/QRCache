package app.qrcache.scan

/**
 * Stops the same code from firing again while it stays in front of the camera.
 * A value counts as new if it differs from the last one, or the last one hasn't
 * been seen for [windowMillis].
 */
class ScanDebouncer(private val windowMillis: Long = 2_000) {
    private var lastValue: String? = null
    private var lastSeenAt = Long.MIN_VALUE

    fun accept(value: String, now: Long): Boolean {
        val isNew = value != lastValue || now - lastSeenAt > windowMillis
        lastValue = value
        lastSeenAt = now
        return isNew
    }
}
