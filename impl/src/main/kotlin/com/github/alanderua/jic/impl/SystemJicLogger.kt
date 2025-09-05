package com.github.alanderua.jic.impl

import com.github.alanderua.jic.api.JicLogger

internal object SystemJicLogger : JicLogger {
    override fun e(msg: String, throwable: Throwable?) {
        System.err.println(msg)
        throwable?.printStackTrace()
    }

    override fun w(msg: String) {
        System.err.println(msg)
    }

    override fun i(msg: String) {
        println(msg)
    }

    override fun d(msg: String) {
        println(msg)
    }
}