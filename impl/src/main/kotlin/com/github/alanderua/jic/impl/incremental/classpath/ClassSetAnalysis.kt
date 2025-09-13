package com.github.alanderua.jic.impl.incremental.classpath

import kotlinx.serialization.Serializable

@Serializable
internal data class ClassSetAnalysis(
    val classHashes: Map<String, Long>,
    val dependents: Map<String, Set<String>>
)

internal fun ClassSetAnalysis.merge(
    newAnalysis: ClassSetAnalysis,
    compiledClasses: Set<String> = emptySet(),
    deletedClasses: Set<String> = emptySet()
): ClassSetAnalysis {

    val mergedDependents = buildMap {
        putAll( dependents.filterKeys { it !in deletedClasses && it !in compiledClasses })
        for ((clazz, depClasses) in newAnalysis.dependents) {
            put(clazz, depClasses + (dependents[clazz] ?: emptySet()))
        }
    }

    val mergedHashes = classHashes + newAnalysis.classHashes

    return ClassSetAnalysis(
        classHashes = mergedHashes,
        dependents = mergedDependents
    )
}