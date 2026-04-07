package ru.itmo.ct.geosvg.dsl

import ru.itmo.ct.geosvg.model.*

@GeoSvgDsl
interface ContourableShape<T : Contourable> {
    var stroke: Stroke?

    fun stroke(
        color: Color? = null,
        width: Double? = null,
        opacity: Double? = null,
        dashed: Boolean = false,
    ): T {
        this.stroke = Stroke(color, width, opacity, ArrowMarker.None, ArrowMarker.None, dashed)
        return build()
    }

    fun build(): T
}

@GeoSvgDsl
interface FillableShape<T : Fillable> {
    var fill: Fill?

    fun fill(
        color: Color,
        opacity: Double? = null,
    ): T {
        this.fill = this.fill?.copy(color = color, opacity = opacity) ?: Fill(color, opacity)
        return build()
    }

    fun build(): T
}

@GeoSvgDsl
class CircleBuilder(
    private val center: Point,
    private val radius: Double,
) : ContourableShape<Circle>,
    FillableShape<Circle> {
    override var stroke: Stroke? = null
    override var fill: Fill? = null

    override fun build(): Circle {
        val circle = Circle(center, radius)
        circle.stroke = stroke
        circle.fill = fill

        return circle
    }
}

@GeoSvgDsl
class RectangleBuilder(
    private val center: Point,
    private val width: Double,
    private val height: Double,
) : ContourableShape<Rectangle>,
    FillableShape<Rectangle> {
    override var stroke: Stroke? = null
    override var fill: Fill? = null

    override fun build(): Rectangle {
        val rectangle = Rectangle(center, width, height)
        rectangle.stroke = stroke
        rectangle.fill = fill

        return rectangle
    }
}

@GeoSvgDsl
class PolygonBuilder(
    private val points: List<Point>,
) : ContourableShape<Polygon>,
    FillableShape<Polygon> {
    override var stroke: Stroke? = null
    override var fill: Fill? = null

    override fun build(): Polygon {
        val polygon = Polygon(points)
        polygon.stroke = stroke
        polygon.fill = fill

        return polygon
    }
}

@GeoSvgDsl
class SegmentBuilder(
    private val p1: Point,
    private val p2: Point,
) : ContourableShape<Segment> {
    override var stroke: Stroke? = null

    override fun build(): Segment {
        val segment = Segment(p1, p2)
        segment.stroke = stroke

        return segment
    }
}

@GeoSvgDsl
class PolylineBuilder(
    private val startPoints: List<Point> = listOf<Point>(),
) : ContourableShape<Polyline>,
    FillableShape<Polyline> {
    private val points: MutableList<Point> = mutableListOf()
    override var stroke: Stroke? = null
    override var fill: Fill? = null

    fun point(point: Point) {
        points.add(point)
    }

    fun point(
        x: Number,
        y: Number,
    ) {
        points.add(Point(x.toDouble(), y.toDouble()))
    }

    fun points(points: Iterable<Point>) {
        this.points.addAll(points)
    }

    override fun build(): Polyline {
        val polyline = Polyline(startPoints + points.toList())
        polyline.stroke = stroke
        polyline.fill = fill
        return polyline
    }
}

@GeoSvgDsl
class TextBuilder(
    private val text: String,
    private val point: Point,
    private val font: String = "Arial",
    private val size: Double = 16.0,
    private val boldArg: Boolean = false,
) {
    var fontSize: Double = 16.0
    var fontBold: Boolean = false
    var textAnchor: Alignment = Alignment.START
    var dominantBaseline: Alignment = Alignment.START
    var fill: Fill? = Fill(Colors.BLACK)

    fun bold(): TextBuilder {
        this.fontBold = true
        return this
    }

    fun anchor(a: Alignment): TextBuilder {
        this.textAnchor = a
        return this
    }

    fun baseline(a: Alignment): TextBuilder {
        this.dominantBaseline = a
        return this
    }

    fun centered(vertical: Boolean = false): TextBuilder {
        this.textAnchor = Alignment.MIDDLE

        if (vertical) {
            this.dominantBaseline = Alignment.MIDDLE
        }

        return this
    }

    fun centeredVertical(): TextBuilder {
        this.dominantBaseline = Alignment.MIDDLE
        return this
    }

    fun fill(
        color: Color,
        opacity: Double = 1.0,
    ): TextBuilder {
        this.fill = Fill(color, opacity)
        return this
    }

    infix fun fill(color: Color): TextBuilder {
        this.fill = Fill(color)
        return this
    }

    infix fun size(fontSize: Double): TextBuilder {
        this.fontSize = fontSize
        return this
    }

    val bold: TextBuilder
        get() {
            this.fontBold = true
            return this
        }

    val alignLeft: TextBuilder
        get() {
            this.textAnchor = Alignment.START
            return this
        }

    val alignCenter: TextBuilder
        get() {
            this.textAnchor = Alignment.MIDDLE
            return this
        }

    val alignRight: TextBuilder
        get() {
            this.textAnchor = Alignment.END
            return this
        }

    val alignTop: TextBuilder
        get() {
            this.dominantBaseline = Alignment.START
            return this
        }

    val alignMiddle: TextBuilder
        get() {
            this.dominantBaseline = Alignment.MIDDLE
            return this
        }

    val alignBottom: TextBuilder
        get() {
            this.dominantBaseline = Alignment.END
            return this
        }

    fun build(): Text {
        val text = Text(text, point, font, size, boldArg)

        text.fontSize = fontSize
        text.fontBold = fontBold
        text.textAnchor = textAnchor
        text.dominantBaseline = dominantBaseline
        text.fill = fill

        return text
    }
}
