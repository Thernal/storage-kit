plugins {
    alias(libs.plugins.storagekit.kmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            // `Flow` is in `KeyValueStore`'s signatures, yet not re-exported: `api(...)` is not used
            // in this repository, so a consumer declares coroutines itself (README → Dependencies you
            // declare).
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
