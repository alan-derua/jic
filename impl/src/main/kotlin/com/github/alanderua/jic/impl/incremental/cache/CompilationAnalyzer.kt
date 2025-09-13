package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.impl.files.fingerprint
import com.github.alanderua.jic.impl.files.hash
import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalyzer
import java.nio.file.Path
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

        val outputDirAnalysis = ClassSetAnalyzer.analyzeOutputDir(outputDir = outDir)

        val classpathAnalysis = ClassSetAnalyzer.analyzeClasspathSet(classpath)

        return PreviousCompilationData(
            outputDirAnalysis = outputDirAnalysis,
            classpathAnalysis = classpathAnalysis,
            metaDataBySource = metaBySource
        )
    }
}