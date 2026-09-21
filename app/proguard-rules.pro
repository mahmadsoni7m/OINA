# OINA - Digital Mirror
# Keep CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }

# Keep our model/settings classes fully (safety in a small app)
-keep class com.soni.oina.settings.** { *; }
