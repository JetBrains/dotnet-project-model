package org.jetbrains.dotnet.discovery

import java.io.InputStream
import java.nio.file.Path

interface StreamFactory {
    fun tryCreate(path: Path): InputStream?
}