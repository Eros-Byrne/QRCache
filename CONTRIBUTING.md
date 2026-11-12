# Contributing

Thanks for your interest in QRCache. The app is finished (v1.1.0 is the final release), so no new features are planned. Bug reports and small fixes are still welcome, and you're free to fork it and build on it under the Apache 2.0 license. This guide covers how work gets done in this repo.

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
- Compose: one screen per file, stateless composables where possible.
- Follow [DESIGN.md](docs/DESIGN.md) for spacing, color and motion. Use the theme tokens, not hard-coded values.

## Tests

- Unit tests for parsing, history and data logic: `./gradlew testDebugUnitTest` (database tests run on Robolectric, so no device is needed)
- A change to the database schema needs a Room migration and a migration test (see `DatabaseMigrationTest`).

## Pull request checklist

- [ ] Builds and tests pass
- [ ] Tried on an old and a new Android version
- [ ] Works in light and dark mode
- [ ] `CHANGELOG.md` updated under **Unreleased**
