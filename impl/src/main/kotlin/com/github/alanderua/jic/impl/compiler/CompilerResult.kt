package com.github.alanderua.jic.impl.compiler

import java.nio.file.Path

internal sealed interface CompilerResult {
    data class Success(
        val classesBySources: Map<Path, Set<String>>
    ) : CompilerResult

    data object CompilationNotNeeded : CompilerResult

    data object Error : CompilerResult
}