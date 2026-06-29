import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun String.orEnv(name: String): String =
    takeIf { it.isNotBlank() } ?: (System.getenv(name) ?: "")

fun String.escapeForBuildConfig(): String =
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.aml_sakr.fitlife"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.aml_sakr.fitlife"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val geminiApiKey = (localProperties.getProperty("FITLIFE_GEMINI_API_KEY") ?: "")
            .orEnv("FITLIFE_GEMINI_API_KEY")
            .ifBlank { (localProperties.getProperty("GEMINI_API_KEY") ?: "").orEnv("GEMINI_API_KEY") }
        val workoutGeminiModelName = (localProperties.getProperty("FITLIFE_GEMINI_MODEL") ?: "")
            .orEnv("FITLIFE_GEMINI_MODEL")
            .ifBlank { (localProperties.getProperty("GEMINI_MODEL_NAME") ?: "").orEnv("GEMINI_MODEL_NAME") }
        buildConfigField("String", "WORKOUT_GEMINI_API_KEY", "\"${geminiApiKey.escapeForBuildConfig()}\"")
        buildConfigField(
            "String",
            "WORKOUT_GEMINI_MODEL_NAME",
            "\"${(workoutGeminiModelName.ifBlank { "" }).escapeForBuildConfig()}\""
        )

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-ui"))
    implementation(project(":feature:auth:auth-data"))
    implementation(project(":feature:auth:auth-domain"))
    implementation(project(":feature:auth:auth-ui"))
    implementation(project(":feature:onboarding:onboarding-data"))
    implementation(project(":feature:onboarding:onboarding-domain"))
    implementation(project(":feature:onboarding:onboarding-ui"))
    implementation(project(":feature:shell:shell-ui"))
    implementation(project(":feature:session:session-ui"))
    implementation(project(":feature:session:session-data"))
    implementation(project(":feature:workout:workout-data"))
    implementation(project(":feature:progress:progress-data"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.core)
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
