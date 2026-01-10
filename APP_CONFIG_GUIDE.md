# App Configuration Guide

## Centralized App Configuration

All app metadata (name, version, APK name) can be configured in **one place**: `app/app-config.properties`

### Configuration File: `app/app-config.properties`

```properties
# App Configuration
APP_NAME=My Client App
APP_VERSION_NAME=1.0.0
APP_VERSION_CODE=1
APK_NAME=client-app
```

### What Gets Updated Automatically

When you update `app-config.properties`, these values are automatically used:

1. **App Name** - Shown on device home screen and in settings
2. **Version Name** - User-visible version (e.g., "1.0.0")
3. **Version Code** - Internal version number (incremented with each release)
4. **APK Name** - Output filename for generated APKs

### Example

If you set:
```properties
APP_NAME=Todo Pro
APP_VERSION_NAME=2.1.0
APP_VERSION_CODE=3
APK_NAME=todo-pro
```

The app will:
- Display as "Todo Pro" on device
- Show version "2.1.0" in settings
- Generate APKs named: `todo-pro-2.1.0.apk` and `todo-pro-2.1.0-debug.apk`

### APK Output Locations

- Debug APK: `app/build/outputs/apk/debug/[APK_NAME]-[VERSION]-debug.apk`
- Release APK: `app/build/outputs/apk/release/[APK_NAME]-[VERSION].apk`

### Notes

- **Version Code** must be an integer and should increment with each release
- **Version Name** can be any string (e.g., "1.0", "1.0.0", "2.0-beta")
- **APK Name** should not include file extension (it's added automatically)
- Debug builds automatically get "(Debug)" suffix in app name
- Changes to `app-config.properties` require a rebuild to take effect
