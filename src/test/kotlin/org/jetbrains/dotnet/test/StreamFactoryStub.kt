package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.discovery.StreamFactory
import java.io.InputStream
import java.nio.file.Path

class StreamFactoryStub : StreamFactory {
    private val _streams: MutableMap<String, InputStream> = mutableMapOf()

    fun add(path: String, inputStream: InputStream): StreamFactoryStub {
        _streams[normalize(path)] = inputStream
        return this
    }

    private fun normalize(path: String) = Path.of(path).normalize().toString()

    override fun tryCreate(path: String): InputStream? {
        return _streams[normalize(path)]
    }
}