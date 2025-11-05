plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "dev.agb.nasmplugin"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Use CLion as the primary platform to get CIDR APIs for compilation
        clion("2025.2")
        bundledPlugin("com.intellij.clion")
        bundledPlugin("com.intellij.cidr.lang")
        bundledPlugin("com.intellij.clion.cmake")

        instrumentationTools()
        pluginVerifier()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    // Gson for parsing compile_commands.json
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        name = "Enhanced NASM Assembly Support"
        version = "1.0.1"

        ideaVersion {
            sinceBuild = "252"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            recommended()
        }

        // Allow missing CIDR dependencies in non-CLion IDEs since they're optional
        freeArgs = listOf(
            "-mute",
            "ForbiddenPluginIdPrefix",
            "-mute",
            "TemplateWordInPluginId",
            "-ignored-problems",
            "com.jetbrains.cidr.*"
        )
    }
}

// Grammar-Kit configuration
tasks {
    generateLexer {
        sourceFile.set(file("src/main/grammar/Nasm.flex"))
        targetOutputDir.set(file("src/main/gen/dev/agb/nasmplugin/lexer"))
        purgeOldFiles.set(true)
    }

    generateParser {
        sourceFile.set(file("src/main/grammar/Nasm.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("dev/agb/nasmplugin/parser/NasmParser.java")
        pathToPsiRoot.set("dev/agb/nasmplugin/psi")
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn(generateLexer, generateParser)
    }

    compileKotlin {
        dependsOn(generateLexer, generateParser)
    }

    test {
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen", "src/main/java")
        }
        kotlin {
            srcDirs("src/main/kotlin")
        }
    }
}

// Disable buildSearchableOptions task if it causes issues
tasks.named("buildSearchableOptions") {
    enabled = false
}
