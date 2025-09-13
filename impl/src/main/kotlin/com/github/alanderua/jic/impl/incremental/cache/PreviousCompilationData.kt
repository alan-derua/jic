package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalysis
import com.github.alanderua.jic.impl.incremental.classpath.merge
import kotlinx.serialization.Serializable

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