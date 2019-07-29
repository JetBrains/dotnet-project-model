package org.jetbrains.dotnet.discovery

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

class SolutionDiscoverImpl(
    private val _discoverers: List<SolutionDeserializer>
) : SolutionDiscover {

    override fun discover(projectStreamFactory: ProjectStreamFactory, paths: Sequence<Path>): Sequence<Solution> =
        paths.map { createSolutionSource(projectStreamFactory, it) }.flatMap { it }

    private fun createSolutionSource(projectStreamFactory: ProjectStreamFactory, path: Path): Sequence<Solution> = sequence {
        LOG.debug("Discover \"$path\"")
        for (discoverer in _discoverers) {
            if (!discoverer.accept(path)) {
                continue
            }

            LOG.debug("Use discoverer \"$discoverer\" for \"$path\"")
            try {
                val solution = discoverer.deserialize(path, projectStreamFactory)
                LOG.debug("\"$discoverer\" finds \"$solution\"")
                yield(solution)
                break
            } catch (ex: Exception) {
                LOG.error("Discover error", ex)
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(SolutionDiscoverImpl::class.java.name)
    }
}