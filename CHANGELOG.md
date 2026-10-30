# Changelog

All notable changes to QRCache are listed here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
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
