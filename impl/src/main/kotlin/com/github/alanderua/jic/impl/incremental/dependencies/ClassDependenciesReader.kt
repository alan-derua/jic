package com.github.alanderua.jic.impl.incremental.dependencies

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

internal object ClassDependenciesReader {

    fun readDependencies(classContent: ByteArray): ClassDependencies {
        val reader = ClassReader(classContent)

        return ClassDependencies(
            className = reader.className.toDotName(),
            dependencies = collectDependencies(reader)
        )
    }

    private fun collectDependencies(reader: ClassReader): Set<String> {
        val recordingRemapper = RecordingRemapper(reader.className)
        val stubVisitor = StubVisitor()
        val remapperVisitor = ClassRemapper(stubVisitor, recordingRemapper)

        reader.accept(remapperVisitor, 0)

        return recordingRemapper.getRecordedTypes()
    }
}

private class StubVisitor : ClassVisitor(Opcodes.ASM9) {
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        object : AnnotationVisitor(api) {}

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor =
        object : FieldVisitor(api) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
                object : AnnotationVisitor(api) {}
        }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor =
        object : MethodVisitor(api) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
                object : AnnotationVisitor(api) {}
        }
}

private class RecordingRemapper(private val ownClassName: String) : Remapper() {

    private val types = hashSetOf<String>()

    fun getRecordedTypes(): Set<String> {
        return types
    }

    private fun isExternalType(type: String): Boolean {
        return type != ownClassName
                && !type.startsWith("java/")
    }

    private fun addTypeName(typeName: String) {
        if (isExternalType(typeName)) {
            types.add(typeName.toDotName())
        }
    }

    override fun map(internalName: String): String {
        addTypeName(internalName)
        return internalName
    }
}

private fun String.toDotName() = replace("/", ".")