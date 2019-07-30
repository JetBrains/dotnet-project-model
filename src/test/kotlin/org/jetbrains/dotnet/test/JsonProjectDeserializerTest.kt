package org.jetbrains.dotnet.test


import org.jetbrains.dotnet.common.XmlDocumentServiceImpl
import org.jetbrains.dotnet.discovery.JsonProjectDeserializer
import org.jetbrains.dotnet.discovery.NuGetConfigDeserializer
import org.jetbrains.dotnet.discovery.NuGetConfigDiscoverer
import org.jetbrains.dotnet.discovery.ReaderFactoryImpl
import org.jetbrains.dotnet.discovery.data.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Path

class JsonProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        val referencies = listOf("Microsoft.Bcl.Immutable" to "1.1.18-beta-*",
            "Microsoft.AspNet.ConfigurationModel" to "0.1-alpha-*",
            "Microsoft.AspNet.DependencyInjection" to "0.1-alpha-*",
            "Microsoft.AspNet.Logging" to "0.1-alpha-*",
            "System.Data.Common" to "0.1-alpha-*"
        ).map { Reference(it) }

        return arrayOf(
            arrayOf(
                "/project.json",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            listOf(
                                Framework("dnx451"),
                                Framework("dnxcore50")
                            ),
                            emptyList(),
                            referencies,
                            sources = listOf(
                                Source("nuget.org", "https://api.nuget.org/v3/index.json", "nuget.config"),
                                Source("Contoso", "https://contoso.com/packages/", "nuget.config"),
                                Source("Test Source", "c:\\packages", "nuget.config")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, expectedSolution: Solution) {
        // Given
        val path = Path.of("projectPath")
        val config = "/nuget.config"
        val streamFactory = ProjectStreamFactoryStub()
            .add(path, this::class.java.getResourceAsStream(target))
            .add(Path.of("nuget.config"), this::class.java.getResourceAsStream(config))

        val deserializer =
            JsonProjectDeserializer(ReaderFactoryImpl(), NuGetConfigDiscoverer(NuGetConfigDeserializer(XmlDocumentServiceImpl())))

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("project.json", true),
            arrayOf("abc\\project.json", true),
            arrayOf("abc//project.json", true),
            arrayOf("abc//ProjecT.JsoN", true),
            arrayOf("aaaproject.json", false),
            arrayOf("project.jsonaaa", false),
            arrayOf("abc\\project.jsonsss", false),
            arrayOf("project.json10aaa", false),
            arrayOf("10project.json", false),
            arrayOf("10rer323project.json", false),
            arrayOf(".json", false),
            arrayOf("json", false),
            arrayOf("  ", false),
            arrayOf("", false)
        )
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(stringPath: String, expectedAccepted: Boolean) {
        // Given
        val deserializer =
            JsonProjectDeserializer(ReaderFactoryImpl())

        // When
        val path = Path.of(stringPath)
        val actualAccepted = deserializer.accept(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}