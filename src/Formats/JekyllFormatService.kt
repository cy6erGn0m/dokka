package org.jetbrains.dokka

public open class JekyllFormatService(locationService: LocationService,
                                 signatureGenerator: LanguageService)
: MarkdownFormatService(locationService, signatureGenerator) {

    override fun appendNodes(location: Location, to: StringBuilder, nodes: Iterable<DocumentationNode>) {
        to.appendln("---")
        appendFrontMatter(nodes, to)
        to.appendln("---")
        to.appendln("")
        super<MarkdownFormatService>.appendNodes(location, to, nodes)
    }

    protected open fun appendFrontMatter(nodes: Iterable<DocumentationNode>, to: StringBuilder) {
        to.appendln("title: ${getPageTitle(nodes)}")
    }
}