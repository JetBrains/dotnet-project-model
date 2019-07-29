package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.toUnixString
import org.jetbrains.dotnet.discovery.ProjectStreamFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Path

class ProjectStreamFactoryStub : ProjectStreamFactory {
    override val baseDirectory: File
        get() = File(".")

    private val _streams: MutableMap<String, InputStream> = mutableMapOf()

    fun add(path: Path, inputStream: InputStream): ProjectStreamFactoryStub {
        _streams[path.toUnixString()] = inputStream
        return this
    }

    override fun tryCreate(path: Path): InputStream? {
        return _streams[path.toUnixString()]
    }
}