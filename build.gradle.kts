// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version Versions.android apply false
    id("org.jetbrains.kotlin.android") version Versions.jetbrainsKotlinAndroid apply false
    id("com.android.library") version Versions.android apply false
    id("com.google.dagger.hilt.android") version Versions.daggerHilt apply false
    id("com.google.gms.google-services") version Versions.googleServices apply false
    id("com.google.firebase.crashlytics") version Versions.crashlytics apply false
}
