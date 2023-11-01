object Versions {
    const val daggerHilt = "2.44"
    const val activityCompose = "1.7.2"
    const val composeBom = "2023.03.00"
    const val junitAndroidTest = "1.1.5"
    const val junitTest = "4.13.2"
    const val espresso = "3.5.1"
    const val kotlin = "1.9.0"
    const val lifecycle = "2.6.2"
    const val android = "8.1.2"
    val lint by lazy {
        val lintVersion = (android.substringBefore(".").toInt() + 23).toString()
        "$lintVersion.${android.substringAfter(".")}"
    }
    const val jetbrainsKotlinAndroid = "1.8.10"
    const val coil = "2.4.0"
    const val navigationCompose = "1.0.0"
    const val firebaseBom = "32.3.1"
    const val googleServices = "4.4.0"
    const val crashlytics = "2.9.9"
    const val splashScreen = "1.0.0"
    const val dialog = "1.2.0"
}