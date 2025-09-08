package com.github.alanderua.jic.cli

internal sealed class Command {
    data class Compile(
        val src: String,
        val outDir: String?,
        val cp: String?,
        val forceRecompile: Boolean
    ): Command()

    data class Print(val msg: String) : Command()

    data object PrintVersion : Command()
}

internal fun parseCommandLineArgs(args: Array<out String>): Command {
    if (args.isEmpty()) return Command.Print(usageString)

    var cp: String? = null
    var outDir: String? = null
    var src: String? = null
    var forceRecompile: Boolean = false

    args.iterator().apply {
        while (hasNext()) {
            when (val arg = next()) {
                "-cp" -> if (!hasNext()) {
                    error("'$arg' param has no arg specified!")
                } else {
                    cp = next().takeUnless { it.startsWith("-") }
                        ?: error("No value provided for '$arg'")
                }
                "-d" -> if (!hasNext()) {
                    error("'$arg' param has no arg specified!")
                } else {
                    outDir = next().takeUnless { it.startsWith("-") }
                        ?: error("No value provided for '$arg'")
                }
                "-force-recompile" -> forceRecompile = true
                "-version" -> return Command.PrintVersion
                else -> src = arg
            }
        }
    }

    check(src != null) {
        "No <sources dir> specified!"
    }

    return Command.Compile(
        src = src,
        outDir = outDir,
        cp = cp,
        forceRecompile = forceRecompile
    )
}

internal val usageString = """
Welcome to incremental java compilation tool 'jic'!

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
""".trimIndent()