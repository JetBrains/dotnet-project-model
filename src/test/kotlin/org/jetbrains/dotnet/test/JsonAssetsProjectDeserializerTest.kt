package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.discovery.*
import org.jetbrains.dotnet.discovery.Target
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Path

class JsonAssetsProjectDeserializerTest {

    @Test
    fun shouldDeserialize() {
        // Given
        val target = "/project.assets.json"
        val path = Path.of("./project.assets.json")
        val streamFactory = ProjectStreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val deserializer =
            JsonAssetsProjectDeserializer(ReaderFactoryImpl())

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        assertEquals(actualSolution.projects.size, 1)
        val project = actualSolution.projects[0]
        assertTrue(project.project.endsWith("ConsoleApp1.csproj"))
        assertEquals(project.frameworks, listOf(Framework("netcoreapp2.2")))
        assertEquals(project.targets, listOf(Target(".NETCoreApp,Version=v2.2")))
        assertTrue(project.references.toSet().containsAll( listOf(
            Reference("AutoMapper", "8.1.1", listOf(
                Reference("Microsoft.CSharp", "4.5.0"),
                Reference("System.Reflection.Emit", "4.3.0")
            ), true),
            Reference("NuGet.Versioning", "5.1.0", emptyList(), false)
        )))
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("project.assets.json", true),
            arrayOf("abc\\project.assets.json", true),
            arrayOf("abc//project.assets.json", true),
            arrayOf("abc/ProJect.asSets.Json", true),
            arrayOf("aaaaproject.assets.json", false),
            arrayOf("project.assets.jsonaaaa", false),
            arrayOf("abc\\project.assets.jsoss", false),
            arrayOf("project.assets.json10aaa", false),
            arrayOf("10project.assets.json", false),
            arrayOf("10rer323project.assets.json", false),
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
            JsonAssetsProjectDeserializer(ReaderFactoryImpl())

        // When
        val path = Path.of(stringPath)
        val actualAccepted = deserializer.accept(path)

        // Then
        assertEquals(actualAccepted, expectedAccepted)
    }
}