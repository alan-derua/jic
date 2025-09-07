package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.impl.files.fingerprint
import com.github.alanderua.jic.impl.files.hash
import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalyzer
import java.nio.file.Path
import kotlin.collections.iterator
import kotlin.io.path.relativeToOrSelf

internal object CompilationAnalyzer {

    fun assembleCache(
        classesBySources: Map<Path, Set<String>>,
        classpath: Collection<Path>,
        workingDir: Path,
        outDir: Path
    ): PreviousCompilationData {
        val metaBySource = buildMap {
            for ((source, classes) in classesBySources) {
                put(
                    source.relativeToOrSelf(workingDir).toString(),
                    SourceFileMeta(
                        fingerPrint = source.fingerprint,
                        hash = source.hash,
                        generatedClasses = classes
                    )
                )
            }
        }

        val generatedClasses = classesBySources.flatMap { it.value }.toSet()

        val outputDirAnalysis = ClassSetAnalyzer.analyzeOutputDir(
            outputDir = outDir,
            classes = generatedClasses
        )

        return PreviousCompilationData(
            outputDirAnalysis = outputDirAnalysis,
            classpath = classpath.map { it.toString() },
            metaDataBySource = metaBySource
        )
    }
}