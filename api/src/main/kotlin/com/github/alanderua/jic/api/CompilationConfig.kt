package com.github.alanderua.jic.api

import java.nio.file.Path

/**
 * Compilation config for [CompilationService.compile]
 */
public interface CompilationConfig {
    /**
     * Trigger full recompilation and incremental cache rebuild
     */
    public fun forceRecompile()
    public val forceRecompile: Boolean

    /**
     * Working dir, used to compute relative paths of sources for incremental compilation cache
     */
    public fun useWorkingDir(workingDir: Path)
    public val workingDir: Path

    /**
     * Output dir, used to store compilation result
     */
    public fun useOut(out: Path)
    public val out: Path

    /**
     * Incremental cache dir, used to store incremental compilation cache
     */
    public fun useCacheDir(cacheDir: Path)
    public val cacheDir: Path
}