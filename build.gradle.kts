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
    sourceSets {
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
