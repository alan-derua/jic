package com.github.alanderua.jic.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class JicHelpTest {

    val buffer = ByteArrayOutputStream()
    val printStream = PrintStream(buffer)
    val jic = Jic(out = printStream)

    @Test
    fun `-version returns runtime version`() {
        jic.exec("-version")

        Assertions.assertEquals(
            "javac ${System.getProperty("java.version")}",
            buffer.toString().trimEnd()
        )
    }

    @Test
    fun `no args return usage instructions`() {
        jic.exec()

        Assertions.assertEquals(
            usageString,
            buffer.toString().trimEnd()
        )
    }
}