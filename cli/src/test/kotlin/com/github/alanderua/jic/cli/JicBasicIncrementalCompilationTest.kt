package com.github.alanderua.jic.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readBytes

class JicBasicIncrementalCompilationTest : BaseJicTest() {

    @Test
    fun `two connected and one loose`() {
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

        sourcesDir.writeFile(
            "Xyz.java",
            // language=java
            """
                class Xyz {
                    static void zyx() {
                        System.out.println("Lorem ipsum");
                    }
                }
            """.trimIndent()
        )

        jic.exec(
            "-d", outDir.toString(),
            sourcesDir.toString()
        )

        Assertions.assertTrue(
            getLog().trimEnd().endsWith("Compilation successful!")
        )

        val expectedClasses = listOf(
            "Foo.class",
            "Bar.class",
            "Xyz.class",
        ).map { outDir.resolve(it) }

        Assertions.assertTrue(
            expectedClasses.all { it.exists() }
        )

        val classesContent = expectedClasses.associateWith { it.readBytes() }

        clearLog()

        sourcesDir.writeFile(
            "Bar.java",
            // language=java
            """
                class Bar {
                    static String bar() {
                        return "barfoo";
                    }
                }
            """.trimIndent()
        )

        jic.exec(
            "-d", outDir.toString(),
            sourcesDir.toString()
        )

        Assertions.assertTrue(
            getLog().contains(
                """
                    d: Need to be recompiled because of source changes:
                        Bar
                    d: Need to be recompiled because of dependencies:
                        Foo -> Bar
                    i: Performing an incremental build
                    Compilation successful!
                """.trimIndent()
            )
        )

        val classesContentAfterIc = expectedClasses.associateWith { it.readBytes() }

        Assertions.assertEquals(
            listOf(
                "Foo.class", // Foo was recompiled because Bar references Foo type but the bytecode produced is the same
                "Xyz.class"
            ),
            classesContentAfterIc.filter { (path, bytes) ->
                classesContent[path].contentEquals(bytes)
            }.keys.map { it.name }
        )
    }
}