plugins {
    alias(libs.plugins.storagekit.kmp.library)
    alias(libs.plugins.storagekit.injection)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // The contracts are not re-exported: an app that injects them depends on `api` itself.
                implementation(projects.storage.api)
                implementation(projects.storage.impl)
            }
        }
    }
}
