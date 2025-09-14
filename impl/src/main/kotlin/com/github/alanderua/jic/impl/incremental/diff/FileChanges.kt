package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.impl.files.FileUtils
import com.github.alanderua.jic.impl.files.fingerprint
import com.github.alanderua.jic.impl.files.hash
import com.github.alanderua.jic.impl.incremental.cache.PreviousCompilationData
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.exists

internal data class FileChanges(
    val modified: Set<Path>,
    val deleted: Set<Path>
) {
    fun isEmpty(): Boolean = modified.isEmpty() && deleted.isEmpty()
}

internal fun PreviousCompilationData.computeFilesChange(sources: List<Path>): FileChanges {
    val prevFiles = metaDataBySource.map { (source, meta) ->
        Path(source) to meta
    }.toMap()

    val sourcesSet = sources.toSet()

    val deletedFiles = (prevFiles.keys - sourcesSet)

    val modifiedFiles = buildSet {
        for (source in sources) {
            if (source.fingerprint != prevFiles[source]?.fingerPrint) {
                if (source.hash != prevFiles[source]?.hash) {
                    add(source)
                }
            }
        }
    }

    return FileChanges(
        modified = modifiedFiles,
        deleted = deletedFiles
    )
}

internal fun PreviousCompilationData.verifyOutDir(outDir: Path): Result<Unit> = runCatching {
    for ((_, meta) in metaDataBySource) {
        val classes = meta.generatedClasses
        for (clazz in classes) {
            val classFile = FileUtils.getClassPath(outDir, clazz)
            if (!classFile.exists()) {
                error("'$classFile' couldn't be found in the previous output")
            }
            if (classFile.hash != outputDirAnalysis.classHashes[clazz]) {
                error("'$classFile' was modified")
            }
        }
    }
}
