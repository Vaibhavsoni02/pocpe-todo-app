# Android Client App

A simple Android client-facing app with Firebase authentication, Meta SDK (Facebook), Mixpanel analytics, Internet connectivity checks, and Bluetooth functionality.

## Features

- ✅ **Firebase Authentication**: Email/Password and Facebook login
- ✅ **Meta SDK (Facebook)**: Facebook login integration
- ✅ **Mixpanel Analytics**: Event tracking and user analytics
- ✅ **Internet Connectivity**: Check internet connection status
- ✅ **Bluetooth**: Check Bluetooth status and request permissions
- ✅ **Login/Logout**: Full authentication flow
- ✅ **Two Pages**: Home screen with two sections (Page One and Page Two)
- ✅ **Modern UI**: Material Design components

## Prerequisites

1. **Android Studio** (latest version recommended)
2. **JDK 17** or higher
3. **Firebase Project**:
   - Create a project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Facebook)
   - Download `google-services.json` and place it in `app/` directory
4. **Facebook App**:
   - Create an app at [Facebook Developers](https://developers.facebook.com/)
   - Get your App ID and Client Token
   - Update `app/src/main/res/values/strings.xml` with your credentials
5. **Mixpanel Account**:
   - Create an account at [Mixpanel](https://mixpanel.com/)
   - Get your project token
   - Update `app/src/main/res/values/strings.xml` with your token

## Setup Instructions

### 1. Clone/Download the Project

```bash
cd /path/to/project
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app with package name: `com.pocpe.todo`
4. Download `google-services.json`
5. Place it in the `app/` directory (replace the example file)
6. Enable Authentication methods:
   - Go to Authentication > Sign-in method
   - Enable "Email/Password"
   - Enable "Facebook" and add your Facebook App ID and Secret

### 3. Configure Facebook SDK

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add Facebook Login product
4. Get your App ID and Client Token
5. Edit `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="facebook_app_id">YOUR_ACTUAL_FACEBOOK_APP_ID</string>
   <string name="facebook_client_token">YOUR_ACTUAL_FACEBOOK_CLIENT_TOKEN</string>
   ```
6. Add your Facebook App ID to the AndroidManifest.xml (it's already configured, just need to update strings.xml)

### 4. Configure Mixpanel

1. Go to [Mixpanel](https://mixpanel.com/)
2. Create a project or select existing one
3. Get your project token
4. Edit `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="mixpanel_token">YOUR_ACTUAL_MIXPANEL_TOKEN</string>
   ```

### 5. Build the Project

#### Using Android Studio:

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click "Run" or press `Shift+F10`

#### Using Command Line:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Using GitHub Actions (Automated Builds)

GitHub Actions provides automated builds without needing Android Studio or a local development environment.

**Setup:**
1. Push your code to a GitHub repository
2. The workflow will automatically run on:
   - Push to `main`, `master`, or `develop` branches
   - Pull requests to `main` or `master`
   - When you create a new release
   - Manual trigger from the Actions tab

**Accessing Built APKs:**
1. Go to your repository on GitHub
2. Click on the **Actions** tab
3. Select the workflow run you want
4. Scroll down to **Artifacts** section
5. Download `app-debug-apk` or `app-release-apk`

**Workflows included:**
- `android.yml` - Builds debug and release APKs on push/PR
- `android-release.yml` - Builds and attaches APKs to GitHub releases

**Note:** For signed release APKs, you'll need to configure keystore secrets in GitHub repository settings (Settings > Secrets and variables > Actions).

### 6. Generate Signed Release APK

1. In Android Studio: **Build > Generate Signed Bundle / APK**
2. Select **APK**
3. Create or select a keystore
4. Build the release APK

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Project Structure

```
app/
├── src/main/
│   ├── java/com/pocpe/todo/
│   │   ├── MainActivity.kt          # Login screen
│   │   └── HomeActivity.kt          # Home screen with two pages
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml    # Login layout
│   │   │   └── activity_home.xml    # Home layout
│   │   └── values/
│   │       ├── strings.xml          # String resources (update with your credentials)
│   │       ├── colors.xml           # Color resources
│   │       └── themes.xml           # App theme
│   └── AndroidManifest.xml          # App manifest
├── build.gradle.kts                 # App-level build configuration
├── google-services.json             # Firebase configuration (download from Firebase)
└── proguard-rules.pro               # ProGuard rules

build.gradle.kts                     # Project-level build configuration
settings.gradle.kts                  # Gradle settings
gradle.properties                    # Gradle properties
```

## App Flow

1. **MainActivity (Login Screen)**:
   - Email/Password login
   - Facebook login button
   - Redirects to HomeActivity on successful login

2. **HomeActivity (Home Screen)**:
   - **Page One**: Internet connectivity check button
   - **Page Two**: Bluetooth status check button
   - Logout button

## Permissions

The app requests the following permissions:
- `INTERNET`: For network connectivity
- `ACCESS_NETWORK_STATE`: To check network status
- `BLUETOOTH`: For Bluetooth functionality (Android 11 and below)
- `BLUETOOTH_ADMIN`: For Bluetooth administration (Android 11 and below)
- `BLUETOOTH_CONNECT`: For Bluetooth connectivity (Android 12+)
- `BLUETOOTH_SCAN`: For Bluetooth scanning (Android 12+)

## Testing

1. **Email Login**:
   - Create a test user in Firebase Console > Authentication
   - Or use the email/password fields to sign up (if sign-up is enabled)

2. **Facebook Login**:
   - Ensure Facebook app is configured correctly
   - Test on a real device (Facebook login may not work on emulator)

3. **Internet Check**:
   - Toggle device WiFi/Mobile data to test

4. **Bluetooth Check**:
   - Toggle device Bluetooth to test
   - Grant permissions when prompted

## Troubleshooting

### Build Errors

- **Gradle sync fails**: Ensure you have internet connection and correct Gradle version
- **Missing google-services.json**: Download from Firebase Console and place in `app/` directory
- **Facebook login not working**: Check that Facebook App ID is correctly set in strings.xml

### Runtime Errors

- **Facebook login crashes**: Ensure Facebook SDK is properly configured
- **Mixpanel events not showing**: Check that Mixpanel token is correct
- **Bluetooth permission denied**: Grant permissions in device settings

## Dependencies

- AndroidX Core
- Material Design Components
- Firebase Authentication & Analytics
- Facebook Android SDK
- Mixpanel Android SDK
- Navigation Component

## Notes

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Build with Java 17 compatibility
- Uses Kotlin language

### App Icons

The app references `ic_launcher` and `ic_launcher_round` icons. You'll need to:
1. Generate app icons using Android Studio's Image Asset Studio (right-click `res` > New > Image Asset)
2. Or add your own icon PNG files to the `mipmap-*` directories
3. The app will build without custom icons, but will use a default Android icon

### Important Configuration Files

Before building, make sure to:
1. Replace `app/google-services.json` with your Firebase config file
2. Update `app/src/main/res/values/strings.xml` with:
   - Your Facebook App ID
   - Your Facebook Client Token
   - Your Mixpanel project token

## License

This project is provided as-is for development purposes.
