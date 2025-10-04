# Compiler Logic

A compiler implementation written in Kotlin that compiles source code into target code through lexical analysis,
parsing, and multiple other phases. 

## Codebase Structure
### Packages:
- ``jsMain``: contains the core compiler logic
- ``jsTest``: test directory for the main logic

These two packages are compiled to JS and used for production

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

    _\* These two commands are also part of the build command_ 

We also included the test suite of the book "Writing a C Compiler" by Nora Sandler. To run the test suite, follow these steps:

```
# Build the compiler
./gradlew createCompilerJar
# Run the test script
cd src/resources/write_a_c_compiler-tests
./test_compiler_kotlin.sh ../../../build/libs/compiler-1.0-SNAPSHOT.jar && cd ../../..
```
For more information, see the [test suite README](https://github.com/nlsandler/write_a_c_compiler/blob/master/README.md).