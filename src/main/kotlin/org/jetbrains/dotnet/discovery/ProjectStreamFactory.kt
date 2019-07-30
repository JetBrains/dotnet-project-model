package org.jetbrains.dotnet.discovery

import java.io.InputStream
import java.nio.file.Path

interface ProjectStreamFactory {
    fun tryCreate(path: Path): InputStream?
//    val baseDirectory: File
}