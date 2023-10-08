plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.android.lint")
}

java {
    sourceCompatibility = ConfigData.compileOptions
    targetCompatibility = ConfigData.compileOptions
}
dependencies{
    compileOnly (Dependencies.lint)
}