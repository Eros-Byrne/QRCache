# Contributing

Thanks for helping with QRCache. This guide covers how work gets done in this repo.

## Setup

- Latest stable Android Studio
- JDK 17 (bundled with Android Studio)
- An Android device or emulator. Test on at least one old (Android 6–8) and one current device or emulator before opening a pull request that touches UI or the camera.

## Branches

- `main` is always buildable.
- Work on a branch named `feature/<short-name>`, `fix/<short-name>` or `docs/<short-name>`.
- Open a pull request into `main`. Keep it focused on one change.

## Commits

Short, present-tense summaries: `Add rename dialog to history item`, not `added stuff`.

## Code style

- Kotlin official style. Run `./gradlew lintDebug testDebugUnitTest` before pushing (CI runs the same).
- Compose: one screen per file, stateless composables where possible, previews for every component.
- Follow [DESIGN.md](docs/DESIGN.md) for spacing, color and motion. Use the theme tokens, not hard-coded values.

## Tests

- Unit tests for view models and data logic: `./gradlew test`
- UI tests for core flows (scan → saved → reopen, rename, delete, search): `./gradlew connectedAndroidTest`
- A change to the database schema needs a Room migration and a migration test.

## Pull request checklist

- [ ] Builds and tests pass
- [ ] Tried on an old and a new Android version
- [ ] Works in light and dark mode
- [ ] `CHANGELOG.md` updated under **Unreleased**
