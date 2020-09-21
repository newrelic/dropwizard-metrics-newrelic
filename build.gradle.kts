buildscript {
    dependencies {
        classpath("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.9")
    }
}

plugins {
    id("com.github.sherter.google-java-format") version "0.9"
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

group = "com.newrelic.telemetry"
// -Prelease=true will render a non-snapshot version
val isRelease = project.findProperty("release") == "true"
version = project.findProperty("releaseVersion") as String + if(isRelease) "" else "-SNAPSHOT"

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

googleJavaFormat {
    exclude(".**")
}

dependencies {
    api("io.dropwizard.metrics:metrics-core:4.1.5")
    api("com.newrelic.telemetry:telemetry:0.6.1")
    implementation("io.dropwizard:dropwizard-metrics:2.0.13")
    implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.6.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.26")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
}

val jar: Jar by tasks
jar.apply {
    manifest.attributes["Implementation-Version"] = project.version
    manifest.attributes["Implementation-Vendor"] = "New Relic, Inc"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

val useLocalSonatype = project.properties["useLocalSonatype"] == "true"

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Implementation of a DropWizard metrics Reporter that sends data as dimensional metrics to New Relic")
                url.set("https://github.com/newrelic/dropwizard-metrics-newrelic")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("newrelic")
                        name.set("New Relic")
                        email.set("opensource@newrelic.com")
                    }
                }
                scm {
                    url.set("git@github.com:newrelic/dropwizard-metrics-newrelic.git")
                    connection.set("scm:git@github.com:newrelic/dropwizard-metrics-newrelic.git")
                }
            }
        }
    }
    repositories {
        maven {
            if (useLocalSonatype) {
                val releasesRepoUrl = uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = uri("http://localhost:8081/repository/maven-snapshots/")
                url = if (isRelease) releasesRepoUrl else snapshotsRepoUrl
            }
            else {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (isRelease) releasesRepoUrl else snapshotsRepoUrl
            }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingKeyId: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

