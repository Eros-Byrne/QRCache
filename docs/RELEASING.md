# Releasing QRCache

This guide covers building a signed release and publishing it on Google Play.
Store text and images live in [`fastlane/metadata/android/en-US`](../fastlane/metadata/android/en-US)
(the layout that fastlane `supply` and F-Droid both read), so you can copy them into Play Console
or upload them with fastlane later.

## 1. One-time setup

### Create the upload key

Google Play signs the app that users download ("Play App Signing"). You sign each upload
with your own **upload key**. Create it once on your computer (needs a JDK):

```sh
keytool -genkeypair -v -keystore qrcache-upload.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Keep `qrcache-upload.jks` and its passwords in a password manager. **Never commit it.** If you lose it,
Google can reset the upload key, but that takes a support request.

### Add the key to GitHub

In the repo, open **Settings → Secrets and variables → Actions** and add:

| Secret | Value |
| --- | --- |
| `QRCACHE_KEYSTORE_BASE64` | Output of `base64 -w0 qrcache-upload.jks` (macOS: `base64 -i qrcache-upload.jks`) |
| `QRCACHE_KEYSTORE_PASSWORD` | The keystore password |
| `QRCACHE_KEY_ALIAS` | `upload` |
| `QRCACHE_KEY_PASSWORD` | The key password |

### Create the Play Console app

1. Sign up for a [Google Play developer account](https://play.google.com/console/signup) (one-time fee).
2. **Create app**: name *QRCache*, default language English (US), App, Free.
   Free is permanent: a free app can't be made paid later.
3. The package name is `app.qrcache`. It is set by the first upload and can never change.
   If Play says it's taken, change `applicationId` in `app/build.gradle.kts` before uploading.

## 2. Build a release

1. Update `versionCode` (+1 every upload) and `versionName` in `app/build.gradle.kts`,
   the `CHANGELOG.md`, and add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`.
2. Merge to `main`, then tag it:
   ```sh
   git tag v1.0.0 && git push origin v1.0.0
   ```
3. The **Release** workflow builds and signs:
   - `app-release.aab`, the bundle you upload to Play
   - `app-release.apk`, attached to the GitHub release for sideloading
   - `mapping.txt`, which lets Play show readable crash reports

   Download them from the workflow run's **Artifacts**. You can also run the workflow by hand
   from the Actions tab.

To build locally instead, set the four values as environment variables (`QRCACHE_KEYSTORE_FILE`
is the path to the `.jks`) and run `./gradlew bundleRelease`.

### Testing the shrunk build before release

Every CI run also uploads `qrcache-release-test-apk`. It is the release build (R8-shrunk, like the
one on Play) signed with a throwaway debug key. Install it on a phone to check nothing broke in
shrinking. Uninstall other QRCache builds first, since each is signed with a different key.

## 3. Store listing

| Play Console field | Source |
| --- | --- |
| App name (30) | `title.txt` |
| Short description (80) | `short_description.txt` |
| Full description (4000) | `full_description.txt` |
| App icon 512×512 | `images/icon.png` |
| Feature graphic 1024×500 | `images/featureGraphic.png` |
| Phone screenshots (2–8) | `images/phoneScreenshots/` |
| Category | Tools |
| Privacy policy URL | https://github.com/Eros-Byrne/QRCache/blob/main/docs/PRIVACY.md |
| Contact email | Your support email (required, shown publicly) |

`icon.png` and `featureGraphic.png` are drawn by code from the launcher icon's shapes. Suggested
screenshots, taken on a phone in light mode: Scan screen, scan result sheet, History with a few
saved codes, search or filters, Settings, and one in dark mode.

## 4. App content (Policy → App content)

These answers match what the app does today. Re-check them whenever a feature touches the network
or new data.

- **Privacy policy:** the URL above.
- **Ads:** No.
- **App access:** All functionality is available without special access.
- **Content rating:** fill in the questionnaire as a *Utility* app with no user-generated content
  shared with others, no violence and so on. Expect *Everyone*.
- **Target audience:** 13 and over. The app isn't designed for children; picking under-13 ages
  adds Families policy requirements.
- **Data safety:**
  - *Does your app collect or share any of the required user data types?* **No.**
    History is stored only on the device, and camera frames are decoded on the device and never stored.
  - The optional *Fetch page titles* setting downloads a page from a link the user scanned. That is
    the user opening their own link, and nothing goes to us, so it is not collection.
  - *Is data encrypted in transit?* The only network use is HTTPS.
  - *Can users request deletion?* Users delete data in the app (swipe to delete, Clear history) or by uninstalling.
- **Permissions:** Camera (scanning). Internet (only for the optional page-title setting).

## 5. Roll out

1. **Internal testing:** upload the `.aab` and add your own Google account as a tester. Install from
   the Play link and check scanning, history and settings.
2. **Closed testing:** Google currently requires new personal developer accounts to run a closed
   test with at least 12 testers for 14 days before production access opens. Check Play Console's
   dashboard for the exact requirement on your account.
3. **Production:** promote the tested release. Start with a staged rollout (for example 20%) and
   watch **Android vitals** for crashes before going to 100%.
