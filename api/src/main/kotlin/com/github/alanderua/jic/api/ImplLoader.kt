package com.github.alanderua.jic.api

import java.util.ServiceLoader
import kotlin.reflect.KClass

internal fun <T : Any> loadImplementation(cls: KClass<T>, classLoader: ClassLoader): T {
    val implementations = ServiceLoader.load(cls.java, classLoader)
    implementations.firstOrNull() ?: error("The classpath contains no implementation for ${cls.qualifiedName}")
    return implementations.singleOrNull()
        ?: error("The classpath contains more than one implementation for ${cls.qualifiedName}")
}