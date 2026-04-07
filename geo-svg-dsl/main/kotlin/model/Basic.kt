package ru.itmo.ct.geosvg.model

interface Drawable

data class Point(
    val x: Double,
    val y: Double,
)

interface Contourable {
    var stroke: Stroke?
}

interface Fillable : Contourable {
    var fill: Fill?
}
