# GeoSurvey Pro Production Readiness Guide

## 1. Testing Strategy
- **Unit Tests**: Coverage for `ValidationUtils`, `BaseViewModel` state transitions, and Repository logic.
- **UI Tests**: Instrumented tests for Authentication flow and Survey submission.
- **Firebase Test Lab**: Recommended for testing across multiple physical devices in the cloud.

## 2. Monitoring & Logging
- **Firebase Crashlytics**: Automatically tracks crashes and non-fatal exceptions.
- **Timber**: Used for structured logging. Only plants `DebugTree` in debug builds to prevent leaking info in production.
- **Firebase Analytics**: Tracks user engagement and survey completion funnels.

## 3. Performance Optimization
- **R8/Proguard**: Enabled in `release` build to obfuscate code and reduce APK size.
- **Resource Shrinking**: Enabled to remove unused resources.
- **Startup**: Lazy initialization of heavy SDKs (like Maps) where possible.
- **Battery**: Fused Location Provider is optimized; avoid high-frequency updates when the app is in background unless strictly necessary.

## 4. APK/AAB Generation
1. Go to `Build` > `Generate Signed Bundle / APK...`
2. Select `Android App Bundle` (recommended for Play Store).
3. Create/select your keystore.
4. Select `release` destination folder.

## 5. Deployment Checklist
- [ ] Update `versionCode` and `versionName` in `app/build.gradle.kts`.
- [ ] Ensure `google-services.json` is production-ready (not dev/test project).
- [ ] Update Firestore and Storage rules for production (no open access).
- [ ] Add production Google Maps API key to `secrets.properties`.
- [ ] Perform a final check of all "Required" fields in survey creation.

## 6. CI/CD Suggestions
- **GitHub Actions**: Automate builds and run tests on every pull request.
- **Firebase App Distribution**: Automatically push new builds to internal testers.

## Secrets and demo accounts

- Put `MAPS_API_KEY` in `secrets.properties` (git-ignored). Copy `secrets.properties.example` to start.
  `local.defaults.properties` holds a placeholder so builds still succeed without it.
- Restrict the Maps key in Google Cloud Console to this app's package name and SHA-1 fingerprint, and
  restrict the Firebase key in `google-services.json` the same way.
- Demo accounts (`DemoAccounts.kt`) are only created in **debug** builds. They have well-known passwords:
  delete them from Firebase Authentication on any project that real users share.
- Admins are created from the Firebase console by setting `role: "admin"` on `users/{uid}`. The security
  rules deliberately stop users from setting their own role, so the debug demo seeder can no longer
  promote itself; set the demo admin's role by hand (or use the emulator).
- Deploy rule changes with `firebase deploy --only firestore:rules`.
