package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.XmlDocumentServiceImpl
import org.jetbrains.dotnet.discovery.MSBuildProjectDeserializer
import org.jetbrains.dotnet.discovery.NuGetConfigDeserializer
import org.jetbrains.dotnet.discovery.NuGetConfigDiscoverer
import org.jetbrains.dotnet.discovery.data.*
import org.jetbrains.dotnet.discovery.data.Target
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Path
import java.util.*

class MSBuildProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                "/project-runtime.csproj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            emptyList(),
                            listOf(
                                Runtime("win7-x64"),
                                Runtime("win-7x86"),
                                Runtime("ubuntu.16.10-x64")
                            ),
                            listOf(
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/GeneratePackageOnBuild.csproj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            listOf(Framework("netstandard2.0")),
                            emptyList(),
                            listOf(
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            ),
                            emptyList(),
                            emptyList(),
                            true
                        )
                    )
                )
            ),
            arrayOf(
                "/project14.csproj",
                Optional.of("/nuget.config"),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(
                                Configuration("Debug"),
                                Configuration("Release")
                            ),
                            emptyList(),
                            emptyList(),
                            listOf(
                                Reference("nunit.engine.api", "3.0.0.0"),
                                Reference("System", "*"),
                                Reference("System.Data", "*"),
                                Reference("System.Xml", "*"),
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            ),
                            sources = listOf(
                                Source("nuget.org", "https://api.nuget.org/v3/index.json", "nuget.config"),
                                Source("Contoso", "https://contoso.com/packages/", "nuget.config"),
                                Source("Test Source", "c:\\packages", "nuget.config")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project.csproj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Core")),
                            listOf(Framework("netcoreapp1.0")),
                            emptyList(),
                            listOf(
                                Reference(
                                    "Microsoft.NET.Sdk",
                                    "1.0.0-alpha-20161104-2"
                                ),
                                Reference(
                                    "Microsoft.NET.Test.Sdk",
                                    "15.0.0-preview-20161024-02"
                                ),
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/build.proj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Release")),
                            emptyList(),
                            emptyList(),
                            listOf(
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            ),
                            listOf(
                                Target("GetNuGet"),
                                Target("Build"),
                                Target("Test")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project-simplified.csproj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Core")),
                            listOf(Framework("netcoreapp1.0")),
                            emptyList(),
                            listOf(
                                Reference(
                                    "Microsoft.NET.Sdk",
                                    "1.0.0-alpha-20161104-2"
                                ),
                                Reference(
                                    "Microsoft.NET.Test.Sdk",
                                    "15.0.0-preview-20161024-02"
                                ),
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project-frameworks.csproj",
                Optional.empty<String>(),
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            listOf(
                                Framework("net45"),
                                Framework("netstandard1.3")
                            ),
                            emptyList(),
                            listOf(
                                Reference("Newtonsoft.Json", "10.0.3"),
                                Reference("jQuery", "3.1.1"),
                                Reference("NLog", "4.3.10")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, config: Optional<String>, expectedSolution: Solution) {
        // Given
        val path = Path.of("projectPath")
        val packagesConfigPath = Path.of("packages.config")
        val streamFactory =
            ProjectStreamFactoryStub()
            .add(path, this::class.java.getResourceAsStream(target))
            .add(packagesConfigPath, this::class.java.getResourceAsStream("/packages.config"))

        config.ifPresent { streamFactory.add(Path.of("nuget.config"), this::class.java.getResourceAsStream(it)) }

        val deserializer =
            MSBuildProjectDeserializer(XmlDocumentServiceImpl(), NuGetConfigDiscoverer(NuGetConfigDeserializer(XmlDocumentServiceImpl())))

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("abc.proj", true),
            arrayOf("abcPproj", false),
            arrayOf("abc.csproj", true),
            arrayOf("abc.vbproj", true),
            arrayOf("abc3232.vbproj", true),
            arrayOf("abc.Proj", true),
            arrayOf("abc.CSproj", true),
            arrayOf("abc.VBproj", true),
            arrayOf("ab c.VBproj", true),
            arrayOf("dd/ff/abc.VBproj", true),
            arrayOf("c:\\dd\\ff\\abc.VBproj", true),
            arrayOf("abc.sln", false),
            arrayOf("abc.", false),
            arrayOf("abc", false),
            arrayOf("abc.projddd", false),
            arrayOf(".proj", false),
            arrayOf("proj", false),
            arrayOf("csproj", false),
            arrayOf("VBproj", false),
            arrayOf("", false)
        )
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(stringPath: String, expectedAccepted: Boolean) {
        // Given
        val deserializer =
            MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val path = Path.of(stringPath)
        val actualAccepted = deserializer.accept(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}