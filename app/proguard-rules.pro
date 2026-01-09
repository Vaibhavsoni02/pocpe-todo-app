# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Meta SDK classes
-keep class com.facebook.** { *; }

# Keep Mixpanel classes
-keep class com.mixpanel.** { *; }

# Keep your application classes
-keep class com.pocpe.todo.** { *; }
