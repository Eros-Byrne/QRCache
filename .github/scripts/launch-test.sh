#!/usr/bin/env bash
# Installs the shrunk release APK (or the APK given as the first argument) on the
# emulator, opens the app and fails if it crashes.
set -x
apk="${1:-$(ls app/build/outputs/apk/release/*.apk | head -1)}"
adb logcat -c
adb install -r "$apk"
adb shell pm grant app.qrcache android.permission.CAMERA
adb shell am start -W -n app.qrcache/.MainActivity
sleep 15
# The emulator's adb link can drop for a moment ("device offline"). Wait for it, so a
# lost connection isn't mistaken for the app having closed.
adb wait-for-device
adb logcat -d > logcat.txt
running=false
for _ in 1 2 3; do
  if adb shell pidof app.qrcache; then running=true; break; fi
  adb wait-for-device
  sleep 2
done
if grep -q "FATAL EXCEPTION" logcat.txt || [ "$running" != true ]; then
  echo "::error::The release build crashed on launch."
  grep -A40 "FATAL EXCEPTION" logcat.txt | sed 's/^.*AndroidRuntime: //' > crash.txt
  mapping=app/build/outputs/mapping/release/mapping.txt
  if [ -z "${1:-}" ] && [ -f "$mapping" ]; then
    "$(ls -d "$ANDROID_HOME"/cmdline-tools/*/bin/retrace | head -1)" "$mapping" crash.txt
  else
    cat crash.txt
  fi
  exit 1
fi
echo "The release build opened and kept running."
