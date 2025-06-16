// build.gradle.kts (Project)
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}

buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.gradle.v872)

    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}