# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Uncomment this to hide the original source file name.
-renamesourcefileattribute SourceFile

# ==========================================
# Kotlin
# ==========================================
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

# ==========================================
# Jetpack Compose
# ==========================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class *

# ==========================================
# Room
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class androidx.room.paging.** { *; }
-dontwarn androidx.room.paging.**

# ==========================================
# Gson
# ==========================================
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep data classes used for JSON serialization/deserialization
-keep class com.compose.yifeng2fa.data.TotpEntity { *; }
-keep class com.compose.yifeng2fa.data.PasswordEntity { *; }
-keep class com.compose.yifeng2fa.data.StrongPasswordHistoryEntity { *; }
-keep class com.compose.yifeng2fa.utils.TotpData { *; }

# ==========================================
# Navigation Compose (Sealed classes / Enums)
# ==========================================
-keep class com.compose.yifeng2fa.ui.Screen { *; }
-keep class com.compose.yifeng2fa.ui.BottomNavItem { *; }
-keepclassmembers enum com.compose.yifeng2fa.viewmodel.SortOrder { *; }

# ==========================================
# CameraX & ML Kit
# ==========================================
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
-keep class com.google.mlkit.vision.barcode.** { *; }

# ==========================================
# Biometric
# ==========================================
-keep class androidx.biometric.** { *; }

# ==========================================
# Guava
# ==========================================
-dontwarn com.google.common.base.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-keep class com.google.common.base.** {*;}

# ==========================================
# Apache Commons Codec
# ==========================================
-keep class org.apache.commons.codec.** { *; }
