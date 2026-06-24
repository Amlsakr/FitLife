pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitLife"
include(":app")
include(":core:core-data")
include(":core:core-domain")
include(":core:core-ui")
include(":feature:auth:auth-data")
include(":feature:auth:auth-domain")
include(":feature:auth:auth-ui")
include(":feature:onboarding:onboarding-data")
include(":feature:onboarding:onboarding-domain")
include(":feature:onboarding:onboarding-ui")
include(":feature:home:home-ui")
include(":feature:profile:profile-ui")
include(":feature:shell:shell-ui")
include(":feature:workout:workout-data")
include(":feature:workout:workout-domain")
include(":feature:workout:workout-ui")
include(":feature:session:session-data")
include(":feature:session:session-domain")
include(":feature:session:session-ui")
include(":feature:progress:progress-data")
include(":feature:progress:progress-domain")
include(":feature:progress:progress-ui")
 
