System.setProperty("android.useAndroidX", "true")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

gradle.beforeProject {
    extensions.extraProperties["android.useAndroidX"] = true
}

rootProject.name = "PersonalLibrary"
include(":app")