# Release Signing

Create the permanent keystore once from the `android` directory:

```bash
keytool -genkeypair -v -keystore personal-library-release.jks -alias personal-library -keyalg RSA -keysize 4096 -validity 10000
```

Keep the keystore and both passwords in an encrypted backup outside this repository.

Copy `signing.properties.example` to `signing.properties` and replace its placeholders. Both paths are excluded from Git.

Build the signed APK:

```bash
gradle --no-daemon :app:assembleRelease
```

The result is `app/build/outputs/apk/release/app-release.apk`.