package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.incremental.cache.CompilationCacheManager
import com.github.alanderua.jic.impl.incremental.cache.findSourcesForClasses
import com.github.alanderua.jic.impl.incremental.cache.getClassesForSource
import com.github.alanderua.jic.impl.logdPrettyPaths
import java.nio.file.Path

internal class CompilationSpecProvider private constructor(
    private val outDir: Path,
    private val compilationCacheManager: CompilationCacheManager,
    private val logger: JicLogger,
) {

    fun computeCompilationSpec(
        sources: List<Path>,
        classpath: List<Path>,
        forceRecompile: Boolean
    ): CompilationSpec {
        if (forceRecompile) {
            return CompilationSpec.FullRecompilation(
                "Forced recompile requested"
            )
        }

        val previousCompilationData = compilationCacheManager.previousCompilationData
            ?: return CompilationSpec.FullRecompilation("Compilation cache is not available")

        previousCompilationData.verifyOutDir(outDir)
            .onFailure {
                logger.e("Output dir verification failed: ${it.message}", it)
                return CompilationSpec.FullRecompilation(
                    "Class files were removed or modified"
                )
            }

        val fileChanges = previousCompilationData.computeFilesChange(sources)
        val classpathChanges = previousCompilationData.classpathChanges(classpath)

        logFileChanges(fileChanges)
        logClasspathChanges(classpathChanges)

        if (fileChanges.isEmpty() && classpathChanges.isEmpty()) {
            return CompilationSpec.CompilationNotNeeded(
                "No classpath and no source file changes"
            )
        }

        val deletedClasses = fileChanges.deleted.flatMap { deletedFile ->
            previousCompilationData.getClassesForSource(deletedFile)
                .onFailure {
                    logger.e("Couldn't find class files: ${it.message}", it)
                    return CompilationSpec.FullRecompilation(
                        "Invalid mapping of source to class files"
                    )
                }
                .getOrThrow()
        }.toSet()

        val changedClasses = fileChanges.modified.flatMap { modifiedFile ->
            previousCompilationData.getClassesForSource(modifiedFile)
                .onFailure {
                    logger.e("Couldn't find class files: ${it.message}", it)
                    return CompilationSpec.FullRecompilation(
                        "Invalid mapping of source to class files"
                    )
                }
                .getOrThrow()
        }

        val dirtySet = previousCompilationData.computeDirtySet(
            changedClasses = changedClasses,
            deletedClasses = deletedClasses,
            classpathChanges = classpathChanges,
        )

        logDirtySet(dirtySet)

        val sourcesToRecompile = previousCompilationData.findSourcesForClasses(dirtySet.map { it.name })
            .onFailure {
                logger.e("Failed to find sources: ${it.message}", it)
                return CompilationSpec.FullRecompilation("Error finding sources for recompilation")
            }
            .getOrThrow()

        return CompilationSpec.IncrementalCompilation(
            sourcesToRecompile = sourcesToRecompile,
            classesToDelete = deletedClasses,
            deletedFiles = fileChanges.deleted.map { it.toString() }.toSet()
        )
    }

    private fun logFileChanges(fileChanges: FileChanges) {
        if (fileChanges.deleted.isNotEmpty()) {
            logger.logdPrettyPaths("Deleted sources", fileChanges.deleted)
        }

        if (fileChanges.modified.isNotEmpty()) {
            logger.logdPrettyPaths("Modified sources", fileChanges.modified)
        }
    }

    private fun logClasspathChanges(classpathChanges: Collection<String>) {
        if (classpathChanges.isNotEmpty()) {
            val changedClassesLogStr = classpathChanges.joinToString(
                prefix = "Changed or deleted classpath files:\n",
                separator = "\n"
            ) { "    $it" }
            logger.d(changedClassesLogStr)
        }
    }

    private fun logDirtySet(dirtySet: Collection<DirtyClass>) {
        val reasonsStr = dirtySet
            .sortedBy { if (it.reason is DirtyClass.Reason.SourceChange) 0 else 1 }
            .joinToString(
                prefix = "Files marked for recompilation:\n",
                separator = "\n"
            ) { (clazz, reason) ->
                when(reason) {
                    is DirtyClass.Reason.Dependency -> {
                        "    $clazz -> depends on ${reason.dependent}"
                    }
                    DirtyClass.Reason.SourceChange -> {
                        "    $clazz -> source change"
                    }
                }
            }
        logger.d(reasonsStr)
    }

    companion object {
        fun create(
            outDir: Path,
            compilationCacheManager: CompilationCacheManager,
            logger: JicLogger
        ): CompilationSpecProvider {
            return CompilationSpecProvider(outDir, compilationCacheManager, logger)
        }
    }
}