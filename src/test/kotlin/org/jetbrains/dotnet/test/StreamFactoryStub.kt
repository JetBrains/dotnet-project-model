package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.toUnixString
import org.jetbrains.dotnet.discovery.StreamFactory
import java.io.InputStream
import java.nio.file.Path

class StreamFactoryStub : StreamFactory {
    private val _streams: MutableMap<String, InputStream> = mutableMapOf()

    fun add(path: Path, inputStream: InputStream): StreamFactoryStub {
        _streams[path.toUnixString()] = inputStream
        return this
    }

    override fun tryCreate(path: Path): InputStream? {
        return _streams[path.toUnixString()]
    }
}