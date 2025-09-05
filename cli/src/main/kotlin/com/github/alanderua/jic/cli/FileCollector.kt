package com.github.alanderua.jic.cli

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.walk

fun collectJavaFiles(src: String): List<Path> {
    val srcPath = Path(src)

    check(srcPath.exists()) {
        "Specified sources dir doesn't exist: '$srcPath'"
    }

    return srcPath.walk()
        .filter { it.extension == "java"}
        .toList()
}

fun collectClasspath(classpath: String): List<Path> {
    val paths = classpath
        .removeSurrounding("\"")
        .split(';',':')

    return paths.flatMap { pathStr ->
        if (pathStr.endsWith("*")) {
            val wildcardPath = Path(pathStr.removeSuffix("*"))

            check(wildcardPath.exists()) {
                "Specified classpath doesn't exist: $pathStr"
            }

            wildcardPath
                .walk()
                .filter { it.extension == "jar" }
                .toList()
        } else {
            val singlePath = Path(pathStr)

            check(singlePath.exists()) {
                "Specified classpath doesn't exist: $pathStr"
            }

            listOf(singlePath)
        }
    }
}