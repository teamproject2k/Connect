plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp") version Versions.ksp
    id("kotlin-parcelize")
}

android {
    namespace = "com.teamproject2k.connect"
    compileSdk = ConfigData.compileSdk
    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    defaultConfig {
        applicationId = "com.teamproject2k.connect"
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
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    testImplementation(Dependencies.junitTest)
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
    implementation(Dependencies.firebaseStorage)
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.3.0")

    //coil
    implementation(Dependencies.coil)
    implementation("io.coil-kt:coil-video:2.4.0")

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
    implementation(Dependencies.gson)
    annotationProcessor(Dependencies.roomCompiler)
    kapt(Dependencies.roomKapt)
    //Constraint layout
    implementation(Dependencies.constraintLayout)
    //Exoplayer
    implementation(Dependencies.exoplayer)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //chucker
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")
}
tasks {
    // Configure the test task to use JUnit Platform
    withType<Test> {
        useJUnitPlatform()
    }
}
kapt {
    correctErrorTypes = true
}


