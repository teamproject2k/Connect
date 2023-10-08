import org.gradle.kotlin.dsl.provideDelegate

object Dependencies {
    val hilt by lazy { "com.google.dagger:hilt-android:${Versions.daggerHilt}" }
    val hiltCompiler by lazy { "com.google.dagger:hilt-android-compiler:${Versions.daggerHilt}" }
    val composeBom by lazy { "androidx.compose:compose-bom:${Versions.composeBom}" }
    val composeActivity by lazy { "androidx.activity:activity-compose:1.7.2" }
    val espresso by lazy { "androidx.test.espresso:espresso-core:3.5.1" }
    val composeUi by lazy { "androidx.compose.ui:ui" }
    val composeGraphics by lazy { "$composeUi-graphics" }
    val composeTooling by lazy { "$composeUi-tooling" }
    val composeToolingPreview by lazy { "$composeTooling-preview" }
    val material3 by lazy { "androidx.compose.material3:material3" }
    private val composeTest by lazy { "$composeUi-test" }
    val composeTestJunit by lazy { "$composeTest-junit4" }
    val composeTestManifest by lazy { "$composeTest-manifest" }
    val kotlin by lazy { "androidx.core:core-ktx:${Versions.kotlin}" }
    val lifecycle by lazy { "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}" }
    val junitTest by lazy { "junit:junit:${Versions.junitTest}" }
    val junitAndroidTest by lazy { "androidx.test.ext:junit:${Versions.junitAndroidTest}" }
    val firebase by lazy { "com.google.firebase:firebase-bom:32.3.1" }
    val firebaseAuth by lazy { "com.google.firebase:firebase-auth" }
    val firebaseAnalytics by lazy { "com.google.firebase:firebase-analytics" }
    val firebaseCrashlytics by lazy { "com.google.firebase:firebase-crashlytics" }
    val firebaseFirestore by lazy { "com.google.firebase:firebase-firestore" }
    val firebaseStorage by lazy { "com.google.firebase:firebase-storage" }
    val navigationCompose by lazy { "androidx.hilt:hilt-navigation-compose:1.0.0" }

}