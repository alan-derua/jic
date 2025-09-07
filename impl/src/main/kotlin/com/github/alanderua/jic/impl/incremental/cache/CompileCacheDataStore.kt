package com.github.alanderua.jic.impl.incremental.cache

import com.github.alanderua.jic.api.JicLogger
import com.github.alanderua.jic.impl.json
import com.github.alanderua.jic.impl.protobuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.encodeToStream
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

@OptIn(ExperimentalSerializationApi::class)
internal class CompileCacheDataStore private constructor(
    private val cacheDir: Path,
    private val logger: JicLogger
) {

    fun savePreviousCompilationData(data: PreviousCompilationData) {
        cacheDir.createDirectories()

        cacheDir.resolve("previousCompilation.json").apply {
            outputStream().use {
                json.encodeToStream(data, it)
            }
        }

        cacheDir.resolve(CACHE_FILE_NAME).apply {
            writeBytes(protobuf.encodeToByteArray(data))
        }
    }

    fun retrievePreviousCompilationData(): PreviousCompilationData? {
        val cacheFile = cacheDir.resolve(CACHE_FILE_NAME)

        if (cacheFile.notExists()) {
            logger.i("Incremental compilation cache file doesn't exist")
            return null
        }

        val byteArray = try {
            cacheFile.readBytes()
        } catch (e: IOException) {
            logger.e("Failed to read incremental compilation cache file", e)
            return null
        }

        return try {
            protobuf.decodeFromByteArray<PreviousCompilationData>(byteArray)
        } catch (e: IllegalArgumentException) {
            logger.e("Failed to decode incremental compilation cache file", e)
            null
        } catch (e: SerializationException) {
            logger.e("Failed to decode incremental compilation cache file", e)
            null
        }
    }

    companion object {
        private const val CACHE_FILE_NAME = "previousCompilation.bin"

        fun create(logger: JicLogger, cacheDir: Path): CompileCacheDataStore {
            return CompileCacheDataStore(cacheDir, logger)
        }
    }
}