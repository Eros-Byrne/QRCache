# Roadmap

How QRCache was built, one milestone at a time. Each milestone ended with a build that worked end to end.

**Status: finished.** Every milestone through v1.1 is done, and v0.2 onward each have an APK on the [Releases page](https://github.com/Eros-Byrne/QRCache/releases). v1.1.0 is the final release. No further work is planned; the ideas that didn't make it are listed at the bottom.

## v0.1: Skeleton ✓
- Android project (Kotlin, Compose, version catalog, `minSdk 23`)
- Theme tokens from [DESIGN.md](DESIGN.md), tab bar, empty screens
- CI on GitHub Actions: build, lint, unit tests

## v0.2: Scan ✓
- Camera permission flow
- CameraX + ML Kit scanning, haptic + success animation
- Scan from image
- `ContentParser` for all v1 types with unit tests

## v0.3: History ✓
- Room database, auto-save every scan, duplicate handling
- History list with date groups, search, filter chips
- Detail sheet with type-based actions, rename, delete, favorite

## v0.4: Settings and polish ✓
- Settings screen, theme, export/import, clear history
- Animations, empty states, accessibility pass
- Test on old and current Android versions

## v1.0: Release ✓
- Optional page titles for links
- App icon (with Android 13 themed icon), Play Store icon and feature graphic, screenshots, store listing
- Privacy policy published ([PRIVACY.md](PRIVACY.md))
- Signed release builds and a release workflow ([RELEASING.md](RELEASING.md)); APKs published on GitHub Releases

## v1.1: Barcodes ✓
- Barcode formats beyond QR (EAN, UPC, Code 128, Code 39, Code 93, Codabar, ITF), from the camera or an image
- All / QR codes / Barcodes switch in History

## Ideas not built

These were on the list at some point but are outside the finished app. They're left here in case someone wants to fork QRCache and take it further.

- Home screen widget and Quick Settings tile
- Notes on scans
- Folders or tags
- Opt-in scan location
- Create your own QR codes
- Optional backup to the user's own Google Drive
- Google Play listing (the steps are in [RELEASING.md](RELEASING.md))
- iOS app (Kotlin Multiplatform shared logic; see [ARCHITECTURE.md](ARCHITECTURE.md#path-to-ios))
