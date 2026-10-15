# Sakya Survey (GeoSurvey Pro)

An Android app for creating and taking location-aware surveys, with QR scanning, geofencing, offline storage and an analytics view.

## Tech stack

- Kotlin, Jetpack Compose, Navigation Compose
- Hilt for dependency injection, Room for local storage
- Firebase (Auth, Firestore, Storage, Cloud Messaging, Crashlytics)
- Google Maps (Maps Compose) and geofencing
- Min SDK 24, target/compile SDK 35 (application id `com.geosurvey.pro`)

## Setup

1. Clone the repo and open it in a recent Android Studio.
2. Create a Firebase project and register the app (`com.geosurvey.pro`). Download `google-services.json` into `app/`. It is git-ignored; `app/google-services.json.example` shows the shape. See [FIREBASE_SETUP.md](FIREBASE_SETUP.md).
3. Copy `secrets.properties.example` to `secrets.properties` (git-ignored) and set `MAPS_API_KEY` to your own Google Maps key, restricted to the package name and SHA-1.
4. Deploy `firestore.rules` to your Firestore project.
5. Build and run: `./gradlew assembleDebug`. Unit tests: `./gradlew testDebugUnitTest`.

Never commit `google-services.json`, `secrets.properties`, `local.properties` or signing keystores.

## More

- [PRODUCTION_GUIDE.md](PRODUCTION_GUIDE.md) covers release preparation.

Not built or run in the review environment; the steps above were checked against the project files only.
