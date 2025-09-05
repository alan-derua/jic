package com.github.alanderua.jic.api

public interface JicLogger {
    public fun e(msg: String, throwable: Throwable? = null)
    public fun w(msg: String)
    public fun i(msg: String)
    public fun d(msg: String)
}