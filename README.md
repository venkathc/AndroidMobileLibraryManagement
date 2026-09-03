# Personal Library Management

Offline-first native Android library manager built with Kotlin, Jetpack Compose, and Room. The app stores its catalogue, loan history, wishlist, and reports locally on the device.

## Android app

The Android source and Android-specific setup instructions are in [android/README.md](android/README.md).

## Build and install

```bash
cd android
gradle :app:installRelease
```

The installed application ID is `com.venkateshgowda.personallibrary`.

## Legacy import fixture

`legacy-library-import.zip` is retained for validating the Android legacy-catalogue import workflow.
