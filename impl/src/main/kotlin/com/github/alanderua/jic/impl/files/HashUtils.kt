package com.github.alanderua.jic.impl.files

import java.io.InputStream
import java.util.zip.CRC32C

internal fun InputStream.crc32c(): Long {
    val crc = CRC32C()
    val buffer = ByteArray(1 shl 13)
    var read = read(buffer)
    while (read >= 0) {
        if (read > 0) crc.update(buffer, 0, read)
        read = read(buffer)
    }
    return crc.value
}

internal val ByteArray.crc32c: Long
    get() = CRC32C().also { it.update(this) }.value