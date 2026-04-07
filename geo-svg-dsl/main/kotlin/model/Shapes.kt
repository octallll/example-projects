package ru.itmo.ct.geosvg.model

data class Segment(
    val p1: Point,
    val p2: Point,
    override var stroke: Stroke? = null,
    var deviation: Double = 0.0,
) : Drawable,
    Contourable

data class Polyline(
    val points: List<Point>,
    override var stroke: Stroke? = null,
    override var fill: Fill? = null,
) : Drawable,
    Contourable,
    Fillable

data class Circle(
    val center: Point,
    val r: Double,
    override var stroke: Stroke? = null,
    override var fill: Fill? = null,
) : Drawable,
    Fillable

data class Rectangle(
    val center: Point,
    val width: Double,
    val height: Double,
    override var stroke: Stroke? = null,
    override var fill: Fill? = null,
) : Drawable,
    Fillable

data class Polygon(
    val points: List<Point>,
    override var stroke: Stroke? = null,
    override var fill: Fill? = null,
) : Drawable,
    Fillable

data class Text(
    val text: String,
    val point: Point,
    val fontFamily: String = "Arial",
    var fontSize: Double = 16.0,
    var fontBold: Boolean = false,
    var textAnchor: Alignment = Alignment.START,
    var dominantBaseline: Alignment = Alignment.START,
    var fill: Fill? = Fill(Colors.BLACK),
) : Drawable

data class Group(
    val elements: List<Drawable>,
    val offset: Point = Point(0.0, 0.0),
) : Drawable

class Scene(
    val elements: List<Drawable>,
    val width: Int = 1024,
    val height: Int = 720,
)
