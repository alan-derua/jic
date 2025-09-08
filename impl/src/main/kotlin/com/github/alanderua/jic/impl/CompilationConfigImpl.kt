package com.github.alanderua.jic.impl

import com.github.alanderua.jic.api.CompilationConfig
import java.nio.file.Path
import kotlin.io.path.Path

internal class CompilationConfigImpl : CompilationConfig {

    override fun forceRecompile() {
        forceRecompile = true
    }

    override var forceRecompile: Boolean = false

    override fun useWorkingDir(workingDir: Path) {
        this.workingDir = workingDir
    }

    override var workingDir: Path = Path(System.getProperty("user.dir"))

    override fun useOut(out: Path) {
        this.out = out
    }

    override var out: Path = workingDir

    override fun useCacheDir(cacheDir: Path) {
        this.cacheDir = cacheDir
    }

    override var cacheDir: Path = workingDir.resolve("cache")
}