package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalysis
import com.github.alanderua.jic.impl.incremental.classpath.merge
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.Path

@Serializable
internal data class PreviousCompilationData(
    val metaDataBySource: Map<String, SourceFileMeta>,
    val outputDirAnalysis: ClassSetAnalysis,
    val classpathAnalysis: ClassSetAnalysis
)

@Serializable
internal data class SourceFileMeta(
    val fingerPrint: Long,
    val hash: Long,
    val generatedClasses: Set<String>,
)

internal fun PreviousCompilationData.merge(
    newData: PreviousCompilationData,
    compiledClasses: Set<String>,
    deletedClasses: Set<String>,
    deletedFiles: Set<String>
): PreviousCompilationData {

    val mergedMetaMap = metaDataBySource.filterKeys { it !in deletedFiles } + newData.metaDataBySource

    return PreviousCompilationData(
        metaDataBySource = mergedMetaMap,
        outputDirAnalysis = outputDirAnalysis.merge(
            newAnalysis = newData.outputDirAnalysis,
            compiledClasses = compiledClasses,
            deletedClasses = deletedClasses
        ),
        classpathAnalysis = newData.classpathAnalysis
    )
}

internal fun PreviousCompilationData.getClassesForSource(
    source: Path
): Result<Collection<String>> = runCatching {
    metaDataBySource[source.toString()]
        ?.generatedClasses ?: error("Couldn't find any classes for the source: $source")
}

internal fun PreviousCompilationData.findSourcesForClasses(
    classes: Collection<String>
): Result<Set<Path>> = runCatching {
    val sourceByClass = buildMap {
        for ((source, meta) in metaDataBySource) {
            for (clazz in meta.generatedClasses) {
                put(clazz, source)
            }
        }
    }

    val sources = buildSet {
        for (clazz in classes) {
            val source = sourceByClass[clazz]
                ?: error("Couldn't find source file for '$clazz'")
            add(Path(source))
        }
    }

    sources
}
