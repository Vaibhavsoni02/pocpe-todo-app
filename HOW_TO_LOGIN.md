# How to Login to the App

## Option 1: Create Account via Sign Up (Recommended)

The app now has **Sign Up** functionality built-in:

1. **Open the app**
2. **Enter your email** in the email field
3. **Enter a password** (must be at least 6 characters)
4. **Click on "Don't have an account? Sign up"** text below the login button
5. Your account will be created automatically and you'll be logged in

**Example:**
- Email: `test@example.com`
- Password: `password123`

## Option 2: Create Account in Firebase Console

If you prefer to create users manually:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **game-web-a57f9**
3. Go to **Authentication** > **Users** tab
4. Click **Add user**
5. Enter:
   - **Email**: `test@example.com`
   - **Password**: `password123`
6. Click **Add user**

Then login with those credentials in the app.

## Option 3: Facebook Login

1. Click the **"Login with Facebook"** button
2. Authorize the app with your Facebook account
3. You'll be automatically logged in

**Note:** Facebook login requires the app to be properly configured and may need to run on a real device (not emulator).

## Testing Credentials

After creating an account using any method above, use those credentials to login:

- **Email**: The email you used to sign up
- **Password**: The password you set

## Troubleshooting

**"Authentication failed" error:**
- Make sure Email/Password authentication is enabled in Firebase Console
- Check that your password is at least 6 characters
- Verify your email format is correct

**"Sign up failed" error:**
- Email might already be registered (try logging in instead)
- Password must be at least 6 characters
- Check Firebase Console for any authentication restrictions

## Verify Firebase Authentication is Enabled

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Authentication** > **Sign-in method**
4. Make sure **Email/Password** is **Enabled**
5. If not enabled, click on it and toggle **Enable**
