# Changelog

All notable changes to QRCache are listed here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-10-30

First Play Store release.

### Added
- Release setup: signed bundle and APK from a version tag (see [docs/RELEASING.md](docs/RELEASING.md)), and a shrunk release APK from every CI run for testing.
- Store listing text, Play Store icon and feature graphic in `fastlane/metadata`.
- Themed app icon on Android 13 and newer.
- Optional "Fetch page titles for links" setting (off by default). It names a scanned link after its web page's title, for example "Grand Hotel – Breakfast Menu" instead of "menu.hotel.com". It only fetches over HTTPS, never from local network addresses, at most 64 KB with a 5 s timeout, and it never overwrites a name you chose.
- Settings (v0.4): theme (System, Light, Dark), vibrate on scan, open links automatically, and open links inside QRCache or in your default browser.
- Export history as JSON (can be imported back) or CSV (for spreadsheets), import from JSON (skips codes you already have), and clear all history with confirmation.
- TalkBack reads each History row's type, and settings switches read as one control.
- History (v0.3): every scan is saved on the device. Rescanning a code moves it to the top and counts it instead of adding a copy.
- History list grouped by Today, Yesterday, This week and Earlier, with search and filter chips (All, Favorites, Links, Wi-Fi, Text, Other).
- Swipe left to delete (with Undo), swipe right to favorite.
- Scan detail sheet with rename, favorite, delete, scan time and scan count. It opens after scanning and from History.
- Scan history is kept out of cloud backups. Phone-to-phone transfer still moves it.
- Camera scanning (v0.2): live QR scanning with CameraX and ML Kit (works offline), haptic tap and success animation, flashlight toggle.
- Scan from image: pick a photo or screenshot and read the code in it.
- Camera permission flow with a friendly screen and a settings shortcut if access is denied.
- Result sheet with type-based actions: open link, join Wi-Fi, add contact, send email, call, send message, open in Maps, add to calendar, plus Copy and Share.
- Content parser for links, Wi-Fi, vCard/MECARD contacts, email, phone, SMS, locations, calendar events and text, with unit tests.
- App skeleton: Kotlin + Jetpack Compose project (Android 6.0+), design theme (colors, Inter type, spacing, motion), Scan / History / Settings tabs, launcher icon.
- GitHub Actions CI: build, lint, unit tests, downloadable debug APK.
- Project docs: README, features, design, architecture, roadmap, privacy policy, contributing guide.
