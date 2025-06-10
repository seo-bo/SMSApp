pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
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
