plugins {
    `kotlin-dsl`
}

group = "io.thernal.storagekit.buildlogic"

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

// compileOnly throughout: the plugins themselves are put on the consuming build's classpath by the
// root `build.gradle.kts`, which declares each one `apply false`. These entries only supply the
// Gradle DSL types the conventions below configure.
dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    // implementation, not compileOnly: unlike the others, the Detekt plugin is applied by a
    // convention rather than declared in the root build, so it has to travel with build-logic.
    implementation(libs.detekt.gradle.plugin)
    testImplementation(libs.junit4)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "io.thernal.storagekit.kmp.library"
            implementationClass = "io.thernal.storagekit.buildlogic.KmpLibraryConventionPlugin"
        }
        register("compose") {
            id = "io.thernal.storagekit.compose"
            implementationClass = "io.thernal.storagekit.buildlogic.ComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "io.thernal.storagekit.android.application"
            implementationClass = "io.thernal.storagekit.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("injection") {
            id = "io.thernal.storagekit.injection"
            implementationClass = "io.thernal.storagekit.buildlogic.InjectionConventionPlugin"
        }
    }
}
