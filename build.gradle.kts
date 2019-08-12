import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.0"
}


val myGroup = "org.jetbrains"
val myArtifactID = rootProject.name
val currentVersion = findProperty("projectVersion") as? String ?: "1.0-SNAPSHOT"
val description = "Provides project model deserializer for .NET projects"
val repUrl = "https://github.com/JetBrains/dotnet-project-model"
group = myGroup
version = currentVersion

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val publicationName = "Lib"

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])
            artifact(sourcesJar.get())
            groupId = myGroup
            artifactId = myArtifactID
            version = currentVersion

            pom.withXml {
                asNode().apply {
                    appendNode("description", description)
                    appendNode("name", rootProject.name)
                    appendNode("url", repUrl)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "The Apache Software License, Version 2.0")
                        appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        appendNode("distribution", "repo")
                    }
                    appendNode("scm").apply {
                        appendNode("url", repUrl)
                    }
                }
            }
        }
    }
}

bintray {
    user = if (project.hasProperty("bintrayUser"))  project.property("bintrayUser") as String? else System.getenv("BINTRAY_USER")
    key = if (project.hasProperty("bintrayApiKey"))  project.property("bintrayApiKey") as String? else System.getenv("BINTRAY_API_KEY")
    publish = true
    setPublications(publicationName)
    setConfigurations("archives")
    pkg.apply {
        repo = "libraries"
        name = myArtifactID
        userOrg = user
        setLicenses("Apache-2.0")
        setLabels("kotlin")
        vcsUrl = "https://github.com/JetBrains/dotnet-project-model.git"
        version.apply {
            name = currentVersion
        }
    }
}



repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useTestNG()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.code.gson:gson:2.5")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    testImplementation("org.testng:testng:6.14.3")
    testImplementation("org.jmock:jmock:2.5.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
