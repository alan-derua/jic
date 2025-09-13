package com.github.alanderua.jic.impl.incremental.classpath

import com.github.alanderua.jic.impl.files.crc32c
import com.github.alanderua.jic.impl.files.visitClasses
import com.github.alanderua.jic.impl.incremental.dependencies.ClassDependencies
import com.github.alanderua.jic.impl.incremental.dependencies.ClassDependenciesReader
import java.nio.file.Path

internal object ClassSetAnalyzer {

    fun analyzeClasspathSet(classpath: Collection<Path>): ClassSetAnalysis {
        return classpath
            .map { analyzeClasspathEntry(it) }
            .reduceOrNull { acc, analysis ->
                acc.merge(analysis)
            }
            ?: ClassSetAnalysis(emptyMap(), emptyMap())
    }

    fun analyzeClasspathEntry(classpathEntry: Path): ClassSetAnalysis {
        return analyzePath(classpathEntry)
    }

    fun analyzeOutputDir(outputDir: Path): ClassSetAnalysis {
        return analyzePath(outputDir)
    }

    private fun analyzePath(path: Path): ClassSetAnalysis {
        val dependents = mutableMapOf<String, MutableSet<String>>()
        val hashes = mutableMapOf<String, Long>()

        path.visitClasses {
            val classContent = it.readAllBytes()
            val hash = classContent.crc32c // TODO: compute actual ABI hash?
            val analysis = ClassDependenciesReader.readDependencies(classContent)
            analysis.addToDependents(dependents)
            hashes[analysis.className] = hash
        }

        return ClassSetAnalysis(
            classHashes = hashes,
            dependents = dependents
        )
    }

    private fun ClassDependencies.addToDependents(dependents: MutableMap<String, MutableSet<String>>) {
        for (dep in dependencies) {
            dependents.getOrPut(dep) { hashSetOf() }
                .add(className)
        }
    }
}