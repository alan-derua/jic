package com.github.alanderua.jic.impl

import com.github.alanderua.jic.api.CompilationConfig
import com.github.alanderua.jic.api.CompilationResult
import com.github.alanderua.jic.api.CompilationService
import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.compiler.Compiler
import com.github.alanderua.jic.impl.compiler.CompilerResult
import com.github.alanderua.jic.impl.compiler.ToolchainCompiler
import com.github.alanderua.jic.impl.files.FileUtils
import com.github.alanderua.jic.impl.incremental.cache.CompilationAnalyzer
import com.github.alanderua.jic.impl.incremental.cache.CompilationCacheManager
import com.github.alanderua.jic.impl.incremental.diff.CompilationSpec
import com.github.alanderua.jic.impl.incremental.diff.CompilationSpecProvider
import java.nio.file.Path
import kotlin.io.path.relativeToOrSelf

internal class CompilationServiceImpl : CompilationService {

    private var logger: JicLogger = SystemJicLogger

    override fun useLogger(logger: JicLogger) {
        this.logger = logger
    }

    private val compiler: Compiler
        get() = ToolchainCompiler(logger)

    override fun makeCompilationConfig(): CompilationConfig {
        return CompilationConfigImpl()
    }

    override fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        config: CompilationConfig
    ): CompilationResult {
        val normalizedSources = sources.map { it.relativeToOrSelf(config.workingDir) }

        logger.logdPrettyPaths("Sources list", normalizedSources)
        logger.logdPrettyPaths("Classpath", classpath, config.workingDir)

        val cacheManager = CompilationCacheManager.create(
            logger = logger,
            cacheDir = config.cacheDir
        )

        val compilationSpecProvider = CompilationSpecProvider.create(
            compilationCacheManager = cacheManager,
            outDir = config.out,
            logger = logger
        )

        val spec = compilationSpecProvider.computeCompilationSpec(
            sources = normalizedSources,
            classpath = classpath,
            forceRecompile = config.forceRecompile
        )

        val result = when(spec) {
            is CompilationSpec.FullRecompilation -> {
                logger.i("Performing a clean build, because: ${spec.reason}")

                compileFull(
                    sources = normalizedSources,
                    classpath = classpath,
                    config = config,
                    compilationCacheManager = cacheManager
                )
            }
            is CompilationSpec.IncrementalCompilation -> {
                logger.i("Performing an incremental build")

                compileIncremental(
                    sources = spec.sourcesToRecompile,
                    classpath = classpath,
                    classesToDelete = spec.classesToDelete,
                    deletedFiles = spec.deletedFiles,
                    config = config,
                    compilationCacheManager = cacheManager
                )
            }
            is CompilationSpec.CompilationNotNeeded -> {
                logger.i("Compilation not needed: ${spec.reason}")
                CompilerResult.CompilationNotNeeded
            }
        }

        return when (result) {
            CompilerResult.Error -> CompilationResult.Error
            CompilerResult.CompilationNotNeeded,
            is CompilerResult.Success -> CompilationResult.Success
        }
    }

    private fun compileFull(
        sources: Collection<Path>,
        classpath: Collection<Path>,
        config: CompilationConfig,
        compilationCacheManager: CompilationCacheManager,
    ): CompilerResult {
        FileUtils.cleanOutDir(config.out)

        val result = compiler.compile(
            sources = sources,
            classpath = classpath,
            out = config.out
        )

        if (result is CompilerResult.Success) {
            val cache = CompilationAnalyzer.assembleCache(
                classesBySources = result.classesBySources,
                classpath = classpath,
                workingDir = config.workingDir,
                outDir = config.out
            )
            compilationCacheManager.processFullCompilationData(cache)
        }

        return result
    }

    private fun compileIncremental(
        sources: Collection<Path>,
        classpath: Collection<Path>,
        classesToDelete: Set<String>,
        deletedFiles: Set<String>,
        config: CompilationConfig,
        compilationCacheManager: CompilationCacheManager
    ): CompilerResult {
        val cacheOutDir = config.cacheDir.resolve("out")
        val incrementalClasspath = listOf(config.out) + classpath

        FileUtils.cleanOutDir(cacheOutDir)

        val result = compiler.compile(
            sources = sources,
            classpath = incrementalClasspath,
            out = cacheOutDir
        )

        if (result is CompilerResult.Success) {
            FileUtils.moveCacheOutToOut(cacheOutDir, config.out)
            FileUtils.deleteClasses(outDir = config.out, classesToDelete)

            val cache = CompilationAnalyzer.assembleCache(
                classesBySources = result.classesBySources,
                classpath = classpath,
                workingDir = config.workingDir,
                outDir = config.out
            )

            val compiledClasses = result.classesBySources.flatMap { it.value }.toSet()

            compilationCacheManager.processIncrementalCompilationData(
                incData = cache,
                compiledClasses = compiledClasses,
                deletedClasses = classesToDelete,
                deletedFiles = deletedFiles
            )
        }

        return result
    }

    override val compilerVersion: String
        get() = compiler.version
}