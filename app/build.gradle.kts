plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // Asegúrate de usar la misma versión de Kotlin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mipapalote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mipapalote"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // Elimina o comenta el bloque composeOptions
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.4.3"
    // }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation(libs.firebase.storage.ktx)
    implementation("androidx.compose.runtime:runtime:1.7.5")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation(libs.androidx.material3)
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.5")
    implementation(libs.androidx.navigation.compose)

    // Escaneo QR
    implementation(libs.core)

    //Estadisticas
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Otros
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(libs.barcode.scanning.common)
    implementation(libs.play.services.code.scanner)
    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("androidx.compose.foundation:foundation:1.7.5")
    implementation("androidx.compose.material:material-icons-core:1.7.5")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.compose.material3)


    // Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.5")
}