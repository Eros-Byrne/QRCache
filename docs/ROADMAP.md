# Roadmap

Each milestone ends with a build that works end to end.

## v0.1: Skeleton
- Android project (Kotlin, Compose, version catalog, `minSdk 23`)
- Theme tokens from [DESIGN.md](DESIGN.md), tab bar, empty screens
- CI on GitHub Actions: build, lint, unit tests

## v0.2: Scan
- Camera permission flow
- CameraX + ML Kit scanning, haptic + success animation
- Scan from image
- `ContentParser` for all v1 types with unit tests

## v0.3: History
- Room database, auto-save every scan, duplicate handling
- History list with date groups, search, filter chips
- Detail sheet with type-based actions, rename, delete, favorite

## v0.4: Settings and polish
- Settings screen, theme, export/import, clear history
- Animations, empty states, accessibility pass
- Test on API 23, 26, 29, 33 and latest; Baseline Profile

## v1.0: Play Store release (free)
- App icon (with Android 13 themed icon), Play Store icon and feature graphic, screenshots, store listing
- Privacy policy published ([PRIVACY.md](PRIVACY.md))
- Signed release bundle, internal → closed → production testing tracks ([RELEASING.md](RELEASING.md))

## v1.1
- Home screen widget, Quick Settings tile
- Barcode formats beyond QR
- Notes on scans

## v1.2
- Folders/tags
- Opt-in scan location
- Create your own QR codes

## v2
- Optional backup to the user's own Google Drive
- iOS app (Kotlin Multiplatform shared logic; see [ARCHITECTURE.md](ARCHITECTURE.md#path-to-ios))
