package org.jetbrains.dokka

import org.jetbrains.dokka.DocumentationNode.*
import org.jetbrains.dokka.LanguageService.RenderMode

/**
 * Implements [LanguageService] and provides rendering of symbols in Java language
 */
public class JavaLanguageService : LanguageService {
    override fun render(node: DocumentationNode, renderMode: RenderMode): ContentNode {
        return ContentText(when (node.kind) {
            Kind.Package -> renderPackage(node)
            Kind.Class,
            Kind.Interface,
            Kind.Enum,
            Kind.EnumItem,
            Kind.Object -> renderClass(node)

            Kind.TypeParameter -> renderTypeParameter(node)
            Kind.Type,
            Kind.UpperBound -> renderType(node)

            Kind.Constructor,
            Kind.Function -> renderFunction(node)
            Kind.Property -> renderProperty(node)
            else -> "${node.kind}: ${node.name}"
        })
    }

    override fun renderName(node: DocumentationNode): String {
        return when (node.kind) {
            Kind.Constructor -> node.owner!!.name
            else -> node.name
        }
    }

    private fun renderPackage(node: DocumentationNode): String {
        return "package ${node.name}"
    }

    private fun renderModifier(node: DocumentationNode): String {
        return when (node.name) {
            "open" -> ""
            "internal" -> ""
            else -> node.name
        }
    }

    private fun renderType(node: DocumentationNode): String {
        return when (node.name) {
            "Unit" -> "void"
            "Int" -> "int"
            "Long" -> "long"
            "Double" -> "double"
            "Float" -> "float"
            "Char" -> "char"
            "Boolean" -> "bool"
        // TODO: render arrays
            else -> node.name
        }
    }

    private fun renderTypeParameter(node: DocumentationNode): String {
        val constraints = node.details(Kind.UpperBound)
        return if (constraints.none())
            node.name
        else {
            node.name + " extends " + constraints.map { renderType(node) }.join()
        }
    }

    private fun renderParameter(node: DocumentationNode): String {
        return "${renderType(node.detail(Kind.Type))} ${node.name}"
    }

    private fun renderTypeParametersForNode(node: DocumentationNode): String {
        return StringBuilder {
            val typeParameters = node.details(Kind.TypeParameter)
            if (typeParameters.any()) {
                append("<")
                append(typeParameters.map { renderTypeParameter(it) }.join())
                append("> ")
            }
        }.toString()
    }

    private fun renderModifiersForNode(node: DocumentationNode): String {
        val modifiers = node.details(Kind.Modifier).map { renderModifier(it) }.filter { it != "" }
        if (modifiers.none())
            return ""
        return modifiers.join(" ", postfix = " ")
    }

    private fun renderClass(node: DocumentationNode): String {
        return StringBuilder {
            when (node.kind) {
                Kind.Class -> append("class ")
                Kind.Interface -> append("interface ")
                Kind.Enum -> append("enum ")
                Kind.EnumItem -> append("enum value ")
                Kind.Object -> append("class ")
                else -> throw IllegalArgumentException("Node $node is not a class-like object")
            }

            append(node.name)
            append(renderTypeParametersForNode(node))
        }.toString()
    }

    private fun renderFunction(node: DocumentationNode): String {
        return StringBuilder {
            when (node.kind) {
                Kind.Constructor -> append(node.owner?.name)
                Kind.Function -> {
                    append(renderTypeParametersForNode(node))
                    append(renderType(node.detail(Kind.Type)))
                    append(" ")
                    append(node.name)
                }
                else -> throw IllegalArgumentException("Node $node is not a function-like object")
            }

            val receiver = node.details(Kind.Receiver).singleOrNull()
            append("(")
            if (receiver != null)
                append((listOf(receiver) + node.details(Kind.Parameter)).map { renderParameter(it) }.join())
            else
                append(node.details(Kind.Parameter).map { renderParameter(it) }.join())

            append(")")
        }.toString()
    }

    private fun renderProperty(node: DocumentationNode): String {
        return StringBuilder {
            when (node.kind) {
                Kind.Property -> append("val ")
                else -> throw IllegalArgumentException("Node $node is not a property")
            }
            append(renderTypeParametersForNode(node))
            val receiver = node.details(Kind.Receiver).singleOrNull()
            if (receiver != null) {
                append(renderType(receiver.detail(Kind.Type)))
                append(".")
            }

            append(node.name)
            append(": ")
            append(renderType(node.detail(Kind.Type)))
        }.toString()
    }
}