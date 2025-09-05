package com.github.alanderua.jic.impl

import com.github.alanderua.jic.api.CompilationConfig
import com.github.alanderua.jic.api.CompilationResult
import com.github.alanderua.jic.api.CompilationService
import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.compiler.CleanBuildCompiler
import com.github.alanderua.jic.impl.compiler.Compiler
import com.github.alanderua.jic.impl.compiler.ProvidedPlatformCompiler
import java.nio.file.Path
import kotlin.io.path.relativeToOrSelf

internal class CompilationServiceImpl : CompilationService {

    private var logger: JicLogger = SystemJicLogger

    override fun useLogger(logger: JicLogger) {
        this.logger = logger
    }

    private val actualCompiler: Compiler
        get() = ProvidedPlatformCompiler(logger)

    override fun makeCompilationConfig(): CompilationConfig {
        return CompilationConfigImpl()
    }

    override fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        config: CompilationConfig
    ): CompilationResult {
        logger.d("Sources list:\n${sources.joinToString("\n") { it.relativeToOrSelf(config.workingDir).toString() }}")
        logger.d("Classpath: \n${classpath.joinToString("\n") { it.relativeToOrSelf(config.workingDir).toString() }}")

        val compiler = CleanBuildCompiler(actualCompiler)

        return compiler.compile(
            sources =  sources,
            classpath = classpath,
            out = config.out
        )
    }

    override val compilerVersion: String
        get() = actualCompiler.version
}