# Quick Start Guide - Building APK with GitHub Actions

## Prerequisites

- GitHub account
- Repository created on GitHub
- Project code ready to push

## Steps

### 1. Initialize Git Repository (if not done)

```bash
git init
git add .
git commit -m "Initial commit"
```

### 2. Add Remote and Push

```bash
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

### 3. Verify GitHub Actions Workflow

1. Go to your GitHub repository
2. Click on the **Actions** tab
3. You should see the workflow start automatically (if you pushed to `main`, `master`, or `develop`)

### 4. Wait for Build

- The build typically takes 3-5 minutes
- You can watch the progress in real-time in the Actions tab
- A green checkmark means the build succeeded
- A red X means the build failed (check logs)

### 5. Download APK

1. Click on the completed workflow run
2. Scroll down to the **Artifacts** section
3. Click on `app-debug-apk` or `app-release-apk`
4. The APK will download to your computer

## Manual Trigger

To manually trigger a build:

1. Go to **Actions** tab
2. Select **Build Android APK** workflow
3. Click **Run workflow** button
4. Select branch and click **Run workflow**

## Troubleshooting

**Build fails immediately:**
- Check that `google-services.json` is in the `app/` directory
- Verify package name matches: `com.pocpe.todo`

**Gradle wrapper error:**
- Run `./gradlew wrapper` in Android Studio terminal
- Commit the generated `gradle-wrapper.jar` file

**Missing dependencies:**
- Ensure all required files are committed
- Check that `strings.xml` has placeholder values (won't break build)

## Next Steps

For signed release builds, see `GITHUB_ACTIONS_SETUP.md` section on "Setting Up Repository Secrets".
