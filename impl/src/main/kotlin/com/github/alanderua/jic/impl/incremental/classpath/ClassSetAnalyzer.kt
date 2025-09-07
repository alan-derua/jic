package com.github.alanderua.jic.impl.incremental.classpath

import com.github.alanderua.jic.impl.incremental.dependencies.ClassDependencies
import com.github.alanderua.jic.impl.incremental.dependencies.ClassDependenciesReader
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream

internal object ClassSetAnalyzer {

    fun analyzeOutputDir(outputDir: Path, classes: Set<String>): ClassSetAnalysis {
        val dependents = mutableMapOf<String, MutableSet<String>>()

        for (clazz in classes) {
            readClass(outputDir, clazz).use {
                ClassDependenciesReader.readDependencies(it)
                    .addToDependents(dependents)
            }
        }

        return ClassSetAnalysis(
            dependents = dependents
        )
    }

    private fun ClassDependencies.addToDependents(dependents: MutableMap<String, MutableSet<String>>) {
        for (dep in dependencies) {
            dependents.getOrPut(dep) { hashSetOf() }
                .add(className)
        }
    }

    private fun readClass(baseDir: Path, className: String): InputStream {
        val classSegments = className.split('.')
        val relativePath = Path(classSegments.joinToString(File.separator) + ".class" )
        val fullPath = baseDir.resolve(relativePath)

        return fullPath.inputStream()
    }
}