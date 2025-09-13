package com.github.alanderua.jic.impl.files

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipFile
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readAttributes
import kotlin.io.path.walk

internal val Path.fingerprint: Long
    get() {
        check(isRegularFile()) {
            "Fingerprinting of dirs it not supported"
        }
        check(exists()) {
            "'$this' doesn't exist"
        }

        val attrs = readAttributes<BasicFileAttributes>()

        val lastModified = attrs.lastModifiedTime().toMillis()
        val size = attrs.size()

        return (lastModified shl 32) xor size
    }

internal val Path.hash: Long
    get() {
        check(isRegularFile()) {
            "Hashing of dirs it not supported"
        }
        check(exists()) {
            "'$this' doesn't exist"
        }

        return inputStream().use {
            it.crc32c()
        }
    }

internal fun Path.visitClasses(action: (inputStream: InputStream) -> Unit) {
    when {
        isRegularFile() && extension == "jar" -> visitJarClasses(action)
        isDirectory() -> visitDirClasses(action)
        else -> error("Unknown file type: '$this'")
    }
}

internal fun Path.visitJarClasses(action: (inputStream: InputStream) -> Unit) {
    require(isRegularFile() && extension == "jar") {
        "'$this' is not a jar file!"
    }

    ZipFile(this.toFile()).use { jar ->
        jar.entries()
            .asSequence()
            .filter { !it.isDirectory && it.name.endsWith(".class") }
            .forEach { classEntry ->
                jar.getInputStream(classEntry).use {
                    action(it)
                }
            }
    }
}

internal fun Path.visitDirClasses(action: (inputStream: InputStream) -> Unit) {
    require(isDirectory()) {
        "'$this' is not a directory!"
    }

    val classes = walk().filter { it.extension == "class" }

    for (clazz in classes) {
        clazz.inputStream().use {
            action(it)
        }
    }
}