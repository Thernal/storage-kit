plugins {
    alias(libs.plugins.storagekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.storage.api)
                implementation(libs.kotlinx.coroutines.core)
                // KeyValueStoreContract is a test suite shipped as main code, so the test libraries it
                // is written against are ordinary dependencies here.
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        // Test source sets get kotlin-test's platform binding automatically; main ones do not. iOS
        // needs none — Kotlin/Native's test annotations are built in.
        androidMain {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}
