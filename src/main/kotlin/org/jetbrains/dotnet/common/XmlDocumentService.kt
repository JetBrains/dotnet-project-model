package org.jetbrains.dotnet.common

import org.w3c.dom.Document

interface XmlDocumentService : Serializer<Document>,
    Deserializer<Document> {
    fun create(): Document
}