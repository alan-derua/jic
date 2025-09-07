package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.api.JicLogger
import java.nio.file.Path

internal class CompilationCacheManager private constructor(
    private val compileCacheDataStore: CompileCacheDataStore
) {

    val previousCompilationData: PreviousCompilationData?
        get() = compileCacheDataStore.retrievePreviousCompilationData()

    fun processFullCompilationData(data: PreviousCompilationData) {
        compileCacheDataStore.savePreviousCompilationData(data)
    }

    fun processIncrementalCompilationData(
        incData: PreviousCompilationData,
        compiledClasses: Set<String>,
        deletedClasses: Set<String>,
        deletedFiles: Set<String>
    ) {
        val prevData = previousCompilationData

        check(prevData != null) {
            "Previous compilation data is null!"
        }

        val mergedData = prevData.merge(
            newData = incData,
            compiledClasses = compiledClasses,
            deletedClasses = deletedClasses,
            deletedFiles = deletedFiles
        )

        compileCacheDataStore.savePreviousCompilationData(mergedData)
    }

    companion object {
        fun create(logger: JicLogger, cacheDir: Path): CompilationCacheManager {
            val compileCacheDataStore = CompileCacheDataStore.create(
                logger = logger,
                cacheDir = cacheDir
            )
            return CompilationCacheManager(compileCacheDataStore)
        }
    }
}