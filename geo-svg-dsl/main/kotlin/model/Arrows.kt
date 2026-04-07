package ru.itmo.ct.geosvg.model

enum class ArrowShape {
    NONE,
    CIRCLE,
    RECTANGLE,
    TRIANGLE,
    STEALTH,
    DIAMOND,
}

data class ArrowMarker(
    val shape: ArrowShape,
    val size: Double,
    val fill: Boolean = false,
    val reversed: Boolean = false,
) {
    companion object {
        val None = ArrowMarker(ArrowShape.NONE, 0.0)

        fun triangle(
            size: Double = 4.0,
            fill: Boolean = true,
            reversed: Boolean = false,
        ) = ArrowMarker(ArrowShape.TRIANGLE, size, fill, reversed)

        fun circle(
            size: Double = 4.0,
            fill: Boolean = false,
            reversed: Boolean = false,
        ) = ArrowMarker(ArrowShape.CIRCLE, size, fill, reversed)

        fun rectangle(
            size: Double = 2.0,
            fill: Boolean = false,
            reversed: Boolean = false,
        ) = ArrowMarker(ArrowShape.RECTANGLE, size, fill, reversed)

        fun stealth(
            size: Double = 4.0,
            fill: Boolean = true,
            reversed: Boolean = false,
        ) = ArrowMarker(ArrowShape.STEALTH, size, fill, reversed)

        fun diamond(
            size: Double = 6.0,
            fill: Boolean = true,
            reversed: Boolean = false,
        ) = ArrowMarker(ArrowShape.DIAMOND, size, fill, reversed)
    }
}
