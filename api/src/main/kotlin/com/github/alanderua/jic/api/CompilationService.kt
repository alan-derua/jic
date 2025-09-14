package com.github.alanderua.jic.api

import java.nio.file.Path

/**
 * Java incremental compilation (jic) service. Let's you incrementally compile Java sources.
 *
 * Load implementation via [loadImplementation].
 */
public interface CompilationService {

    /**
     * Use provided [logger] for logging
     */
    public fun useLogger(logger: JicLogger)

    /**
     * Create [CompilationConfig] instance for [compile]
     *
     * @see compile
     */
    public fun makeCompilationConfig(): CompilationConfig

    /**
     * Incrementally compile Java sources
     *
     * @param sources Complete list of java sources
     * @param classpath Classpath to use for compilation
     * @param config Compilation configuration
     * @return Either success or error
     *
     * @see makeCompilationConfig
     * @see CompilationConfig
     */
    public fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        config: CompilationConfig
    ): CompilationResult

    /**
     * Returns Java compiler version, which will be used for compilation
     */
    public val compilerVersion: String

    public companion object {
        public fun loadImplementation(classLoader: ClassLoader): CompilationService {
            return loadImplementation(CompilationService::class, classLoader)
        }
    }
}