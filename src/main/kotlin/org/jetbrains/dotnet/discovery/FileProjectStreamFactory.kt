package org.jetbrains.dotnet.discovery

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Path

class FileProjectStreamFactory(private val baseDirectory: File) : ProjectStreamFactory {
    override fun tryCreate(path: Path): InputStream? {
        val file = baseDirectory.toPath().resolve(path).toFile()
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