package com.github.alanderua.jic.impl.incremental.diff

import com.github.alanderua.jic.impl.incremental.cache.PreviousCompilationData
import com.github.alanderua.jic.impl.incremental.classpath.ClassSetAnalysis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirtySetTest {

    private fun analysis(
        dependents: Map<String, Set<String>> = emptyMap()
    ) = ClassSetAnalysis(
        classHashes = emptyMap(),
        dependents = dependents
    )

    private fun data(
        dependents: Map<String, Set<String>> = emptyMap()
    ) = PreviousCompilationData(
        metaDataBySource = emptyMap(),
        outputDirAnalysis = analysis(dependents),
        classpathAnalysis = analysis()
    )

    private fun dependencyReason(vararg reason: String) =
        DirtyClass.Reason.Dependency(dependent = reason.toSet())

    @Test
    fun `empty input result in empty dirty set`() {
        val data = data(
            dependents = mapOf(
                "A" to setOf("B"),
                "X" to setOf("Y")
            )
        )
        val dirty = data.computeDirtySet(
            changedClasses = emptyList(),
            deletedClasses = emptyList(),
            classpathChanges = emptyList()
        )

        assertEquals(
            emptyList<DirtyClass>(),
            dirty.toList()
        )
    }

    @Test
    fun `changed class is marked as SourceChange`() {
        val data = data()
        val dirty = data.computeDirtySet(
            changedClasses = listOf("A"),
            deletedClasses = emptyList(),
            classpathChanges = emptyList()
        )

        assertEquals(
            listOf(
                DirtyClass(name = "A", reason = DirtyClass.Reason.SourceChange)
            ),
            dirty.toList()
        )
    }

    @Test
    fun `deleted class marks dependent as Dependency`() {
        val data = data(dependents = mapOf("X" to setOf("Y")))
        val dirty = data.computeDirtySet(
            changedClasses = emptyList(),
            deletedClasses = listOf("X"),
            classpathChanges = emptyList()
        )

        assertEquals(
            listOf(
                DirtyClass(name = "Y", reason = dependencyReason("X"))
            ),
            dirty.toList()
        )
    }

    @Test
    fun `classpath change propagates to dependents`() {
        val data = data(dependents = mapOf("LibClass" to setOf("UserClass")))
        val dirty = data.computeDirtySet(
            changedClasses = emptyList(),
            deletedClasses = emptyList(),
            classpathChanges = listOf("LibClass")
        )

        assertEquals(
            listOf(
                DirtyClass(name = "UserClass", reason = dependencyReason("LibClass"))
            ),
            dirty.toList()
        )
    }

    @Test
    fun `dependency is propagated transitively`() {
        val data = data(
            dependents = mapOf(
                "A" to setOf("B"),
                "B" to setOf("C")
            )
        )
        val dirty = data.computeDirtySet(
            changedClasses = listOf("A"),
            deletedClasses = emptyList(),
            classpathChanges = emptyList()
        )

        assertEquals(
            listOf(
                DirtyClass(name = "A", reason = DirtyClass.Reason.SourceChange),
                DirtyClass(name = "B", reason = dependencyReason("A")),
                DirtyClass(name = "C", reason = dependencyReason("B"))
            ),
            dirty.toList()
        )
    }

    @Test
    fun `already dirty class accumulates multiple dependency reasons`() {
        val data = data(
            dependents = mapOf(
                "A" to setOf("C"),
                "B" to setOf("C")
            )
        )
        val dirty = data.computeDirtySet(
            changedClasses = listOf("A", "B"),
            deletedClasses = emptyList(),
            classpathChanges = emptyList()
        )

        assertEquals(
            setOf(
                DirtyClass(name = "A", reason = DirtyClass.Reason.SourceChange),
                DirtyClass(name = "B", reason = DirtyClass.Reason.SourceChange),
                DirtyClass(name = "C", reason = dependencyReason("A", "B"))
            ),
            dirty.toSet()
        )
    }

    @Test
    fun `class with both SourceChange and Dependency keeps SourceChange`() {
        val data = data(dependents = mapOf("A" to setOf("B")))
        val dirty = data.computeDirtySet(
            changedClasses = listOf("B"),
            deletedClasses = listOf("A"),
            classpathChanges = emptyList()
        )

        assertEquals(
            setOf(DirtyClass(name = "B", reason = DirtyClass.Reason.SourceChange)),
            dirty.toSet()
        )
    }
}
