# Incremental Java Compilation Tool (jic)

This tools does basic incremental compilation of Java sources based on type references.

The tool analyzes produced `.class` files after compilation by finding all used types in a `.class`
and builds a reverse dependency map `class -> dependents`.

The tool keeps track of Java files and classes produces by each file. When a Java file is changed,
all class files produced by that file and their dependents are marked for recompilation.

The tool analyzes provided classpath before and after compilation and marks dependent source classes
for recompilation if the classpath has changed.

## Known limitations

Known limitations, which are not handled (implemented)(yet):

- Inline constant changes are not detected
- `package-info.java` and `module-info.java` are not checked
- Annotation processors are not checked
- Compilation target SDK is Java 8
- Passing down properties to the compiler is not implemented

## Launching

Try out the tool by importing the project in IntelliJ IDEA, 
editing some Java sources in `sandbox/src` and running `Sandbox libs1` or `Sandbox libs2` config.

Or download the `jic.jar` from the releases page, set JDK as default java runtime and run via command line:

```bash
java -jar jic.jar <option>
```

## Options

```text
Usage: jic <options> <sources dir>
where possible options include:
    -version
        Print java compiler version
    -cp <path>
        Specify where to find user class files
    -d <directory>
        Specify where to place generated class files
    -force-recompile
        Do not use incremental compilation and compile all files
```

## Project structure overview

Project consists of three modules:

- `:api` contains API definition of the tool
- `:cli` CLI interface, uses `:api` module to access the tool and includes `:impl` as runtime
- `:impl` contains actual tool implementation

Implementation structure:

- `compiler` compiler interface definition and toolchain compiler impl
- `files` file utils
- `incremental`
   - `cache` assemble incremental cache and save/load it
   - `classpath` analyse dependencies between classes
   - `dependencies` analyse class dependencies
   - `diff` determine sources to compile based on input and previous compilation cache

## Potential improvements

- Detect inline constant changes
- Split dependents map into accessible and private ones to limit the dirty set
- More fine-grained classpath analysis with an ABI signature (publicly available classes, their fields and method)
- Cache classpath snapshots
- Create a proper test suite
- Support compiler params
- Support annotation processing
- Support all Java 8+ targets
- Become a daemon to keep caches in memory and save time by not loading compilator classes on every tool start 
- Add measurements to every step to analyze performance problems
- Implement more fine-grained dependency tracking by recording not only the types but actual methods/field of the type
- Integrate the tool into some build system
