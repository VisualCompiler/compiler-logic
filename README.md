# Compiler Logic

A compiler implementation written in Kotlin that compiles source code into target code through lexical analysis,
parsing, and multiple other phases. 

## Codebase Structure
### Packages:
- ``jsMain``: contains the core compiler logic
- ``jsTest``: test directory for the main logic

These `js` packages are compiled to JavaScript and used for production.

- ``jvmMain`` and ``jvmTest``: generated automatically through building the project. These packages are copied versions of ``jsMain`` and ``jsTest`` without js-specific code and are used only to generate test coverage reports, since Kover (the plugin we use to generate test reports) only supports JVM-compatible Kotlin code.

## Useful CLI Commands

To **build** the project, run:

``./gradlew build``

To fix **ktlint** errors run:

``./gradlew ktlintFormat`` or install the **Ktlint** plugin.

To run all **tests**, run:

``./gradlew jsTest``

To only **compile** Kotlin to JS without building the whole project run:

``./gradlew compileKotlinJs``

To generate a test coverage report,
1. Sync your jvm directories by running:

    ``./gradlew syncJvmSources``

2. Then run:

    ``./gradlew koverHtmlReport``

_All of these commands are also part of the build command_ 

More test cases are found in the test suite of the book "Writing a C Compiler" by Nora Sandler. The test suite is also included in this project and can be run by following the steps below.


1. Build the compiler

``./gradlew build``

2. Create a jar file

``./gradlew createCompilerJar``

3. Run the test script

``cd src/resources/write_a_c_compiler-tests ./test_compiler_kotlin.sh ../../../build/libs/compiler-1.0-SNAPSHOT.jar && cd ../../..``

For more information, see the [test suite's README](https://github.com/nlsandler/write_a_c_compiler/blob/master/README.md).