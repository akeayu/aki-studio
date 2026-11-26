package com.akidigitalstudio.aki

import android.graphics.Path

/** Bounded, editable path commands expressed in layer-local normalized coordinates. */
object VectorPathEngine {
    sealed interface Command {
        data class Move(val x: Float, val y: Float) : Command
        data class Line(val x: Float, val y: Float) : Command
        data class Quad(val cx: Float, val cy: Float, val x: Float, val y: Float) : Command
        data class Cubic(val c1x: Float, val c1y: Float, val c2x: Float, val c2y: Float, val x: Float, val y: Float) : Command
        data object Close : Command
    }

    private val tokenPattern = Regex("[MLQCZ]|-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)")
    private val arity = mapOf("M" to 2, "L" to 2, "Q" to 4, "C" to 6, "Z" to 0)

    /** Syntax: M x y, L x y, Q cx cy x y, C c1x c1y c2x c2y x y, Z. */
    fun parse(source: String): List<Command> {
        require(source.length in 1..2048) { "Path data must contain 1–2048 characters" }
        val tokens = tokenPattern.findAll(source).map { it.value }.toList()
        val residue = tokenPattern.replace(source, "").replace(",", "").replace(Regex("\\s+"), "")
        require(residue.isEmpty()) { "Path contains unsupported characters" }
        require(tokens.isNotEmpty() && tokens.size <= 512) { "Path command count is invalid" }
        val result = mutableListOf<Command>()
        var index = 0
        var active = ""
        while (index < tokens.size) {
            if (tokens[index] in arity.keys) active = tokens[index++]
            require(active.isNotEmpty()) { "Path must begin with a command" }
            val count = arity.getValue(active)
            if (active == "Z") {
                result += Command.Close
                active = ""
                continue
            }
            require(index + count <= tokens.size && tokens.subList(index, index + count).none { it in arity.keys }) {
                "Path command has incomplete coordinates"
            }
            val values = tokens.subList(index, index + count).map { it.toFloatOrNull() ?: throw IllegalArgumentException("Invalid path coordinate") }
            require(values.all { it.isFinite() && it in 0f..1f }) { "Path coordinates must be between 0 and 1" }
            result += when (active) {
                "M" -> Command.Move(values[0], values[1])
                "L" -> Command.Line(values[0], values[1])
                "Q" -> Command.Quad(values[0], values[1], values[2], values[3])
                "C" -> Command.Cubic(values[0], values[1], values[2], values[3], values[4], values[5])
                else -> throw IllegalArgumentException("Unsupported path command")
            }
            index += count
            if (active == "M") active = "L"
        }
        require(result.isNotEmpty() && result.first() is Command.Move) { "Path must start with M" }
        require(result.size <= 128) { "Path contains too many commands" }
        return result
    }

    fun toAndroidPath(source: String, bounds: android.graphics.RectF): Path {
        val path = Path()
        parse(source).forEach { command ->
            when (command) {
                is Command.Move -> path.moveTo(bounds.left + command.x * bounds.width(), bounds.top + command.y * bounds.height())
                is Command.Line -> path.lineTo(bounds.left + command.x * bounds.width(), bounds.top + command.y * bounds.height())
                is Command.Quad -> path.quadTo(
                    bounds.left + command.cx * bounds.width(), bounds.top + command.cy * bounds.height(),
                    bounds.left + command.x * bounds.width(), bounds.top + command.y * bounds.height()
                )
                is Command.Cubic -> path.cubicTo(
                    bounds.left + command.c1x * bounds.width(), bounds.top + command.c1y * bounds.height(),
                    bounds.left + command.c2x * bounds.width(), bounds.top + command.c2y * bounds.height(),
                    bounds.left + command.x * bounds.width(), bounds.top + command.y * bounds.height()
                )
                Command.Close -> path.close()
            }
        }
        return path
    }
}
