# Architecture

## Stack

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for Android |
| UI | Jetpack Compose + Material 3 (restyled, see [DESIGN.md](DESIGN.md)) | Fast to build smooth, animated UI |
| Camera | CameraX | Handles device quirks across old and new phones |
| QR decoding | ML Kit Barcode Scanning, **bundled** model | Fast, works offline, no download on first use, works without Google Play services |
| Database | Room (SQLite) with an FTS4 table for search | Reliable local storage, instant full-text search |
| Settings | DataStore (Preferences) | Simple key-value settings |
| Async | Kotlin Coroutines + Flow | UI updates automatically when history changes |
| Navigation | Navigation Compose | Three tabs plus a sheet |
| Dependency injection | Manual (one `AppContainer`) | Small app; no framework needed yet |
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
- Test matrix: emulators for API 23, 26, 29, 33 and latest, plus one low-end physical device.

## Module layout

Start as a single `app` module with clear packages. Split into modules only if build times or the iOS port call for it.

```
app/src/main/java/app/qrcache/
├── QRCacheApp.kt            Application, creates AppContainer
├── MainActivity.kt          Single activity, hosts Compose
├── AppContainer.kt          Wires repository, database, settings
├── data/
│   ├── db/                  Room database, DAO, entities, migrations
│   ├── ScanRepository.kt    Only entry point for reading/writing scans
│   └── SettingsRepository.kt
├── scan/
│   ├── ScanScreen.kt
│   ├── ScanViewModel.kt
│   ├── QrAnalyzer.kt        CameraX ImageAnalysis → ML Kit
│   └── ContentParser.kt     Raw text → typed content (URL, Wi-Fi, vCard, …)
├── history/
│   ├── HistoryScreen.kt
│   └── HistoryViewModel.kt
├── detail/
│   ├── ScanDetailSheet.kt
│   └── ScanActions.kt       Open, join Wi-Fi, add contact, share, copy
├── settings/
│   └── SettingsScreen.kt
└── ui/
    ├── theme/               Color, Type, Shape, Motion tokens
    └── components/          Shared composables from DESIGN.md
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
    val firstScannedAt: Instant,
    val lastScannedAt: Instant,
    val scanCount: Int = 1,
    val favorite: Boolean = false,
)
```

- `raw` is unique: rescanning the same code updates `lastScannedAt` and `scanCount` instead of inserting a duplicate.
- An FTS4 table mirrors `title` and `raw` for search.
- History is sorted by `lastScannedAt` descending.
- Schema changes go through Room migrations with exported schemas in `app/schemas/` and a migration test.

## Scan flow

```
CameraX preview ─▶ ImageAnalysis (latest frame only, ~720p)
                     │
                     ▼
               QrAnalyzer (ML Kit) ── no code ─▶ next frame
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

- Camera binds in `onStart` and the preview shows before anything else loads.
- Analysis uses `STRATEGY_KEEP_ONLY_LATEST` so slow phones never fall behind.
- History list uses `LazyColumn` with stable keys (`id`) and paging if history grows past a few thousand rows.
- Baseline Profile and R8 full mode in release builds.
- Target cold start under 1 second to camera preview on a mid-range phone.

## Privacy and network

- No analytics, crash-reporting or ad SDKs.
- The only network use is opening links the user taps, and, if the user turns it on, fetching a page title for new links.
- Android Auto Backup is limited to settings; the history database is excluded unless the user exports it. (Revisit when optional Drive backup is added.)

## Testing

- Unit: `ContentParser` (every QR content type, malformed input), repository upsert/duplicate logic, view models.
- Instrumented: Room DAO and migrations, core UI flows with Compose UI tests.
- Manual: a printed sheet of test QR codes (link, Wi-Fi, vCard, long text, tiny, damaged) checked on each test device.

## Path to iOS

Keep business logic (`ContentParser`, repository rules, data model) free of Android imports so it can move to a Kotlin Multiplatform shared module later. The iOS UI can then be SwiftUI, or Compose Multiplatform if sharing UI proves worth it. Decide at the v2 milestone; see [ROADMAP.md](ROADMAP.md).
