# GitHub Actions Setup Guide

This guide explains how to use GitHub Actions to automatically build your Android APK without Android Studio.

## Overview

GitHub Actions workflows are configured to automatically build your Android APK whenever you:
- Push code to main/master/develop branches
- Create a pull request
- Create a new release
- Manually trigger the workflow

## Quick Start

1. **Generate Gradle Wrapper (if not already done):**
   If you don't have `gradle/wrapper/gradle-wrapper.jar`, you need to generate it:
   
   **Option A: Using Android Studio:**
   - Open the project in Android Studio
   - Terminal: Run `./gradlew wrapper` or `gradlew wrapper`
   - This will generate the `gradle-wrapper.jar` file
   
   **Option B: Using Gradle (if installed):**
   ```bash
   gradle wrapper
   ```
   
   **Note:** GitHub Actions will automatically download Gradle if the wrapper jar is missing, but it's recommended to include it in your repository.

2. **Push your code to GitHub:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

2. **View builds:**
   - Go to your repository on GitHub
   - Click the **Actions** tab
   - Watch the workflow run in real-time

3. **Download APK:**
   - Once the build completes, scroll down to the **Artifacts** section
   - Download `app-debug-apk` or `app-release-apk`

## Workflow Files

### 1. `android.yml` (Standard Build)

This workflow builds both debug and release APKs on every push and PR.

**Triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main` or `master`
- New releases
- Manual trigger (`workflow_dispatch`)

**Outputs:**
- `app-debug-apk` - Debug APK (always available)
- `app-release-apk` - Release APK (may require signing config)

**Features:**
- Uses JDK 17
- Sets up Android SDK automatically
- Runs Gradle build
- Uploads APKs as artifacts (30-day retention)
- Generates build summary with commit info

### 2. `android-release.yml` (Release Build)

This workflow is optimized for creating release builds and attaching them to GitHub releases.

**Triggers:**
- When a new release is created
- Manual trigger with version inputs

**Outputs:**
- Attaches APK directly to the GitHub release
- Also uploads as artifact

**Signing Configuration:**
To build signed release APKs, add these secrets to your repository:
- `KEYSTORE_BASE64` - Base64-encoded keystore file
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

## Setting Up Repository Secrets (Optional)

For signed release builds, you need to configure secrets:

1. **Generate keystore** (if you don't have one):
   ```bash
   keytool -genkey -v -keystore keystore.jks -alias my-key -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Encode keystore to base64:**
   ```bash
   # On Linux/Mac
   base64 -i keystore.jks > keystore_base64.txt
   
   # On Windows PowerShell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) | Out-File keystore_base64.txt
   ```

3. **Add secrets to GitHub:**
   - Go to your repository
   - Settings > Secrets and variables > Actions
   - Click "New repository secret"
   - Add each secret:
     - `KEYSTORE_BASE64` - Content from keystore_base64.txt
     - `KEYSTORE_PASSWORD` - Your keystore password
     - `KEY_ALIAS` - Your key alias (e.g., "my-key")
     - `KEY_PASSWORD` - Your key password

## Manual Workflow Trigger

You can manually trigger workflows:

1. Go to **Actions** tab in your repository
2. Select the workflow (e.g., "Build Android APK")
3. Click **Run workflow** button
4. Select branch and click **Run workflow**

For `android-release.yml`, you can also provide:
- Version name (e.g., "1.0.0")
- Version code (e.g., "1")

## Viewing Build Logs

1. Click on a workflow run in the Actions tab
2. Click on the **build** job
3. Expand any step to see detailed logs
4. Debug build issues using the logs

## Troubleshooting

### Build Fails

**Common issues:**
- **Missing `google-services.json`**: Make sure it's committed to the repository (or use secrets)
- **Missing credentials**: Update `strings.xml` or use GitHub Secrets
- **Gradle sync issues**: Check build logs for specific errors
- **Out of memory**: GitHub Actions runners have limited resources

**Solutions:**
- Check the workflow logs for specific error messages
- Ensure all required files are committed
- Verify package name matches in all files
- Check that all dependencies are properly configured

### Release APK Not Available

The release APK requires a signing configuration. Either:
1. Set up keystore secrets (see above)
2. Use the debug APK for testing
3. Sign the APK manually after downloading

### Workflow Not Running

- Check that workflow files are in `.github/workflows/` directory
- Verify the branch name matches the trigger conditions
- Ensure workflow files are committed to the repository
- Check repository settings for Actions permissions

## Best Practices

1. **Don't commit sensitive data:**
   - Use GitHub Secrets for API keys, tokens, and keystores
   - Never commit `google-services.json` if it contains sensitive data (use secrets)

2. **Use branches:**
   - Use feature branches for development
   - Merge to main only after testing
   - Builds run automatically on PRs

3. **Tag releases:**
   - Create tags for version releases
   - Use semantic versioning (e.g., v1.0.0)
   - Releases trigger the release workflow

4. **Monitor builds:**
   - Set up notifications for failed builds
   - Review build summaries for quick status
   - Keep artifacts organized by version

## File Retention

- Debug APKs: 30 days
- Release APKs: 90 days (in release workflow)
- Download APKs regularly if you need them longer

## Security Notes

- GitHub Actions runs in isolated virtual environments
- Secrets are encrypted and only accessible during workflow runs
- Never log or expose secrets in workflow outputs
- Use environment secrets for organization-level secrets

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Actions Setup](https://github.com/android-actions/setup-android)
- [Gradle Build Documentation](https://docs.gradle.org/)
