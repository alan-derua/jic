package com.github.alanderua.jic.impl.incremental.dependencies

internal data class ClassDependencies(
    val className: String,
    val dependencies: Set<String>
)