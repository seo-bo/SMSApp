pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Version Catalog 를 쓰지 않으실 거면, 여기서 직접 플러그인 버전을 선언하세요.
        id("com.android.application") version "7.4.2" apply false
        id("org.jetbrains.kotlin.android") version "1.8.21" apply false
        id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SMSApp"
include(":app")
