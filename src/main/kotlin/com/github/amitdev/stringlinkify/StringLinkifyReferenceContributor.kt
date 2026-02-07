package com.github.amitdev.stringlinkify

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.paths.WebReference
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import java.util.*

private val LOG = Logger.getInstance(StringLinkifyReferenceContributor::class.java)
private const val CLASS_MAP_PROPERTIES = "StringLiteralClassNames"

class StringLinkifyReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        patterns.forEach { pattern ->
            registrar.registerReferenceProvider(pattern, LinkifyReferenceProvider)
        }
    }
}

object LinkifyReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) =
        element.findMatchingUrl()?.let { arrayOf(WebReference(element, it)) } ?: emptyArray()
}

private fun getClassLoader(pluginId: String): ClassLoader {
    if (pluginId.isBlank()) {
        return StringLinkifyReferenceContributor::class.java.classLoader
    }
    val descriptor = PluginManagerCore.getPlugin(PluginId.getId(pluginId))
    return descriptor?.pluginClassLoader ?: StringLinkifyReferenceContributor::class.java.classLoader
}

private val patterns: List<ElementPattern<out PsiElement>> by lazy {
    ResourceBundle.getBundle(CLASS_MAP_PROPERTIES)
        .keySet()
        .mapNotNull { className ->
            val pluginId = ResourceBundle.getBundle(CLASS_MAP_PROPERTIES).getString(className)
            loadClass<PsiElement>(className, pluginId)
        }
        .map { StandardPatterns.instanceOf(it) }
}

private fun <T> loadClass(name: String, pluginId: String): Class<T>? {
    return try {
        @Suppress("UNCHECKED_CAST")
        Class.forName(name, true, getClassLoader(pluginId)) as Class<T>
    } catch (e: ClassNotFoundException) {
        LOG.info("Class '$name' not found, skipping")
        null
    }
}
