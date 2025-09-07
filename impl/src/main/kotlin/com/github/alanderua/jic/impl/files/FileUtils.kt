package com.github.alanderua.jic.impl.files

import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.*

internal object FileUtils {
    fun cleanOutDir(out: Path) {
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

    @OptIn(ExperimentalPathApi::class)
    fun moveCacheOutToOut(cacheOut: Path, out: Path) {
        require(cacheOut.isDirectory()) { "'$cacheOut' is not a directory" }
        check(out.exists()) { "'$out' doesn't exist" }

        cacheOut.copyToRecursively(target = out, followLinks = false, overwrite = true)

//        cacheOut.deleteRecursively()
    }

    fun getClassPath(outDir: Path, clazz: String): Path {
        val relPath = Path(clazz.replace('.', '/') + ".class")
        return outDir.resolve(relPath)
    }

    fun deleteClasses(outDir: Path, classes: Collection<String>) {
        for (clazz in classes) {
            getClassPath(outDir, clazz).deleteIfExists()
        }
    }
}