package com.github.alanderua.jic.api

import java.nio.file.Path

public interface CompilationConfig {
    public fun forceRecompile()
    public val forceRecompile: Boolean

    public fun useWorkingDir(workingDir: Path)
    public val workingDir: Path

    public fun useOut(out: Path)
    public val out: Path

    public fun useCacheDir(cacheDir: Path)
    public val cacheDir: Path
}