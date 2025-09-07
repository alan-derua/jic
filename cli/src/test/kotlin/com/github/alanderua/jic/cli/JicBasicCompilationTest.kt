package com.github.alanderua.jic.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.exists

class JicBasicCompilationTest : BaseJicTest() {

    @Test
    fun `simple two files compilation`() {
        sourcesDir.writeFile(
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

        sourcesDir.writeFile(
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
            getLog().trimEnd().endsWith("Compilation successful!")
        )

        Assertions.assertTrue(
            workingDirectory.resolve("Foo.class").exists()
        )
        Assertions.assertTrue(
            workingDirectory.resolve("Bar.class").exists()
        )
    }

    @Test
    fun `syntax error fail`() {
        sourcesDir.writeFile(
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
            getLog().contains("src/Foo.java:3:43: error: ';' expected")
        )
    }
}