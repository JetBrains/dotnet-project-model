package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.XmlDocumentServiceImpl
import org.jetbrains.dotnet.discovery.NuGetConfigDeserializer
import org.jetbrains.dotnet.discovery.NuGetConfigDiscoverer
import org.jetbrains.dotnet.discovery.data.Source
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Path

class NuGetConfigDiscovererTest {

    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> =
        arrayOf(
            arrayOf(
                "/a/b/c/d",
                listOf(
                    "/configs/config1" to "/a/b/c/nuget.config",
                    "/configs/config2" to "/a/b/nuget.config",
                    "/configs/config3" to "/nuget.config"
                ),
                setOf(
                    Source("nuget.org", "https://api.nuget.org/v3/index.json", "/a/b/c/nuget.config"),
                    Source("Contoso", "https://contoso.com/packages/", "/a/b/nuget.config"),
                    Source("MyRepo - ES", "https://MyRepo/ES/nuget", "/nuget.config"),
                    Source("Test Source", "c:\\packages", "/nuget.config")
                )
            )
        )


    @Test(dataProvider = "testDeserializeData")
    fun testDiscover(path: String, configs: List<Pair<String, String>>, expectedSources: Set<String>) {
        val streamFactory = ProjectStreamFactoryStub()
        configs.forEach {
            streamFactory.add(Path.of(it.second), this::class.java.getResourceAsStream(it.first))
        }
        val discoverer =
            NuGetConfigDiscoverer(
                NuGetConfigDeserializer(
                    XmlDocumentServiceImpl()
                )
            )


        // When
        val actualSolution = discoverer.discover(Path.of(path), streamFactory).toSet()

        // Then
        assertEquals(actualSolution, expectedSources)
    }
}