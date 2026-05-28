plugins {
    kotlin("jvm") version "2.3.21"
    jacoco
    `java-gradle-plugin`
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("com.diffplug.spotless") version "8.5.1"
    id("io.github.sfali23.gradle-semantic-build-versioning") version ("0.2.0")
}

group = "io.github.sfali23"

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.35.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        // Custom rule to replace 3+ newlines with just 2
        replaceRegex("Remove extra newlines", "\\n\\n\\n+", "\n\n")
    }

    kotlin {
        target("src/**/*.kt")
        trimTrailingWhitespace()
        endWithNewline()
        // Custom rule to replace 3+ newlines with just 2
        replaceRegex("Remove extra newlines", "\\n\\n\\n+", "\n\n")
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
   // withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}

sourceSets.test
    .get()
    .java
    .srcDir("src/test/kotlin")
sourceSets.test
    .get()
    .resources
    .srcDir("src/test/resources")

val createPluginClasspathFile by tasks.registering {
    inputs.files(sourceSets.main.get().runtimeClasspath)
    outputs.dir(temporaryDir)
    doLast {
        file("$temporaryDir/plugin-classpath.txt").writeText(
            sourceSets.main
                .get()
                .runtimeClasspath
                .joinToString("\n"),
        )
    }
}

configurations {
    create("jacocoRuntime")
}

val createJacocoAgentClasspathFile by tasks.registering {
    inputs.files(configurations["jacocoRuntime"])
    outputs.dir(temporaryDir)
    doLast {
        val jacocoAgentClasspathFile = file("$temporaryDir/jacoco-agent-classpath.txt")
        jacocoAgentClasspathFile.writeText(
            """|${configurations["jacocoRuntime"].asPath}
               |${tasks.jacocoTestReport.get().reports.xml.outputLocation.get().asFile.absolutePath}
            """.trimMargin(),
        )
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")

    testImplementation("org.eclipse.jgit:org.eclipse.jgit.junit:7.6.0.202603022253-r")
    testImplementation("com.typesafe:config:1.4.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("io.cucumber:cucumber-java:7.14.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.14.0")
    testImplementation("io.cucumber:cucumber-expressions:16.1.2")

    add("jacocoRuntime", "org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime")

    testRuntimeOnly(files(createPluginClasspathFile.get()))
    testRuntimeOnly(files(createJacocoAgentClasspathFile.get()))
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    environment("message", "test env")
    environment("emptyMessage", "")
    finalizedBy(tasks.jacocoTestReport)
    doFirst {
        delete(
            tasks.jacocoTestReport
                .get()
                .reports.xml.outputLocation
                .get()
                .asFile,
        )
    }

    // Don't fail if no tests are discovered (test compatibility needs fixing)
    failOnNoDiscoveredTests = false

    useJUnitPlatform {
        includeEngines("junit-jupiter")
        includeEngines("cucumber")
    }

    systemProperty("cucumber.junit-platform-engine.enabled", "true")
    systemProperty("cucumber.features", "classpath:features")
    systemProperty("cucumber.glue", "steps")
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

gradlePlugin {
    website = "https://github.com/sfali23/gradle-semantic-build-versioning"
    vcsUrl = "https://github.com/sfali23/gradle-semantic-build-versioning"
    plugins {
        create("semanticBuildVersioningPlugin") {
            id = "io.github.sfali23.gradle-semantic-build-versioning"
            implementationClass = "semverrelease.SemanticBuildVersioningPlugin"
            displayName = "Gradle Semantic Build Versioning Plugin"
            description =
                "This is a Gradle settings-plugin that provides support for semantic versioning of builds. It is quite easy to use and extremely configurable. The plugin allows you to bump the major, minor, patch or pre-release version based on the latest version, which is identified from a git tag. It also allows you to bump pre-release versions based on a scheme that you define. The version can be bumped by using version-component-specific project properties or can be bumped automatically based on the contents of a commit message. If no manual bumping is done via commit message or project property, the plugin will increment the version-component with the lowest precedence; this is usually the patch version, but can be the pre-release version if the latest version is a pre-release one. The plugin does its best to ensure that you do not accidentally violate semver rules while generating your versions; in cases where this might happen the plugin forces you to be explicit about violating these rules. As this is a settings plugin, it is applied to settings.gradle and version calculation is therefore performed right at the start of the build, before any projects are configured. This means that the project version is immediately available (almost as if it were set explicitly - which it effectively is), and will never change during the build (barring some other, external task that attempts to modify the version during the build). While the build is running, tagging or changing the project properties will not influence the version that was calculated at the start of the build."
            tags.set(listOf("versioning", "semantic-versioning", "git", "build-versioning", "auto-versioning", "version"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates("io.github.sfali23", "gradle-semantic-build-versioning", "$version")

    pom {
        name.set("Gradle Semantic Build Versioning Plugin")
        description.set("This is a Gradle settings-plugin that provides support for semantic versioning of builds.")
        url.set("https://github.com/sfali23/gradle-semantic-build-versioning")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("sfali23")
                name.set("Syed Farhan Ali")
                email.set("f.syed.ali@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/sfali23/gradle-semantic-build-versioning.git")
            developerConnection.set("scm:git:ssh://github.com:sfali23/gradle-semantic-build-versioning.git")
            url.set("https://github.com/sfali23/gradle-semantic-build-versioning/tree/main")
        }
    }
}

semverrelease {
    startingVersion.set("0.2.0")
    extraReleaseBranches.set(listOf("fix_publishing"))
}
