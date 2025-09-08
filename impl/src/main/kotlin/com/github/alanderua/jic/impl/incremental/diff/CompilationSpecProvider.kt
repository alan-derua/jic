package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.files.FileUtils
import com.github.alanderua.jic.impl.files.fingerprint
import com.github.alanderua.jic.impl.files.hash
import com.github.alanderua.jic.impl.incremental.cache.CompilationCacheManager
import com.github.alanderua.jic.impl.incremental.cache.PreviousCompilationData
import com.github.alanderua.jic.impl.logdPrettyPaths
import java.nio.file.Path
import java.util.Stack
import kotlin.io.path.Path
import kotlin.io.path.exists

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

        if (!previousCompilationData.verifyOutDir(outDir)) {
            return CompilationSpec.FullRecompilation(
                "Class files were removed or modified"
            )
        }

        val fileChanges = previousCompilationData.computeFilesChange(sources)
        val classpathChanges = previousCompilationData.classpathChanged(classpath)

        if (fileChanges.isEmpty() && !classpathChanges) {
            return CompilationSpec.CompilationNotNeeded(
                "No classpath and no source file changes"
            )
        }

        if (classpathChanges) {
            return CompilationSpec.FullRecompilation(
                "Classpath has changed"
            )
        }

        val classesToDelete = fileChanges.deleted.flatMap { deletedFile ->
            previousCompilationData.metaDataBySource[deletedFile.toString()]
                ?.generatedClasses ?: emptyList()
        }.toSet()

        val changedClasses = fileChanges.modified.flatMap { modifiedFile ->
            previousCompilationData.metaDataBySource[modifiedFile.toString()]
                ?.generatedClasses ?: emptyList()
        }

        val changedClassesLogStr = changedClasses.joinToString(
            prefix = "Need to be recompiled because of source changes:\n",
            separator = "\n"
        ) { "    $it" }
        logger.d(changedClassesLogStr)

        val dirtySet = hashSetOf<String>()
        dirtySet += changedClasses

        val processDeps = Stack<String>().apply {
            addAll(classesToDelete + changedClasses)
        }

        val reasons = mutableMapOf<String, MutableSet<String>>()

        while (processDeps.isNotEmpty()) {
            val curr = processDeps.pop()
            previousCompilationData.outputDirAnalysis
                .dependents[curr]
                ?.let { dependents ->
                    val newDeps = dependents - dirtySet

                    if (newDeps.isNotEmpty()) {
                        reasons.getOrPut(curr) { mutableSetOf() }
                            .addAll(newDeps)

                        dirtySet += newDeps
                        processDeps.addAll(newDeps)
                    }
                }
        }

        if (reasons.isNotEmpty()) {
            val reasonsStr = reasons.entries.joinToString(
                prefix = "Need to be recompiled because of dependencies:\n",
                separator = "\n"
            ) { (dependency, dependents) ->
                dependents.joinToString(
                    separator = "\n"
                ) { "    $it -> $dependency" }
            }
            logger.d(reasonsStr)
        }

        val sourcesToRecompile = previousCompilationData.findSourcesForClasses(dirtySet)

        return CompilationSpec.IncrementalCompilation(
            sourcesToRecompile = sourcesToRecompile,
            classesToDelete = classesToDelete,
            deletedFiles = fileChanges.deleted.map { it.toString() }.toSet()
        )
    }

    private fun PreviousCompilationData.findSourcesForClasses(
        classes: Collection<String>
    ): Set<Path> {
        val sourceByClass = buildMap {
            for ((source, meta) in metaDataBySource) {
                for (clazz in meta.generatedClasses) {
                    put(clazz, source)
                }
            }
        }

        val sources =  classes.mapNotNull { sourceByClass[it] }.toSet()

        return sources.map { Path(it) }.toSet()
    }

    private fun PreviousCompilationData.classpathChanged(classpath: List<Path>): Boolean {
        // TODO: actual ABI diff computation

        return classpath != this.classpath.map { Path(it) }
    }

    private fun PreviousCompilationData.computeFilesChange(sources: List<Path>): FileChanges {
        val prevFiles = metaDataBySource.map { (source, meta) ->
            Path(source) to meta
        }.toMap()

        val sourcesSet = sources.toSet()

        val deletedFiles = (prevFiles.keys - sourcesSet)

        if (deletedFiles.isNotEmpty()) {
            logger.logdPrettyPaths("Deleted sources", deletedFiles)
        }

        val modifiedFiles = buildSet {
            for (source in sources) {
                if (source.fingerprint != prevFiles[source]?.fingerPrint) {
                    if (source.hash != prevFiles[source]?.hash) {
                        add(source)
                    }
                }
            }
        }

        if (modifiedFiles.isNotEmpty()) {
            logger.logdPrettyPaths("Modified sources", modifiedFiles)
        }

        return FileChanges(
            modified = modifiedFiles,
            deleted = deletedFiles
        )
    }

    private fun PreviousCompilationData.verifyOutDir(outDir: Path): Boolean {
        for ((_, meta) in metaDataBySource) {
            val classes = meta.generatedClasses
            for (clazz in classes) {
                val classFile = FileUtils.getClassPath(outDir, clazz)
                if (!classFile.exists()) return false
            }
        }
        return true
    }

    private data class FileChanges(
        val modified: Set<Path>,
        val deleted: Set<Path>
    ) {
        fun isEmpty(): Boolean = modified.isEmpty() && deleted.isEmpty()
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