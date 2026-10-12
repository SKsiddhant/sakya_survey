# Referenced by the release build type in app/build.gradle.kts (this file was missing, which
# breaks `assembleRelease`).

# Firestore maps documents onto these classes by reflecting on field names. If R8 renames or
# strips them, toObjects()/set() silently return empty or wrong data in release builds only.
-keep class com.geosurvey.pro.domain.model.** { *; }

# Keep line numbers so Crashlytics stack traces stay readable.
-keepattributes Signature, *Annotation*, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
