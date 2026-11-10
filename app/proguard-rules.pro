# ML Kit finds its parts at runtime, and R8 full mode strips or merges them,
# which crashed the release build on launch (NullPointerException in
# BarcodeScanning.getClient). Keep ML Kit and its Play services internals whole.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode_bundled.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_common.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }
-keep class com.google.firebase.components.** { *; }
-keep class com.google.android.libraries.barhopper.** { *; }
-keep class com.google.photos.vision.barhopper.** { *; }
