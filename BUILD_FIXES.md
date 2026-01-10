# Build Fixes and Release APK Guide

## Issues Fixed

### 1. APK Naming Code
- **Problem:** The APK naming code had a potential syntax issue
- **Fix:** Simplified the APK naming logic in `build.gradle.kts`
- **Result:** APKs now properly named as `[APK_NAME]-[VERSION].apk` or `[APK_NAME]-[VERSION]-debug.apk`

### 2. GitHub Actions Workflow
- **Problem:** Workflow was looking for fixed APK names
- **Fix:** Updated workflow to use wildcard pattern `*.apk` to find APKs with custom names
- **Result:** GitHub Actions will now correctly find and upload APKs regardless of naming

### 3. Release APK Generation
- **Problem:** Release APK wasn't being generated
- **Fix:** Release APK can now be built (unsigned for testing, signed for production)
- **Result:** You can now build release APKs locally or via GitHub Actions

## How to Get Release APK

### Method 1: GitHub Actions (Easiest)

1. Go to your GitHub repository: https://github.com/Vaibhavsoni02/pocpe-todo-app
2. Click on **Actions** tab
3. Wait for the latest build to complete (or trigger manually)
4. Click on the completed workflow run
5. Scroll down to **Artifacts** section
6. Download **app-release-apk** (if available) or **app-debug-apk**

**Note:** Release APK will be unsigned (for testing). For Play Store, you need signing (see Method 3).

### Method 2: Build Locally (Requires Java/JDK)

**Prerequisites:**
- JDK 17+ installed
- JAVA_HOME environment variable set

**Windows PowerShell:**
```powershell
# Set JAVA_HOME if not set (adjust path to your JDK)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Build release APK
.\gradlew.bat assembleRelease

# APK will be at:
# app\build\outputs\apk\release\client-app-1.0.0.apk
```

**Linux/Mac:**
```bash
./gradlew assembleRelease

# APK will be at:
# app/build/outputs/apk/release/client-app-1.0.0.apk
```

### Method 3: Build Signed Release APK (For Play Store)

See `BUILD_RELEASE_APK.md` for detailed instructions on creating a keystore and signing the APK.

## Current APK Configuration

Based on `app/app-config.properties`:
- **APK Name:** `client-app`
- **Version:** `1.0.0`
- **Version Code:** `1`

**Output Files:**
- Debug: `client-app-1.0.0-debug.apk`
- Release: `client-app-1.0.0.apk`

## Debug vs Release APK

| Feature | Debug APK | Release APK (Unsigned) |
|---------|-----------|------------------------|
| **Signing** | Auto-signed | Unsigned (needs signing for Play Store) |
| **Size** | Larger | Smaller |
| **Testing** | ✅ Perfect for testing | ✅ Good for testing |
| **Play Store** | ❌ Cannot upload | ❌ Cannot upload (needs signing) |
| **Installation** | ✅ Works on any device | ✅ Works on any device |

**For Play Store:** You need a signed release APK. See `BUILD_RELEASE_APK.md` for signing instructions.

## Troubleshooting

### Build Failed in GitHub Actions

1. Check the **Actions** tab for error messages
2. Common issues:
   - Missing `google-services.json` (should be committed)
   - Gradle sync issues
   - Missing dependencies

### Can't Build Locally

**Error: JAVA_HOME not set**
- Install JDK 17+
- Set JAVA_HOME environment variable
- Or use GitHub Actions instead

**Error: Gradle not found**
- Make sure you're in the project root directory
- Use `.\gradlew.bat` (Windows) or `./gradlew` (Linux/Mac)

### APK Not Found After Build

- Check `app/build/outputs/apk/release/` directory
- APK name follows: `[APK_NAME]-[VERSION].apk`
- Check `app/app-config.properties` for APK_NAME value

## Next Steps

1. **For Testing:** Use debug APK or unsigned release APK
2. **For Production:** Create keystore and build signed release APK (see `BUILD_RELEASE_APK.md`)
3. **For Play Store:** Follow Google Play Console requirements for signed APK

## Quick Commands

```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Build release APK (unsigned)
.\gradlew.bat assembleRelease

# Clean and rebuild
.\gradlew.bat clean assembleRelease

# Check build tasks
.\gradlew.bat tasks
```
