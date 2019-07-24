package org.jetbrains.dotnet.discovery

import java.io.InputStream

interface StreamFactory {
    fun tryCreate(path: String): InputStream?
}