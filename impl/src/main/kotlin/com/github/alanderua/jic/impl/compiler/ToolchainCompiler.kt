package com.github.alanderua.jic.impl.compiler

import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.toDiagnosticsListener
import com.github.alanderua.jic.impl.toWriter
import com.sun.source.util.JavacTask
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import javax.lang.model.util.Elements
import javax.tools.JavaCompiler
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import kotlin.io.path.createDirectories

internal class ToolchainCompiler(
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
        sources: Collection<Path>,
        classpath: Collection<Path>,
        out: Path,
    ): CompilerResult = fileManager.use { fileManager ->
        out.createDirectories()

        val untis = fileManager.getJavaFileObjectsFromPaths(sources)

        fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, listOf(out))
        fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath)

        val options = listOfNotNull(
            "-implicit:none",
            "--release", "8",
            "-Xlint:-options"
//            "-verbose"
        )

        val task = compiler.getTask(
            outWriter,
            fileManager,
            diagnosticListener,
            options,
            null,
            untis
        ) as JavacTask

        val collector = SourceToClassesCollector(task.elements)

        task.addTaskListener(collector)

        return if (task.call()) {
            CompilerResult.Success(
                classesBySources = collector.getCollectedClasses()
            )
        } else {
            CompilerResult.Error
        }
    }
}

private class SourceToClassesCollector(
    private val elements: Elements
) : TaskListener {

    private val classesBySource = mutableMapOf<Path, MutableSet<String>>()

    fun getCollectedClasses(): Map<Path, Set<String>> {
        return classesBySource
    }

    override fun finished(e: TaskEvent?) {
        if (e?.kind != TaskEvent.Kind.GENERATE) return

        val src = e.sourceFile ?: return
        val type = e.typeElement ?: return

        val srcPath = Paths.get(src.toUri())

        val binaryName = elements.getBinaryName(type).toString()

        classesBySource.getOrPut(srcPath) { mutableSetOf() }
            .add(binaryName)
    }
}