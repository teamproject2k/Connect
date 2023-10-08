plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.data"
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        minSdk = ConfigData.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        targetCompatibility =  ConfigData.compileOptions
    }
    kotlinOptions {
        jvmTarget = ConfigData.kotlinOptions
    }
}

dependencies {

    implementation(Dependencies.kotlin)
    testImplementation(Dependencies.junitTest)
    androidTestImplementation(Dependencies.junitAndroidTest)
    androidTestImplementation(Dependencies.espresso)
    //firebase
    implementation(Dependencies.firebaseFirestore)
    implementation(Dependencies.firebaseStorage)
    implementation(Dependencies.firebaseAuth)

}