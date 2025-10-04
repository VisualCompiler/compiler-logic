plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
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
                exclude("**/export/", "**/compiler/")
            }
        }

        if (jsTestDir.exists()) {
            copy {
                from(jsTestDir)
                into(jvmTestDir)
                exclude("**/export")
            }
        }
    }
}

// Ensure syncJvmSources runs before any compilation tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("syncJvmSources")
}

listOf(
    "runKtlintCheckOverJsMainSourceSet",
    "runKtlintCheckOverJsTestSourceSet",
    "runKtlintCheckOverJvmMainSourceSet",
    "runKtlintCheckOverJvmTestSourceSet"
).forEach { taskName ->
    tasks.named(taskName) {
        dependsOn("ktlintFormat")
    }
}
// Make jvmTest depend on syncJvmSources
tasks.named("jvmTest") {
    dependsOn("syncJvmSources")
}

// Also make jsTest depend on syncJvmSources to ensure consistency
tasks.named("jsTest") {
    dependsOn("syncJvmSources")
}

tasks.named<Delete>("clean") {
    delete(
        file("src/jvmMain"),
        file("src/jvmTest")
    )
}

// Ensure tests run and Kover HTML report is generated when running the standard build
tasks.named("build") {
    dependsOn("jsTest", "jvmTest")
    finalizedBy("koverHtmlReport")
}

// Ensure koverHtmlReport depends on test execution to have coverage data
tasks.named("koverHtmlReport") {
    dependsOn("jsTest", "jvmTest")
}

// Task to create the main class
tasks.register<JavaCompile>("compileMainClass") {
    group = "build"
    description = "Compiles the main class for the JAR"
    val tempDir = temporaryDir
    val mainClassFile = File(tempDir, "CompilerMain.java")
    // Create the main class file during configuration
    val mainClassContent =
        """
package compiler;

import java.io.File;
import java.nio.file.Files;

public class CompilerMain {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar compiler.jar <input_file>");
            System.exit(1);
        }
        
        File inputFile = new File(args[0]);
        if (!inputFile.exists()) {
            System.out.println("Error: File " + args[0] + " does not exist");
            System.exit(1);
        }
        
        try {
            String sourceCode = new String(Files.readAllBytes(inputFile.toPath()));
            CompilerWorkflow.Companion.fullCompile(sourceCode);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
        """.trimIndent()

    mainClassFile.writeText(mainClassContent)

    source = fileTree(tempDir) { include("**/*.java") }
    destinationDirectory = file("$buildDir/classes/java/main")
    classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles + files(kotlin.jvm().compilations["main"].output)

    dependsOn("jvmMainClasses")
}

// Create executable JAR for JVM target
tasks.register<Jar>("createCompilerJar") {
    group = "build"
    description = "Creates an executable JAR for the JVM target"
    from(kotlin.jvm().compilations["main"].output)
    from("$buildDir/classes/java/main") {
        include("compiler/CompilerMain.class")
    }
    archiveBaseName.set("compiler")
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "compiler.CompilerMain"
    }
    dependsOn("jvmMainClasses", "compileMainClass")
    // Include all dependencies in the JAR
    from(
        kotlin
            .jvm()
            .compilations["main"]
            .runtimeDependencyFiles
            .map { if (it.isDirectory) it else zipTree(it) }
    )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
