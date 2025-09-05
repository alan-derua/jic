package com.github.alanderua.jic.api

import java.nio.file.Path

public interface CompilationConfig {
    public fun useWorkingDir(workingDir: Path)
    public val workingDir: Path

    public fun useOut(out: Path)
    public val out: Path
}