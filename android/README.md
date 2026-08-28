# Personal Library Android App

Native, offline-first Android application for `Venkatesh Gowdas Personal Library`.

## Requirements

- Android Studio or a compatible Android SDK installation
- JDK 11
- Gradle available on `PATH`
- Android 10 (API 29) or later on the device

## Build

Run commands from this `android` directory.

```bash
gradle :app:assembleDebug
gradle :app:assembleRelease
```

APK outputs:

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Signed release: `app/build/outputs/apk/release/app-release.apk`

The release build requires local signing credentials. See [RELEASE_SIGNING.md](RELEASE_SIGNING.md).

## Install On A Device

Copy the release APK to the device, open it from the Files app, and select **Install** or **Update**. Android may ask you to allow the Files app to install unknown apps.

Install an updated APK over the existing application. Do **not** uninstall the app or clear its storage unless you want to remove its local catalogue.

For a connected device, install directly with:

```bash
gradle :app:installRelease
```

## Accounts and sign in

The app requires an account sign-in. The initial administrator account is `admin` with password `admin123`.

- The sign-in username field is a dropdown of existing accounts; select an account before entering its password.
- Password fields provide a show/hide control.
- Administrators can create users or reset user passwords from **Settings** > **Manage users**.
- **Switch user** keeps the current session available until another user signs in. Select **Cancel** on the switch sign-in screen to return to the app.
- **Sign out** closes the current app session.

## Backup and restore

Use **Settings** to create encrypted `.plb` backups containing the local database and book covers. Restore an encrypted backup from the same screen using its passphrase. Restoring replaces the local library data after validation and restarts the app.

## Multi-Library Support

Use **Library** to create and switch libraries.

- A library has a name, optional description and image, owner, and created date.
- The person entered during creation is stored as the library owner.
- Creating a library switches to it automatically.
- Books, wishlist items, loans, dashboard totals, search, reports, and exports use the selected library.
- Existing catalogues are retained in the migrated `Personal Library` after installing an update.

## Test

```bash
gradle :app:testDebugUnitTest
gradle :app:compileDebugKotlin
```

To run the focused price-input validation test:

```bash
gradle :app:testDebugUnitTest --tests com.venkateshgowda.personallibrary.data.PriceValidatorTest
```