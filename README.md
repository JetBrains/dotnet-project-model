# .NET project model parser

Provides project model deserializer for .NET projects: 
  * csproj files
  * project.json
  * packages.config
  * nuget.config
  * project.asset.json

## Example

```kt
// initialize utilitis services
val readerFactory = ReaderFactoryImpl()
val xmlDocumentService = XmlDocumentServiceImpl1()

// initialize nuget.config deserializer and Discoverer
val nuGetConfigDeserializer = NuGetConfigDeserializer(xmlDocumentService)
val nuGetConfigDiscoverer = NuGetConfigDiscoverer(nuGetConfigDeserializer)

// initialize project deserializers and Discoverer
val jsonProjectDeserializer = JsonProjectDeserializer(readerFactory, nuGetConfigDiscoverer)
val msBuildProjectDeserializer = MSBuildProjectDeserializer(xmlDocumentService, nuGetConfigDiscoverer)
val msBuildSolutionDeserializer = MSBuildSolutionDeserializer(readerFactory, msBuildProjectDeserializer)
val jsonAssetsProjectDeserializer = JsonAssetsProjectDeserializer(readerFactory, nuGetConfigDiscoverer)

val deserializers = listOf(
    jsonProjectDeserializer,
    msBuildProjectDeserializer,
    msBuildSolutionDeserializer,
    jsonAssetsProjectDeserializer
)

val discoverer = SolutionDiscoverImpl(deserializers)

//initialize stream factory
val projectRoot = File("/path/to/project")
val streamFactory = FileProjectStreamFactory(projectRoot)

// deserialize project
val projectFiles = projectRoot.list()?.asSequence()?.map { Paths.get(it) } ?: emptySequence()
val solutions = discoverer.discover(streamFactory, projectFiles).toList()

solutions.flatMap { it.projects }.forEach { println(it) }
```

