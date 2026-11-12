# Architecture

## Stack

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for Android |
| UI | Jetpack Compose + Material 3 (restyled, see [DESIGN.md](DESIGN.md)) | Fast to build smooth, animated UI |
| Camera | CameraX | Handles device quirks across old and new phones |
| Code reading | ML Kit Barcode Scanning, **bundled** model (QR codes and barcodes) | Fast, works offline, no download on first use, works without Google Play services |
| Database | Room (SQLite) | Reliable local storage |
| Settings | DataStore (Preferences) | Simple key-value settings |
| Async | Kotlin Coroutines + Flow | UI updates automatically when history changes |
| Navigation | A tab state in `QRCacheApp` with a crossfade | Three tabs plus a sheet; no navigation library needed |
| Dependency injection | Manual (one `AppContainer`) | Small app; no framework needed |
| Links | Custom Tabs (AndroidX Browser) | Opens menus fast without leaving the app |
| Build | Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) | One place for versions |

## Android version support

| Setting | Value |
|---|---|
| `minSdk` | 23 (Android 6.0) |
| `targetSdk` / `compileSdk` | Latest stable |

Current AndroidX and Compose releases require API 23 or newer, so this is the oldest version we can support without freezing on old libraries. It covers about 99% of active Android devices.

Handling older versions:

- **Dates:** use `java.time` with core library desugaring enabled (needed below API 26).
- **Blur on bars:** API 31+ only; older versions get a solid translucent color.
- **Themed (monochrome) icon:** API 33+; older versions use the normal adaptive icon.
- **Image picker:** use `PickVisualMedia`, which falls back to the system file picker on older devices.
- **Notifications / permissions:** only camera permission is needed. Request at runtime (API 23+ model).
- **Edge-to-edge:** use `enableEdgeToEdge()`, which handles insets consistently across versions (and is required on Android 15+).

## Module layout

A single `app` module with clear packages.

```
app/src/main/java/app/qrcache/
├── QRCacheApplication.kt    Application, creates AppContainer
├── MainActivity.kt          Single activity, hosts Compose
├── QRCacheApp.kt            App shell: the three tabs
├── AppContainer.kt          Wires repository, database, settings, title fetcher
├── data/
│   ├── db/                  Room database (with migration), DAO, entity
│   ├── ScanRepository.kt    Only entry point for reading/writing scans
│   ├── ScanType.kt
│   ├── SettingsRepository.kt  DataStore-backed settings
│   ├── HistoryBackup.kt     JSON and CSV export, JSON import
│   ├── TitleFetcher.kt      Opt-in page title lookup (HTTPS, public addresses only)
│   └── PageTitle.kt         Pure title parsing and address checks
├── scan/
│   ├── ScanScreen.kt        Permission gate, QR code / Barcode switch, viewfinder, scan from image
│   ├── CameraPreview.kt     CameraX controller + ML Kit analyzer
│   ├── ContentParser.kt     Raw text → typed content (URL, Wi-Fi, vCard, …)
│   ├── IcsDate.kt           Calendar event date parsing
│   ├── ScanDebouncer.kt     Ignores the same code for 2 s
│   ├── ScanActions.kt       Open, join Wi-Fi, add contact, share, copy
│   └── ScanContentUi.kt     Icon and label per content type
├── history/
│   ├── HistoryScreen.kt
│   └── HistoryList.kt       Pure grouping, search and filter logic
├── detail/
│   └── ScanDetailSheet.kt   Saved scan: actions, rename, favorite, delete
├── settings/
│   └── SettingsScreen.kt
├── ui/
│   ├── theme/               Color, Type, Shape, Motion tokens
│   └── components/          Shared composables from DESIGN.md
└── util/
    └── ContextExt.kt
```

## Data model

```kotlin
@Entity(tableName = "scans", indices = [Index(value = ["raw"], unique = true)])
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raw: String,               // exact decoded text
    val type: ScanType,            // LINK, TEXT, WIFI, CONTACT, EMAIL, PHONE, SMS, GEO, EVENT
    val title: String,             // user-editable display name
    val titleIsCustom: Boolean,    // true once the user renames it
    val firstScannedAt: Long,      // epoch millis
    val lastScannedAt: Long,
    val scanCount: Int = 1,
    val favorite: Boolean = false,
    val barcode: Boolean = false,  // read in Barcode mode rather than as a QR code (added in v1.1, database v2)
)
```

- `raw` is unique: rescanning the same code updates `lastScannedAt` and `scanCount` instead of inserting a duplicate.
- Search filters the loaded list in memory (title and content), which is instant for thousands of scans. Move to an FTS table if histories ever get much bigger.
- History is sorted by `lastScannedAt` descending.
- Schema changes go through a Room migration and a migration test (`DatabaseMigrationTest` covers v1 → v2). Room exports schemas to `app/schemas/` at build time.

## Scan flow

```
CameraX preview ─▶ ImageAnalysis (latest frame only, ~720p)
                     │
                     ▼
            MlKitAnalyzer (ML Kit) ── no code ─▶ next frame
                     │ code found
                     ▼
     debounce: ignore the same value for 2s
                     │
                     ▼
     ContentParser → type + default title
                     │
                     ▼
     ScanRepository.upsert() ─▶ Room
                     │
                     ▼
     haptic + success animation, open ScanDetailSheet
```

## Performance

- The camera is bound to the screen's lifecycle and the preview shows before anything else loads.
- Analysis uses `STRATEGY_KEEP_ONLY_LATEST` so slow phones never fall behind.
- History list uses `LazyColumn` with stable keys (`id`).
- R8 full mode in release builds (ML Kit keep rules are in `app/proguard-rules.pro`).

## Privacy and network

- No analytics, crash-reporting or ad SDKs.
- The only network use is opening links the user taps, and, if the user turns it on, fetching a page title for new links.
- Android Auto Backup is limited to settings; the history database is excluded unless the user exports it.

## Testing

- Unit tests (`./gradlew testDebugUnitTest`): `ContentParser` (every content type, malformed input), calendar dates, scan debouncing, history grouping and filters, page title parsing, export/import, color contrast, and, with Robolectric, the repository's upsert/duplicate logic and the database migration.
- CI (`.github/workflows/android.yml`) builds, lints and runs the unit tests, then installs the release APK on an emulator and checks that it launches.
- Manual: real codes (link, Wi-Fi, vCard, long text, barcodes) checked on a phone before each release.

## Path to iOS

An iOS version was never built, but the code leaves the door open: business logic (`ContentParser`, history grouping, data model) has no Android imports, so a fork could move it to a Kotlin Multiplatform shared module and put a SwiftUI or Compose Multiplatform UI on top. See [ROADMAP.md](ROADMAP.md#ideas-not-built).
