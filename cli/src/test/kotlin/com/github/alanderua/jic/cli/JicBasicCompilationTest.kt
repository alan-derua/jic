package com.github.alanderua.jic.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists

class JicBasicCompilationTest {

    @TempDir
    lateinit var workingDirectory: Path

    val sourcesDir by lazy {
        workingDirectory.resolve("src").also {
            Files.createDirectories(it)
        }
    }

    val outDir by lazy {
        workingDirectory.resolve("out").also {
            Files.createDirectories(it)
        }
    }

    fun Path.createFile(name: String, content: String): Path {
        return Files.writeString(
            resolve(name),
            // language=java
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    val buffer = ByteArrayOutputStream()
    val printStream = PrintStream(buffer)
    val jic by lazy {
        Jic(
            workingDir = workingDirectory,
            out = printStream,
            err = printStream
        )
    }

    @Test
    fun `simple two files compilation`() {
        sourcesDir.createFile(
            "Foo.java",
            // language=java
            """
                class Foo {
                    void foo() {
                        System.out.println(Bar.bar());
                    }
                }
            """.trimIndent()
        )

        sourcesDir.createFile(
            "Bar.java",
            // language=java
            """
                class Bar {
                    static String bar() {
                        return "foobar";
                    }
                }
            """.trimIndent()
        )

        jic.exec(
            sourcesDir.toString()
        )

        Assertions.assertTrue(
            buffer.toString().trimEnd().endsWith("Compilation successful!")
        )

        Assertions.assertTrue(
            workingDirectory.resolve("Foo.class").exists()
        )
        Assertions.assertTrue(
            workingDirectory.resolve("Bar.class").exists()
        )
    }

    @Test
    fun `Syntax error fail`() {
        sourcesDir.createFile(
            "Foo.java",
            // language=java
            """
                class Foo {
                    void foo() {
                        System.out.println("Hello world!")
                    }
                }
            """.trimIndent()
        )

        jic.exec(
            sourcesDir.toString()
        )

        Assertions.assertTrue(
            buffer.toString().contains("src/Foo.java:3:43: error: ';' expected")
        )
    }
}