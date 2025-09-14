package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.impl.incremental.cache.PreviousCompilationData
import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalyzer
import java.nio.file.Path
import java.util.Stack

internal fun PreviousCompilationData.classpathChanges(classpath: List<Path>): Set<String> {
    val prev = classpathAnalysis
    val curr = ClassSetAnalyzer.analyzeClasspathSet(classpath)

    val changedClasses = buildSet {
        // changed or deleted classes
        for ((clazz, hash) in prev.classHashes) {
            if (curr.classHashes[clazz] != hash) {
                add(clazz)
            }
        }
    }

    return changedClasses
}

internal data class DirtyClass(
    val name: String,
    val reason: Reason
) {
    sealed class Reason {
        data object SourceChange : Reason()
        data class Dependency(val dependent: Set<String>) : Reason()
    }
}

internal fun PreviousCompilationData.computeDirtySet(
    changedClasses: Collection<String>,
    deletedClasses: Collection<String>,
    classpathChanges: Collection<String>,
): Collection<DirtyClass> {
    val dirtyMap = hashMapOf<String, DirtyClass>()

    val sourceChanges = changedClasses
        .map { DirtyClass(name = it, reason = DirtyClass.Reason.SourceChange) }
        .associateBy { it.name }

    dirtyMap.putAll(sourceChanges)

    val processDeps = Stack<String>().apply {
        addAll(deletedClasses + changedClasses + classpathChanges)
    }

    while (processDeps.isNotEmpty()) {
        val curr = processDeps.pop()
        outputDirAnalysis.dependents[curr]
            ?.let { dependents ->
                for (dep in dependents) {
                    dirtyMap.compute(dep) { _, dirty ->
                        if (dirty == null) {
                            processDeps.push(dep)
                            DirtyClass(
                                name = dep,
                                reason = DirtyClass.Reason.Dependency(setOf(curr)))
                        } else {
                            if (dirty.reason is DirtyClass.Reason.Dependency) {
                                dirty.copy(
                                    reason = dirty.reason.copy(dependent = dirty.reason.dependent + curr)
                                )
                            } else {
                                dirty
                            }
                        }
                    }
                }
            }
    }

    return dirtyMap.values
}