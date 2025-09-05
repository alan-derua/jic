package com.github.alanderua.jic.api

import java.nio.file.Path

public interface CompilationService {

    public fun useLogger(logger: JicLogger)

    public fun makeCompilationConfig(): CompilationConfig

    public fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        config: CompilationConfig
    ): CompilationResult

    public val compilerVersion: String

    public companion object {
        public fun loadImplementation(classLoader: ClassLoader): CompilationService {
            return loadImplementation(CompilationService::class, classLoader)
        }
    }
}