package org.jetbrains.dotnet.common

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

open class XPathReader {

    private val xPath = XPathFactory.newInstance().newXPath()

    protected fun getElements(doc: Document, xpath: String): Sequence<Element> = sequence {
        val nodes = xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodes.length) {
            val element = nodes.item(i) as Element
            yield(element)
        }
    }

    protected fun getContents(doc: Document, xpath: String): Sequence<String> =
        getElements(doc, xpath).map { it.textContent }.filter { !it.isNullOrBlank() }

    protected fun getAttributes(doc: Document, xpath: String, attributeName: String): Sequence<String> =
        getElements(doc, xpath).map { it.getAttribute(attributeName) }.filter { !it.isNullOrBlank() }

}