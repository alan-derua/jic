package com.github.alanderua.jic.impl.incremental.diff

import java.nio.file.Path

internal sealed interface CompilationSpec {
    data class CompilationNotNeeded(
        val reason: String
    ) : CompilationSpec

    data class FullRecompilation(
        val reason: String
    ) : CompilationSpec

    data class IncrementalCompilation(
        val sourcesToRecompile: Set<Path>,
        val classesToDelete: Set<String>,
        val deletedFiles: Set<String>
    ) : CompilationSpec
}