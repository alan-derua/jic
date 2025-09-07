package com.github.alanderua.jic.cli

import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories

open class BaseJicTest {

    @TempDir
    lateinit var workingDirectory: Path

    val sourcesDir by lazy {
        workingDirectory.resolve("src").also {
            it.createDirectories()
        }
    }

    val outDir by lazy {
        workingDirectory.resolve("out").also {
            it.createDirectories()
        }
    }

    private val buffer = ByteArrayOutputStream()
    private val printStream = PrintStream(buffer)

    protected val jic by lazy {
        Jic(
            workingDir = workingDirectory,
            out = printStream,
            err = printStream
        )
    }

    protected fun getLog(): String = buffer.toString()

    protected fun clearLog() = buffer.reset()

    protected fun Path.writeFile(name: String, content: String): Path {
        return Files.writeString(
            resolve(name),
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}