package io.thernal.storagekit.detektrules

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider
import io.thernal.storagekit.detektrules.collections.UnsafeCollectionIndexAccess
import io.thernal.storagekit.detektrules.packageboundary.LayerPackageBoundary
import io.thernal.storagekit.detektrules.packageboundary.LayerPackageRequired
import io.thernal.storagekit.detektrules.preview.PreviewMustBePrivate
import io.thernal.storagekit.detektrules.style.ExpressionBodyNotAllowed
import io.thernal.storagekit.detektrules.style.MultilineConstructorRequired

class ProjectRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("project")

    override fun instance() = RuleSet(
        ruleSetId,
        listOf(
            ::PreviewMustBePrivate,
            ::UnsafeCollectionIndexAccess,
            ::LayerPackageBoundary,
            ::LayerPackageRequired,
            ::ExpressionBodyNotAllowed,
            ::MultilineConstructorRequired,
        ),
    )
}
