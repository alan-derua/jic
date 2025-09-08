package com.github.alanderua.jic.cli

import com.github.alanderua.jic.api.CompilationResult
import com.github.alanderua.jic.api.CompilationService
import com.github.alanderua.jic.api.JicLogger
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeToOrSelf

fun main(vararg args: String) {
    Jic().exec(*args)
}

class Jic(
    private val workingDir: Path = Path(System.getProperty("user.dir")),
    private val out: PrintStream = System.out,
    private val err: PrintStream = System.err
) {

    private val logger = object : JicLogger {
        override fun e(msg: String, throwable: Throwable?) {
            err.println("e: $msg")
            throwable?.printStackTrace(err)
        }

        override fun w(msg: String) {
            err.println("w: $msg")
        }

        override fun i(msg: String) {
            out.println("i: $msg")
        }

        override fun d(msg: String) {
            out.println("d: $msg")
        }
    }

    private val compilationService by lazy {
        CompilationService.loadImplementation(Jic::javaClass.javaClass.classLoader)
            .apply { useLogger(logger) }
    }

    fun exec(vararg args: String) {
        when (val command = parseCommandLineArgs(args)) {
            is Command.Compile -> {
                val outDir = command.outDir?.let { Path(it) } ?: workingDir

                logger.d("Compiling sources for $command")

                val config = compilationService.makeCompilationConfig().apply {
                    if (command.forceRecompile) {
                        forceRecompile()
                    }
                    useWorkingDir(workingDir)
                    useOut(outDir)
                }

                val compilationResult = compilationService.compile(
                    sources = collectJavaFiles(command.src),
                    classpath = command.cp?.let(::collectClasspath) ?: emptyList(),
                    config = config
                )
                when (compilationResult) {
                    CompilationResult.Error -> err.println("Compilation failed!")
                    CompilationResult.Success -> out.println("Compilation successful!")
                }
            }
            is Command.Print -> out.println(command.msg)
            Command.PrintVersion -> out.println(compilationService.compilerVersion)
        }
    }
}

