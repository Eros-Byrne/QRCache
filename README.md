# QRCache

**Your QR codes, remembered.**

QRCache is a free Android app that scans QR codes and keeps a history of everything you've scanned, so you can open it again later without rescanning.

> You're at a hotel, scan the breakfast menu on the table, and walk away. The next morning you want it again. Open QRCache, and it's right there.

> **Status:** v1.0 is ready for the Play Store: scanning, history, settings and export all work. See the [roadmap](docs/ROADMAP.md) for what comes next.

## Features (v1)

- **Fast scanning.** Open the app and the camera is ready. Point it at a code, done.
- **Automatic history.** Every scan is saved with the date, time and a readable title.
- **Reopen anything.** Tap a past scan to open the link, copy the text, or join the Wi-Fi network.
- **Search.** Find a scan by title, content or website.
- **Rename and delete.** Give scans names that make sense to you ("Hotel breakfast menu"), and remove the ones you don't need.
- **Private by default.** History stays on your phone. No account, no ads, no tracking.

See [docs/FEATURES.md](docs/FEATURES.md) for the full list and what's planned later.

## Platform

| | |
|---|---|
| Platform | Android (iOS later, see [roadmap](docs/ROADMAP.md)) |
| Minimum Android | 6.0 Marshmallow (API 23) |
| Target Android | Latest stable release |
| Language | Kotlin |
| UI | Jetpack Compose |
| Price | Free |

## Docs

| Document | What's in it |
|---|---|
| [FEATURES.md](docs/FEATURES.md) | What the app does, screen by screen |
| [DESIGN.md](docs/DESIGN.md) | Look and feel: colors, type, motion, components |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Tech stack, code structure, data model |
| [ROADMAP.md](docs/ROADMAP.md) | Milestones from first build to iOS |
| [PRIVACY.md](docs/PRIVACY.md) | Privacy policy (also used for the Play Store listing) |
| [RELEASING.md](docs/RELEASING.md) | Signing, building and publishing on Google Play |
| [CONTRIBUTING.md](CONTRIBUTING.md) | How to build, branch and submit changes |
| [CHANGELOG.md](CHANGELOG.md) | Release history |

## Building

Once the app code lands:

1. Install the latest stable [Android Studio](https://developer.android.com/studio).
2. Clone this repo and open the folder in Android Studio.
3. Run the `app` configuration on a device or emulator.

Or from the command line:

```bash
./gradlew assembleDebug
./gradlew test
```

## License

Apache 2.0. See [LICENSE](LICENSE).
