package com.github.alanderua.jic.impl.compiler

import com.github.alanderua.jic.api.CompilationResult
import java.nio.file.Path

internal interface Compiler {
    val version: String

    fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        out: Path,
    ): CompilationResult
}