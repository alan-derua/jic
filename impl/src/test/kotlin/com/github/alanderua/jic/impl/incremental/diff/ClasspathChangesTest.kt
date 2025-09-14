package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.impl.incremental.cache.PreviousCompilationData
import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalysis
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ClasspathChangesTest {

    private fun analysis(classHashes: Map<String, Long>) =
        ClassSetAnalysis(classHashes, emptyMap())

    private fun data(prev: Map<String, Long>) =
        PreviousCompilationData(
            metaDataBySource = emptyMap(),
            outputDirAnalysis = analysis(emptyMap()),
            classpathAnalysis = analysis(prev)
        )

    @Test
    fun `returns empty when classpath unchanged`() {
        val prev = mapOf("A" to 1L, "B" to 2L)
        val curr = mapOf("A" to 1L, "B" to 2L)

        val result = data(prev).classpathChanges(analysis(curr))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detects changed hash`() {
        val prev = mapOf("A" to 1L)
        val curr = mapOf("A" to 99L)

        val result = data(prev).classpathChanges(analysis(curr))

        assertEquals(
            setOf("A"),
            result
        )
    }

    @Test
    fun `detects deleted class`() {
        val prev = mapOf("A" to 1L, "B" to 2L)
        val curr = mapOf("A" to 1L)

        val result = data(prev).classpathChanges(analysis(curr))

        assertEquals(
            setOf("B"),
            result
        )
    }

    @Test
    fun `new class does not count as change`() {
        val prev = mapOf("A" to 1L)
        val curr = mapOf("A" to 1L, "C" to 3L)

        val result = data(prev).classpathChanges(analysis(curr))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `class change and class deletion`() {
        val prev = mapOf("A" to 1L, "B" to 2L, "C" to 3L)
        val curr = mapOf("A" to 99L, "C" to 3L)

        val result = data(prev).classpathChanges(analysis(curr))

        assertEquals(
            setOf("A", "B"),
            result
        )
    }
}
