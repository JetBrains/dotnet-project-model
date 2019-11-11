package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.discovery.ProjectStreamFactory
import java.io.InputStream
import java.nio.file.Path

class ProjectStreamFactoryStub : ProjectStreamFactory {

    private val _streams: MutableMap<String, InputStream> = mutableMapOf()

    fun add(path: Path, inputStream: InputStream): ProjectStreamFactoryStub {
        _streams[path.normalize().toNormalizedUnixString()] = inputStream
        return this
    }

    override fun tryCreate(path: Path): InputStream? {
        return _streams[path.normalize().toNormalizedUnixString()]
    }
}