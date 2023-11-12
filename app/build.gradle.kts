plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp") version Versions.ksp
}

android {
    namespace = "com.example.connect"
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        applicationId = "com.example.connect"
        minSdk = ConfigData.minSdk
        targetSdk = ConfigData.compileSdk
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = ConfigData.compileOptions
        targetCompatibility = ConfigData.compileOptions
    }
    kotlinOptions {
        jvmTarget = ConfigData.kotlinOptions
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = ConfigData.kotlinCompilerExtensionVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(Dependencies.kotlin)
    implementation(Dependencies.lifecycle)
    implementation(Dependencies.composeActivity)
    implementation(platform(Dependencies.composeBom))
    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeGraphics)
    implementation(Dependencies.composeToolingPreview)
    implementation(Dependencies.material3)
    testImplementation(Dependencies.junitTest)
    androidTestImplementation(Dependencies.junitAndroidTest)
    androidTestImplementation(Dependencies.espresso)
    androidTestImplementation(platform(Dependencies.composeBom))
    androidTestImplementation(Dependencies.composeTestJunit)
    debugImplementation(Dependencies.composeTooling)
    debugImplementation(Dependencies.composeTestManifest)
    // dagger-hilt
    implementation(Dependencies.hilt)
    kapt(Dependencies.hiltCompiler)
    implementation(Dependencies.navigationComposeHilt)
    //firebase
    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseAnalytics)
    implementation(Dependencies.firebaseCrashlytics)
    implementation(Dependencies.firebaseAuth)
    implementation(Dependencies.firebaseFirestore)
    //coil
    implementation(Dependencies.coil)
    //view model
    implementation(Dependencies.viewModelCompose)
    implementation(Dependencies.liveDataCompose)
    lintChecks(project(Dependencies.lintCheck))
    //Splash Screen Api
    implementation(Dependencies.splashScreen)
    //navigation compose
    implementation(Dependencies.navigationCompose)
    ksp(Dependencies.ksp)
    //Dialog Picker
    implementation(Dependencies.dialog)
    //Room
    implementation(Dependencies.roomRuntime)
    annotationProcessor(Dependencies.roomCompiler)
    kapt(Dependencies.roomKapt)
    //constraint layout
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation ("com.google.code.gson:gson:2.10.1")


}
kapt {
    correctErrorTypes = true
}


