package com.github.alanderua.jic.impl.files

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.CRC32C
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readAttributes

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

        val crc = CRC32C()
        FileChannel.open(this).use { ch ->
            val buf = ByteBuffer.allocateDirect(1 shl 20)
            while (true) {
                val n = ch.read(buf)
                if (n < 0) break
                buf.flip()
                crc.update(buf)
                buf.clear()
            }
        }
        return crc.value
    }