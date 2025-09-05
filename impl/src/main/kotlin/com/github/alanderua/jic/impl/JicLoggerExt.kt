package com.github.alanderua.jic.impl

import com.github.alanderua.jic.api.JicLogger
import java.io.Writer
import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject

internal fun JicLogger.toWriter() = object : Writer() {
    val sb = StringBuilder()

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        sb.append(cbuf, off, len)
    }

    override fun flush() {
        if (sb.isNotEmpty()) {
            i(sb.toString())
            sb.setLength(0)
        }
    }

    override fun close() {}
}

internal fun JicLogger.toDiagnosticsListener() = object : DiagnosticListener<JavaFileObject> {
    override fun report(d: Diagnostic<out JavaFileObject?>?) {
        if (d == null) return

        val msg = formatDiagnostic(d)

        when (d.kind) {
            Diagnostic.Kind.ERROR -> e(msg)
            Diagnostic.Kind.WARNING,
            Diagnostic.Kind.MANDATORY_WARNING -> w(msg)
            Diagnostic.Kind.NOTE,
            Diagnostic.Kind.OTHER -> i(msg)
        }
    }
}

private fun formatDiagnostic(d: Diagnostic<out JavaFileObject>): String {
    val kind = d.kind.toString().lowercase()
    val file = d.source?.name ?: "<unknown>"
    val line = if (d.lineNumber != Diagnostic.NOPOS) d.lineNumber.toString() else "?"
    val col  = if (d.columnNumber != Diagnostic.NOPOS) d.columnNumber.toString() else "?"
    val msg  = d.getMessage(null).trim()

    return "$file:$line:$col: $kind: $msg"
}