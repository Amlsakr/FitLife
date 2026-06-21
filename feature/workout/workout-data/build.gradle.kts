import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.aml_sakr.fitlife.feature.workout.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

tasks.withType<Test>().configureEach {
    systemProperty("FITLIFE_PROJECT_ROOT", rootProject.projectDir.absolutePath)
    systemProperty("FITLIFE_RUN_GEMINI_LIVE_BENCHMARK", System.getenv("FITLIFE_RUN_GEMINI_LIVE_BENCHMARK") ?: "false")
    systemProperty("FITLIFE_GEMINI_QUOTA_VERIFIED", System.getenv("FITLIFE_GEMINI_QUOTA_VERIFIED") ?: "false")
    System.getenv("FITLIFE_GEMINI_MODEL")?.let { systemProperty("FITLIFE_GEMINI_MODEL", it) }
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":feature:workout:workout-domain"))
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
