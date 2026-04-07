package ru.itmo.ct.geosvg.model

@JvmInline
value class Color(
    val color: String,
) {
    constructor(hexColor: Int) : this(String.format("#%06x", hexColor))
    constructor(red: Int, green: Int, blue: Int) : this((red shl 16) or (green shl 8) or blue)
}

object Colors {
    val WHITE = Color(255, 255, 255)
    val LIGHT_GRAY = Color(192, 192, 192)
    val GRAY = Color(128, 128, 128)
    val DARK_GRAY = Color(64, 64, 64)
    val BLACK = Color(0, 0, 0)
    val RED = Color(255, 0, 0)
    val PINK = Color(255, 175, 175)
    val ORANGE = Color(255, 200, 0)
    val YELLOW = Color(255, 255, 0)
    val GREEN = Color(0, 255, 0)
    val MAGENTA = Color(255, 0, 255)
    val CYAN = Color(0, 255, 255)
    val BLUE = Color(0, 0, 255)
}
