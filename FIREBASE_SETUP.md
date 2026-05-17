# Firebase Setup Guide for GeoSurvey Pro

Follow these steps to complete the Firebase integration:

## 1. Firebase Project Creation
- Go to the [Firebase Console](https://console.firebase.google.com/).
- Create a new project named "GeoSurvey Pro".
- Enable **Google Analytics** for the project (optional but recommended).

## 2. Android App Configuration
- Register your Android app with package name `com.geosurvey.pro`.
- Download the `google-services.json` file.
- Place `google-services.json` in the `app/` directory of your project.

## 3. Enable Firebase Services
In the Firebase Console, enable the following:
- **Authentication**: Enable Email/Password provider.
- **Firestore Database**: Create a database in "Start in test mode" (update rules for production later).
- **Storage**: Create a storage bucket.
- **Cloud Messaging**: Automatically enabled, but ensure you follow the notification permissions for Android 13+.

## 4. Firestore Collections Structure
The app is designed to work with the following structure:
- `users/`: `{ id, email, displayName, profileImageUrl }`
- `surveys/`: `{ id, title, description, creatorId, createdAt }`
- `responses/`: `{ id, surveyId, respondentId, answers: {}, timestamp }`

## 5. Security Rules (Basic)
For development, you can use:
```
allow read, write: if request.auth != null;
```
Update these rules before production to ensure data privacy.
