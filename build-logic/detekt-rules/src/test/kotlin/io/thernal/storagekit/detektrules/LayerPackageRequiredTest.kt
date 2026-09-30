package io.thernal.storagekit.detektrules

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.thernal.storagekit.detektrules.packageboundary.LayerPackageRequired
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerPackageRequiredTest {
    private val rule = LayerPackageRequired(Config.empty)

    @Test
    fun `reports a file in the root package of an impl module`() {
        val findings = rule.lint(
            """
            package io.thernal.storagekit.storage.impl

            class BackStackNavigator
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports a file in the root package of an api module`() {
        val findings = rule.lint(
            """
            package io.thernal.storagekit.storage.api

            interface Navigator
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `reports a package that is not a layer`() {
        val findings = rule.lint(
            """
            package io.thernal.storagekit.storage.api.deeplink

            class DeepLink
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `allows each layer package and its topical sub packages`() {
        val sources = listOf(
            "io.thernal.storagekit.storage.api.domain",
            "io.thernal.storagekit.storage.api.presentation.navigator",
            "io.thernal.storagekit.storage.impl.data",
            "io.thernal.storagekit.storage.impl.domain.deeplink",
            "io.thernal.storagekit.storage.impl.presentation.scene",
        )

        sources.forEach { packageName ->
            assertEquals(0, rule.lint("package $packageName").size)
        }
    }

    @Test
    fun `ignores wiring build-logic and non-module packages`() {
        val sources = listOf(
            "io.thernal.storagekit.storage.wiring",
            "io.thernal.storagekit.buildlogic",
            "io.thernal.storagekit.detektrules.style",
        )

        sources.forEach { packageName ->
            assertEquals(0, rule.lint("package $packageName").size)
        }
    }

    @Test
    fun `ignores a segment that only starts with a module name`() {
        val findings = rule.lint(
            """
            package io.thernal.storagekit.implementation.detail

            class Detail
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}
