package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.common.toUnixString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Path

class FileStreamFactory(private val baseDirectory: File) : StreamFactory {
    override fun tryCreate(path: Path): InputStream? {
        val file = File(baseDirectory, path.toUnixString())
        if (!file.exists()) {
            LOG.debug("File $path was not found")
            return null
        }

        return file.inputStream()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(SolutionDiscoverImpl::class.java.name)
    }
}