package com.github.alanderua.jic.impl.compiler

import java.nio.file.Path

internal interface Compiler {
    val version: String

    fun compile(
        sources: Collection<Path>,
        classpath: Collection<Path>,
        out: Path,
    ): CompilerResult
}