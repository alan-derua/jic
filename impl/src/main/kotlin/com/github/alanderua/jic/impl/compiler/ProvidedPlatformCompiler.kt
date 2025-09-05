package com.github.alanderua.jic.impl.compiler

import com.github.alanderua.jic.api.CompilationResult
import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.toDiagnosticsListener
import com.github.alanderua.jic.impl.toWriter
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.tools.JavaCompiler
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import kotlin.io.path.createDirectories

internal class ProvidedPlatformCompiler(
    logger: JicLogger
) : Compiler {

    private val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        ?: error( "Could not load java compiler! Are you running on JDK 17+?")

    private val fileManager
        get() = compiler.getStandardFileManager(diagnosticListener, null, null)

    private val diagnosticListener = logger.toDiagnosticsListener()

    private val outWriter = logger.toWriter()

    override val version: String
        get() {
            return ByteArrayOutputStream().use { stream ->
                val code = compiler.run(null, stream, stream, "-version")

                if (code != 0) {
                    error("Failed to get compiler version with code '$code': $stream")
                } else {
                    stream.toString(Charsets.UTF_8)
                }
            }
        }

    override fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        out: Path,
    ): CompilationResult = fileManager.use { fileManager ->
        out.createDirectories()

        val untis = fileManager.getJavaFileObjectsFromPaths(sources)

        fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, listOf(out))
        fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath)

        val options = listOfNotNull(
            "-implicit:none",
//            "-verbose"
        )

        val task = compiler.getTask(
            outWriter,
            fileManager,
            diagnosticListener,
            options,
            null,
            untis
        )

        val result = task.call()

        return if (result) {
            CompilationResult.Success
        } else {
            CompilationResult.Error
        }
    }
}
