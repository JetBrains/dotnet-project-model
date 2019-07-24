package org.jetbrains.dotnet.discovery

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

class FileStreamFactory(private val baseDirectory: File) : StreamFactory {
    override fun tryCreate(path: String): InputStream? {
        val file = File(baseDirectory, path)
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