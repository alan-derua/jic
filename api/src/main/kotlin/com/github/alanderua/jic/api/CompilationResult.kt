package com.github.alanderua.jic.api

/**
 * Compilation result
 */
public sealed class CompilationResult {
    /**
     * Successful compilation
     */
    public data object Success : CompilationResult()

    /**
     * Compilation was not successful
     */
    public data object Error : CompilationResult()
}