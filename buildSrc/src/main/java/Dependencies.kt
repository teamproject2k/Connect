import org.gradle.kotlin.dsl.provideDelegate

object Dependencies {
    val hilt by lazy { "com.google.dagger:hilt-android:${Versions.daggerHilt}" }
    val hiltCompiler by lazy { "com.google.dagger:hilt-android-compiler:${Versions.daggerHilt}" }
    val composeBom by lazy { "androidx.compose:compose-bom:${Versions.composeBom}" }
    val composeActivity by lazy { "androidx.activity:activity-compose:${Versions.activityCompose}" }
    val espresso by lazy { "androidx.test.espresso:espresso-core:${Versions.espresso}" }
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
    val firebaseBom by lazy { "com.google.firebase:firebase-bom:${Versions.firebaseBom}" }
    val firebaseAuth by lazy { "com.google.firebase:firebase-auth" }
    val firebaseAnalytics by lazy { "com.google.firebase:firebase-analytics-ktx" }
    val firebaseCrashlytics by lazy { "com.google.firebase:firebase-crashlytics-ktx" }
    val firebaseFirestore by lazy { "com.google.firebase:firebase-firestore-ktx" }
    val firebaseStorage by lazy { "com.google.firebase:firebase-storage-ktx" }
    val coil by lazy { "io.coil-kt:coil-compose:${Versions.coil}" }
    val viewModelCompose by lazy { "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifecycle}" }
    val liveDataCompose by lazy { "androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}" }
    val lint by lazy { "com.android.tools.lint:lint-api:${Versions.lint}" }
    val lintCheck by lazy { ":LintRuleLibrary" }
    val splashScreen by lazy { "androidx.core:core-splashscreen:${Versions.splashScreen}" }
    val navigationCompose by lazy { "io.github.raamcosta.compose-destinations:animations-core:${Versions.navigationCompose}" }
    val ksp by lazy { "io.github.raamcosta.compose-destinations:ksp:${Versions.navigationCompose}" }
    val navigationComposeHilt by lazy { "androidx.hilt:hilt-navigation-compose:${Versions.navigationComposeHilt}" }
    val dialog by lazy { "com.maxkeppeler.sheets-compose-dialogs:core:${Versions.dialog}" }
    val calender by lazy { "com.maxkeppeler.sheets-compose-dialogs:calender:${Versions.dialog}" }

}