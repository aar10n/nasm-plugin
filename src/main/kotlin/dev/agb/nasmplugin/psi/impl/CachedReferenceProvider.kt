package dev.agb.nasmplugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * Base class for PSI elements that provide cached references.
 * Reduces the overhead of creating new reference objects on every call.
 */
abstract class CachedReferenceProvider(node: ASTNode) : ASTWrapperPsiElement(node) {

    private val referenceCache: CachedValue<PsiReference?> by lazy {
        CachedValuesManager.getManager(project).createCachedValue(
            {
                CachedValueProvider.Result.create(
                    createReference(),
                    this
                )
            },
            false
        )
    }

    private val referencesCache: CachedValue<Array<PsiReference>> by lazy {
        CachedValuesManager.getManager(project).createCachedValue(
            {
                CachedValueProvider.Result.create(
                    createReferences(),
                    this
                )
            },
            false
        )
    }

    override fun getReference(): PsiReference? {
        return referenceCache.value
    }

    override fun getReferences(): Array<PsiReference> {
        return referencesCache.value
    }

    /**
     * Creates a single reference for this element.
     * Override this method to provide the reference implementation.
     */
    protected open fun createReference(): PsiReference? = null

    /**
     * Creates multiple references for this element.
     * Override this method if the element can have multiple references.
     */
    protected open fun createReferences(): Array<PsiReference> {
        val ref = createReference()
        return if (ref != null) arrayOf(ref) else PsiReference.EMPTY_ARRAY
    }

    /**
     * Note: Reference caches are automatically invalidated when the PSI changes.
     * The CachedValue uses the PSI element itself as a dependency, so when the element
     * or its structure changes, the cache is automatically invalidated and recreated
     * on the next access. No manual invalidation is needed.
     */
}