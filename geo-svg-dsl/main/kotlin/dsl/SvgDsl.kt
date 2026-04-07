package ru.itmo.ct.geosvg.dsl

import ru.itmo.ct.geosvg.model.*

@DslMarker
annotation class GeoSvgDsl

@GeoSvgDsl
abstract class AbstractBuilder {
    protected val elements: MutableList<Drawable> = mutableListOf()

    @GeoSvgDsl
    fun circle(
        center: Point,
        radius: Double,
        builder: CircleBuilder.() -> Unit = {},
    ): Circle {
        val circleBuilder = CircleBuilder(center, radius)
        circleBuilder.builder()

        val circle = circleBuilder.build()
        elements.add(circle)
        return circle
    }

    @GeoSvgDsl
    fun rectangle(
        center: Point,
        width: Double,
        height: Double,
        builder: RectangleBuilder.() -> Unit = {},
    ): Rectangle {
        val rectangleBuilder = RectangleBuilder(center, width, height)
        rectangleBuilder.builder()

        val rectangle = rectangleBuilder.build()
        elements.add(rectangle)
        return rectangle
    }

    @GeoSvgDsl
    fun polygon(
        point1: Point,
        point2: Point,
        point3: Point,
        vararg points: Point,
        builder: PolygonBuilder.() -> Unit = {},
    ): Polygon {
        val polygonBuilder = PolygonBuilder(listOf(point1, point2, point3) + points)
        polygonBuilder.builder()

        val polygon = polygonBuilder.build()
        elements.add(polygon)
        return polygon
    }

    @GeoSvgDsl
    fun line(
        p1: Point,
        p2: Point,
        builder: SegmentBuilder.() -> Unit = {},
    ): Segment {
        val segmentBuilder = SegmentBuilder(p1, p2)
        segmentBuilder.builder()

        val segment = segmentBuilder.build()
        elements.add(segment)
        return segment
    }

    @GeoSvgDsl
    fun polyline(
        point1: Point,
        point2: Point,
        vararg points: Point,
        builder: PolylineBuilder.() -> Unit = {},
    ): Polyline {
        val polylineBuilder = PolylineBuilder(listOf(point1, point2) + points)
        polylineBuilder.builder()

        val polyline = polylineBuilder.build()
        elements.add(polyline)
        return polyline
    }

    @GeoSvgDsl
    fun polyline(block: PolylineBuilder.() -> Unit): Polyline {
        val polylineBuilder = PolylineBuilder()
        polylineBuilder.block()

        val polyline = polylineBuilder.build()
        elements.add(polyline)
        return polyline
    }

    @GeoSvgDsl
    fun text(
        text: String,
        point: Point,
        font: String = "Arial",
        size: Double = 16.0,
        bold: Boolean = false,
        builder: TextBuilder.() -> Unit = {},
    ): Text {
        val textBuilder = TextBuilder(text, point, font, size, bold)
        textBuilder.builder()

        val text = textBuilder.build()
        elements.add(text)
        return text
    }

    @GeoSvgDsl
    fun group(
        offset: Point = Point(0.0, 0.0),
        builder: GroupBuilder.() -> Unit,
    ): Group {
        val groupBuilder = GroupBuilder(offset)
        groupBuilder.builder()
        val group = groupBuilder.build()
        elements.add(group)
        return group
    }
}

@GeoSvgDsl
class SceneBuilder(
    private val width: Int,
    private val height: Int,
) : AbstractBuilder() {
    val center: Point
        get() = Point(width / 2.0, height / 2.0)

    fun build(): Scene {
        return Scene(elements.toList(), width, height)
    }
}

@GeoSvgDsl
class GroupBuilder(
    private val offset: Point,
) : AbstractBuilder() {
    fun build(): Group {
        return Group(elements, offset)
    }
}

@GeoSvgDsl
fun scene(
    width: Int = 800,
    height: Int = 600,
    builder: SceneBuilder.() -> Unit,
): Scene {
    val sceneBuilder = SceneBuilder(width, height)
    sceneBuilder.builder()
    return sceneBuilder.build()
}
