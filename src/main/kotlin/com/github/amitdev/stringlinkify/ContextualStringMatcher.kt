package com.github.amitdev.stringlinkify

import com.intellij.openapi.vcs.IssueNavigationConfiguration
import com.intellij.openapi.vcs.IssueNavigationLink
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag

fun PsiElement.findMatchingUrl(): String? {
    val candidates = buildMatchCandidates()
    if (candidates.isEmpty()) return null

    val links = IssueNavigationConfiguration.getInstance(project)?.links ?: return null

    for (candidate in candidates) {
        for (link in links) {
            val url = link.matchAndReplace(candidate)
            if (url != null && url != candidate) {
                return url
            }
        }
    }
    return null
}

private fun PsiElement.buildMatchCandidates(): List<String> {
    val content = extractContent() ?: return emptyList()
    val candidates = mutableListOf<String>()

    val contextual = buildContextualString(content)
    if (contextual != null) {
        candidates.add(contextual)
    }

    candidates.add(content)

    return candidates
}

private fun PsiElement.extractContent(): String? {
    val str = when (this) {
        is XmlAttributeValue -> value
        is XmlTag -> value.trimmedText
        else -> {
            val t = text ?: return null
            when {
                t.isEmpty() -> null
                t.startsWith("\"\"\"") -> t.removeSurrounding("\"\"\"")
                t.startsWith("\"") -> t.removeSurrounding("\"")
                t.startsWith("'") -> t.removeSurrounding("'")
                t.startsWith("//") -> t.removePrefix("//").trim()
                t.startsWith("/*") -> t.removePrefix("/*").removeSuffix("*/").trim()
                else -> t
            }
        }
    }
    return str?.takeIf { it.isNotBlank() }
}

private fun PsiElement.buildContextualString(content: String): String? {
    val callName = findEnclosingCallName() ?: return null
    return "$callName(\"$content\")"
}

private fun PsiElement.findEnclosingCallName(): String? {
    var current: PsiElement? = this.parent
    var depth = 0
    val maxDepth = 5

    while (current != null && depth < maxDepth) {
        val name = extractCallName(current)
        if (name != null) return name
        current = current.parent
        depth++
    }
    return null
}

private fun extractCallName(element: PsiElement): String? {
    val className = element.javaClass.name

    return when {
        className.contains("PsiNewExpression") -> {
            element.children
                .firstOrNull { it.javaClass.name.contains("PsiJavaCodeReferenceElement") }
                ?.text
        }

        className.contains("PsiMethodCallExpression") -> {
            element.children
                .firstOrNull { it.javaClass.name.contains("PsiReferenceExpression") }
                ?.let { ref ->
                    val text = ref.text
                    if (text.contains(".")) text.substringAfterLast(".") else text
                }
        }

        className.contains("KtCallExpression") -> {
            element.children.firstOrNull()?.text
        }

        className.contains("KtAnnotationEntry") -> {
            element.children
                .firstOrNull { it.javaClass.name.contains("KtConstructorCalleeExpression") }
                ?.text
        }

        else -> null
    }
}

private fun IssueNavigationLink.matchAndReplace(text: String): String? {
    return try {
        val regex = issuePattern.toRegex()
        if (regex.containsMatchIn(text)) {
            regex.replace(text, linkRegexp)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
