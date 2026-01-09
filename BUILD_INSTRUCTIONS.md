# Quick Build Instructions

## Prerequisites Checklist

- [ ] Android Studio installed
- [ ] JDK 17+ installed
- [ ] Firebase project created
- [ ] Facebook app created
- [ ] Mixpanel account created

## Configuration Steps

### Step 1: Firebase Setup
1. Go to https://console.firebase.google.com/
2. Create/select your project
3. Add Android app with package: `com.pocpe.todo`
4. Download `google-services.json`
5. Place it in `app/` directory

### Step 2: Facebook Setup
1. Go to https://developers.facebook.com/
2. Create an app and add Facebook Login
3. Get your App ID and Client Token
4. Edit `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="facebook_app_id">YOUR_APP_ID</string>
   <string name="facebook_client_token">YOUR_CLIENT_TOKEN</string>
   ```

### Step 3: Mixpanel Setup
1. Go to https://mixpanel.com/
2. Create a project
3. Get your project token
4. Edit `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="mixpanel_token">YOUR_MIXPANEL_TOKEN</string>
   ```

## Building the APK

### Option 1: Android Studio
1. Open project in Android Studio
2. Wait for Gradle sync
3. Build > Build Bundle(s) / APK(s) > Build APK(s)
4. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Command Line (Windows PowerShell)
```powershell
# Navigate to project directory
cd C:\Users\netway\claudeprojects\app

# Build debug APK
.\gradlew.bat assembleDebug

# APK will be at: app\build\outputs\apk\debug\app-debug.apk
```

### Option 3: Command Line (Linux/Mac)
```bash
# Navigate to project directory
cd /path/to/project

# Build debug APK
./gradlew assembleDebug

# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

## Installing on Device

### Via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Via Android Studio
1. Connect device via USB
2. Enable USB Debugging on device
3. Click Run in Android Studio
4. Select your device

## Generating Signed Release APK

1. In Android Studio: Build > Generate Signed Bundle / APK
2. Select APK
3. Create new keystore (or use existing)
4. Fill in keystore details
5. Select release build type
6. Build APK

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Troubleshooting

**Gradle sync fails:**
- Check internet connection
- Verify JDK version (17+)
- Clean and rebuild: Build > Clean Project, then Build > Rebuild Project

**Build errors:**
- Ensure `google-services.json` is in `app/` directory
- Check that all credentials in `strings.xml` are updated
- Verify package name matches: `com.pocpe.todo`

**Runtime errors:**
- Facebook login requires real device (emulator may not work)
- Check that Firebase Authentication is enabled
- Verify all API keys and tokens are correct

## Testing

1. **Email Login**: Create test user in Firebase Console > Authentication
2. **Facebook Login**: Requires real device with Facebook app installed
3. **Internet Check**: Toggle WiFi/Mobile data
4. **Bluetooth Check**: Toggle Bluetooth, grant permissions when prompted
