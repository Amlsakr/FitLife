plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":core:core-domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
}
