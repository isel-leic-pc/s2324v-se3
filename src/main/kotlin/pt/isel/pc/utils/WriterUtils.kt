package pt.isel.pc.utils

import java.io.Writer

fun Writer.sendLine(line: String) {
    appendLine(line)
    flush()
}
