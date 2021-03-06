package org.jetbrains.dokka

import java.io.File

/**
 * Service for building the outline of the package contents.
 */
public interface OutlineFormatService {
    fun getOutlineFileName(location: Location): File

    public fun appendOutlineHeader(location: Location, node: DocumentationNode, to: StringBuilder)
    public fun appendOutlineLevel(to: StringBuilder, body: () -> Unit)

    /** Appends formatted outline to [StringBuilder](to) using specified [location] */
    public fun appendOutline(location: Location, to: StringBuilder, nodes: Iterable<DocumentationNode>) {
        for (node in nodes) {
            appendOutlineHeader(location, node, to)
            if (node.members.any()) {
                val sortedMembers = node.members.sortBy { it.name }
                appendOutlineLevel(to) {
                    appendOutline(location, to, sortedMembers)
                }
            }
        }
    }

    fun formatOutline(location: Location, nodes: Iterable<DocumentationNode>): String =
            StringBuilder { appendOutline(location, this, nodes) }.toString()
}
