# Changelog

All notable changes to QRCache are listed here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow [Semantic Versioning](https://semver.org/).

APKs for each version are on the [Releases page](https://github.com/Eros-Byrne/QRCache/releases).

## [Unreleased]

### Fixed
- CI now opens the release build on an emulator, so a crash on launch can't ship again.

### Added
- Barcode scanning: a "QR code / Barcode" switch at the top of the camera reads product and ticket barcodes (EAN, UPC, Code 128, Code 39, Code 93, Codabar, ITF), from the camera or from an image. Barcode scans are saved to History, and a "All / QR codes / Barcodes" switch at the top of History shows them separately. JSON and CSV exports record which kind each scan is.
- Install guide, screenshots and badges in the README.
- GitHub Releases with an APK for v0.2.0, v0.3.0, v0.4.0 and v1.0.0.

## [1.0.0] - 2026-10-30

The first full release: everything from the earlier milestones, plus link titles and Play Store prep.

### Fixed
- 2026-11-10: the APK was rebuilt because the first one closed straight away ("QRCache keeps stopping"). The release build's code shrinker broke the barcode reader library; it now keeps it intact. If you installed v1.0.0 before this date, uninstall it and install the new APK.

### Added
- Optional "Fetch page titles for links" setting (off by default). It names a scanned link after its web page's title, for example "Grand Hotel – Breakfast Menu" instead of "menu.hotel.com". It only fetches over HTTPS, never from local network addresses, at most 64 KB with a 5 s timeout, and it never overwrites a name you chose.
- Themed app icon on Android 13 and newer.
- Release setup: signed bundle and APK from a version tag (see [docs/RELEASING.md](https://github.com/Eros-Byrne/QRCache/blob/main/docs/RELEASING.md)), and a shrunk release APK from every CI run for testing.
- Store listing text, Play Store icon and feature graphic in `fastlane/metadata`.

## [0.4.0] - 2026-10-30

Settings, and a way to take your history with you.

### Added
- Settings: theme (System, Light, Dark), vibrate on scan, open links automatically, and open links inside QRCache or in your default browser.
- Export history as JSON (can be imported back) or CSV (for spreadsheets), import from JSON (skips codes you already have), and clear all history with confirmation.
- TalkBack reads each History row's type, and settings switches read as one control.

## [0.3.0] - 2026-10-30

Every scan is now saved, so you can find it again.

### Added
- History: every scan is saved on the device. Rescanning a code moves it to the top and counts it instead of adding a copy.
- History list grouped by Today, Yesterday, This week and Earlier, with search and filter chips (All, Favorites, Links, Wi-Fi, Text, Other).
- Swipe left to delete (with Undo), swipe right to favorite.
- Scan detail sheet with rename, favorite, delete, scan time and scan count. It opens after scanning and from History.
- Scan history is kept out of cloud backups. Phone-to-phone transfer still moves it.

## [0.2.0] - 2026-10-30

The first build that scans.

### Added
- Camera scanning: live QR scanning with CameraX and ML Kit (works offline), haptic tap and success animation, flashlight toggle.
- Scan from image: pick a photo or screenshot and read the code in it.
- Camera permission flow with a friendly screen and a settings shortcut if access is denied.
- Result sheet with type-based actions: open link, join Wi-Fi, add contact, send email, call, send message, open in Maps, add to calendar, plus Copy and Share.
- Content parser for links, Wi-Fi, vCard/MECARD contacts, email, phone, SMS, locations, calendar events and text, with unit tests.

## [0.1.0] - 2026-10-30

Project setup.

### Added
- App skeleton: Kotlin + Jetpack Compose project (Android 6.0+), design theme (colors, Inter type, spacing, motion), Scan / History / Settings tabs, launcher icon.
- GitHub Actions CI: build, lint, unit tests, downloadable debug APK.
- Project docs: README, features, design, architecture, roadmap, privacy policy, contributing guide.

[Unreleased]: https://github.com/Eros-Byrne/QRCache/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Eros-Byrne/QRCache/releases/tag/v1.0.0
[0.4.0]: https://github.com/Eros-Byrne/QRCache/releases/tag/v0.4.0
[0.3.0]: https://github.com/Eros-Byrne/QRCache/releases/tag/v0.3.0
[0.2.0]: https://github.com/Eros-Byrne/QRCache/releases/tag/v0.2.0
[0.1.0]: https://github.com/Eros-Byrne/QRCache/compare/709b173...9d1aabe
