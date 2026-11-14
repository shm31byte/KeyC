plugins {
    id("com.android.application")
}

android {
    namespace = "com.ctrlkeys"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ctrlkeys"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
