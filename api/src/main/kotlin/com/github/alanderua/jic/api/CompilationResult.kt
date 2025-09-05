package com.github.alanderua.jic.api

public sealed class CompilationResult {
    public data object Success : CompilationResult()
    public data object Error : CompilationResult()
}