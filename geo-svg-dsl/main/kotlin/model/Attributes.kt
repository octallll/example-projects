package ru.itmo.ct.geosvg.model

data class Stroke(
    val color: Color? = null,
    val width: Double? = null,
    val opacity: Double? = null,
    val arrowStart: ArrowMarker = ArrowMarker.None,
    val arrowEnd: ArrowMarker = ArrowMarker.None,
    val dashed: Boolean = false,
)

val NONE_STROKE = Stroke(color = Color("none"))

data class Fill(
    val color: Color? = null,
    val opacity: Double? = null,
)

val NONE_FILL = Fill(color = Color("none"))

enum class Alignment {
    START,
    END,
    MIDDLE,
}
