# How to Build Release APK

## Quick Guide

### Option 1: Build Release APK Locally (Unsigned - for testing)

**Windows (PowerShell):**
```powershell
cd C:\Users\netway\claudeprojects\app
.\gradlew.bat assembleRelease
```

**Linux/Mac:**
```bash
./gradlew assembleRelease
```

**Output Location:**
- `app/build/outputs/apk/release/[APK_NAME]-[VERSION].apk`
- Example: `app/build/outputs/apk/release/client-app-1.0.0.apk`

### Option 2: Build Release APK via GitHub Actions

1. Go to your GitHub repository
2. Click on **Actions** tab
3. Select **Build Android APK** workflow
4. Click **Run workflow** button
5. Wait for build to complete
6. Download APK from **Artifacts** section

### Option 3: Build Signed Release APK (For Play Store)

#### Step 1: Create a Keystore

**Windows (PowerShell):**
```powershell
keytool -genkey -v -keystore app/my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

**Linux/Mac:**
```bash
keytool -genkey -v -keystore app/my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

You'll be prompted to enter:
- Keystore password
- Key password
- Your name, organization, etc.

#### Step 2: Create keystore.properties

Create file: `app/keystore.properties`

```properties
storeFile=my-release-key.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=my-key-alias
keyPassword=YOUR_KEY_PASSWORD
```

**⚠️ IMPORTANT:** Add `keystore.properties` to `.gitignore` to keep passwords secure!

#### Step 3: Update build.gradle.kts

Add signing config to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing code ...
    
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
                
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config ...
        }
    }
}
```

#### Step 3: Build Signed APK

```powershell
.\gradlew.bat assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/[APK_NAME]-[VERSION].apk`

## Differences: Debug vs Release APK

| Feature | Debug APK | Release APK |
|---------|-----------|-------------|
| **Signing** | Auto-signed by debug key | Requires signing config |
| **Optimization** | Not optimized | Can be optimized (ProGuard/R8) |
| **Size** | Larger | Smaller (if optimized) |
| **Performance** | Slower | Faster |
| **Debugging** | Enabled | Disabled |
| **Play Store** | ❌ Cannot upload | ✅ Can upload |
| **Testing** | ✅ Good for testing | ✅ Production ready |

## Current Configuration

Based on `app/app-config.properties`:
- **APK Name:** `client-app`
- **Version:** `1.0.0`
- **Output:** `client-app-1.0.0.apk` (release) or `client-app-1.0.0-debug.apk` (debug)

## Troubleshooting

**Error: "Task 'assembleRelease' not found"**
- Make sure you're in the project root directory
- Run `./gradlew tasks` to see available tasks

**Error: "Keystore file not found"**
- For unsigned release APK, you don't need a keystore
- For signed APK, ensure `keystore.properties` and keystore file exist

**Error: "Release build requires signing config"**
- This is normal for unsigned builds
- Either add signing config (see Option 3) or use debug APK for testing

**APK not found after build:**
- Check `app/build/outputs/apk/release/` directory
- APK name follows pattern: `[APK_NAME]-[VERSION].apk`
- Check `app-config.properties` for APK_NAME value
