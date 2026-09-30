plugins {
    alias(libs.plugins.storagekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.storage.api)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.datastore.preferences.core)
                implementation(libs.okio)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.storage.testing)
            }
        }
    }
}
