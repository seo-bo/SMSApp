plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

val navVer  = "2.7.7"
val lifeVer = "2.8.0"

android {
    namespace   = "com.example.smsapp"
    compileSdk  = 35

    defaultConfig {
        applicationId = "com.example.smsapp"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"
    }

    buildFeatures { viewBinding = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    /* ───────── 기존 의존성 ───────── */
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.navigation:navigation-fragment-ktx:$navVer")
    implementation("androidx.navigation:navigation-ui-ktx:$navVer")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeVer")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeVer")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeVer")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("org.tensorflow:tensorflow-lite:2.11.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.3")

    /* ───────── 추가된 의존성 ───────── */
    // Material Components 1.11+  →  Theme.Material3.*, NavigationBarView, TextInputLayout 등 XML 빨간줄 해결
    implementation("com.google.android.material:material:1.11.0")

    // 코루틴(Android)  →  lifecycleScope.launch 사용 시 필수
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
}
