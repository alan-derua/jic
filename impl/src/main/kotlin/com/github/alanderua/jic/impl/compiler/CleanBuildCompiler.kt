package com.github.alanderua.jic.impl.compiler

import com.github.alanderua.jic.api.CompilationResult
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.visitFileTree

internal class CleanBuildCompiler(
    val actualCompiler: Compiler
) : Compiler by actualCompiler {

    override fun compile(
        sources: List<Path>,
        classpath: List<Path>,
        out: Path
    ): CompilationResult {
        cleanOutDir(out)
        return actualCompiler.compile(sources = sources, classpath = classpath, out = out)
    }

    private fun cleanOutDir(out: Path) {
        if (out.exists() && out.isDirectory()) {
            out.visitFileTree {
                onVisitFile { file, _ ->
                    if (file.isRegularFile() && file.extension == "class") {
                        file.deleteIfExists()
                    }
                    FileVisitResult.CONTINUE
                }

                onPostVisitDirectory { dir, _ ->
                    if (dir.listDirectoryEntries().isEmpty()) {
                        dir.deleteIfExists()
                    }
                    FileVisitResult.CONTINUE
                }
            }
        }
    }
}