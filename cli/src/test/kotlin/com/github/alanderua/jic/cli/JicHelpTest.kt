package com.github.alanderua.jic.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JicHelpTest : BaseJicTest() {

    @Test
    fun `-version returns runtime version`() {
        jic.exec("-version")

        Assertions.assertEquals(
            "javac ${System.getProperty("java.version")}",
            getLog().trimEnd()
        )
    }

    @Test
    fun `no args return usage instructions`() {
        jic.exec()

        Assertions.assertEquals(
            usageString,
            getLog().trimEnd()
        )
    }
}