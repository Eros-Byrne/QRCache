#!/usr/bin/env bash
# Installs the shrunk release APK on the emulator, opens the app and fails if it crashes.
set -x
apk="$(ls app/build/outputs/apk/release/*.apk | head -1)"
adb logcat -c
adb install -r "$apk"
adb shell pm grant app.qrcache android.permission.CAMERA
adb shell am start -W -n app.qrcache/.MainActivity
sleep 15
adb logcat -d > logcat.txt
if grep -q "FATAL EXCEPTION" logcat.txt || ! adb shell pidof app.qrcache; then
  echo "::error::The release build crashed on launch."
  grep -A40 "FATAL EXCEPTION" logcat.txt | sed 's/^.*AndroidRuntime: //' > crash.txt
  "$(ls -d "$ANDROID_HOME"/cmdline-tools/*/bin/retrace | head -1)" \
    app/build/outputs/mapping/release/mapping.txt crash.txt
  exit 1
fi
echo "The release build opened and kept running."
