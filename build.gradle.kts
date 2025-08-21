plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    kotlin("plugin.serialization") version "2.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        outputModuleName = "CompilerLogic"
        useEsModules()
        binaries.executable()
        nodejs {
            testTask {
                useMocha()
            }
        }
        generateTypeScriptDefinitions()
    }
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter:5.9.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

koverReport {
    filters {
        includes {
            classes("*")
        }
    }
    verify {
        rule {
            bound {
                minValue = 0 // Temporary
            }
        }
    }
}

// Task to automatically sync JVM sources from JS sources
tasks.register("syncJvmSources") {
    group = "build"
    description = "Sync JVM source directories from JS source directories, excluding JS-specific files"

    doLast {
        val srcDir = project.file("src")
        val jsMainDir = File(srcDir, "jsMain/kotlin")
        val jsTestDir = File(srcDir, "jsTest/kotlin")
        val jvmMainDir = File(srcDir, "jvmMain/kotlin")
        val jvmTestDir = File(srcDir, "jvmTest/kotlin")

        // Create directories if they don't exist
        jvmMainDir.mkdirs()
        jvmTestDir.mkdirs()

        if (jsMainDir.exists()) {
            copy {
                from(jsMainDir)
                into(jvmMainDir)
                exclude("**/CompilationOutput.kt")
                exclude("**/CompilerExport.kt")
            }
        }

        if (jsTestDir.exists()) {
            copy {
                from(jsTestDir)
                into(jvmTestDir)
                exclude("**/CompilerExportTest.kt")
            }
        }
    }
}

// Make jvmTest depend on syncJvmSources
tasks.named("jvmTest") {
    dependsOn("syncJvmSources")
}
