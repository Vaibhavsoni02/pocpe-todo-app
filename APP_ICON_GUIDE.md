# App Icon Setup Guide

## Current Icon Setup

The app now uses a vector drawable icon. To customize it:

### Option 1: Replace the Vector Drawable (Recommended)

Edit these files:
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_foreground.xml` - Foreground icon
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` - Full adaptive icon

### Option 2: Use PNG Images

Replace the vector drawables with PNG images:

1. Create icon images in these sizes:
   - `mipmap-mdpi`: 48x48 px
   - `mipmap-hdpi`: 72x72 px
   - `mipmap-xhdpi`: 96x96 px
   - `mipmap-xxhdpi`: 144x144 px
   - `mipmap-xxxhdpi`: 192x192 px

2. Name them:
   - `ic_launcher.png` (for regular icon)
   - `ic_launcher_round.png` (for round icon)

3. Place in respective `mipmap-*` directories

4. Update `AndroidManifest.xml` if needed (should work automatically)

### Option 3: Use Android Studio Image Asset Studio

1. In Android Studio: Right-click `res` folder
2. Select **New > Image Asset**
3. Choose **Launcher Icons (Adaptive and Legacy)**
4. Upload your image or design using the studio
5. Click **Next** and **Finish**

This will automatically generate all required icon sizes.

### Icon Requirements

- **Foreground**: Should be mostly transparent (only the icon itself)
- **Background**: Solid color or gradient (defined in adaptive icon XML)
- **Safe Zone**: Keep important parts in center 66% of the icon (Android will crop edges)
- **Format**: PNG (recommended) or Vector Drawable (XML)

### Current Icon Design

The app currently uses a simple purple circular icon with white inner circle. You can customize:
- Colors in `colors.xml` (purple_500, etc.)
- Shape in `ic_launcher_foreground.xml`
- Background in `ic_launcher.xml`
