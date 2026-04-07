package ru.itmo.ct.geosvg.model

import kotlin.math.abs
import kotlin.math.sqrt

fun intersect(
    from: Point,
    to: Point,
    shape: Contourable,
): Point {
    return when (shape) {
        is Circle -> intersectWithCircle(from, to, shape)
        is Rectangle -> intersectWithRectangle(from, to, shape)
        is Segment -> intersectWithSegment(from, to, shape)
        is Polyline -> intersectWithPolyline(from, to, shape.points)
        is Polygon -> intersectWithPolyline(from, to, shape.points)
        else -> to
    }
}

private fun intersectWithCircle(
    from: Point,
    to: Point,
    circle: Circle,
): Point {
    val dx = to.x - from.x
    val dy = to.y - from.y

    // Параметры уравнения прямой: from + t*(to - from)
    val a = dx * dx + dy * dy
    val b = 2 * (dx * (from.x - circle.center.x) + dy * (from.y - circle.center.y))
    val c =
        (from.x - circle.center.x) * (from.x - circle.center.x) +
            (from.y - circle.center.y) * (from.y - circle.center.y) -
            circle.r * circle.r

    val discriminant = b * b - 4 * a * c

    if (discriminant < 0) {
        // Нет пересечения
        return to
    }

    // Находим параметр t для точки пересечения (ближайшей к from)
    val t1 = (-b - sqrt(discriminant)) / (2 * a)
    val t2 = (-b + sqrt(discriminant)) / (2 * a)

    // Выбираем t в диапазоне [0,1] (между from и to)
    val t =
        when {
            t1 in 0.0..1.0 -> t1
            t2 in 0.0..1.0 -> t2
            else -> return to
        }

    return Point(from.x + t * dx, from.y + t * dy)
}

private fun intersectWithRectangle(
    from: Point,
    to: Point,
    rect: Rectangle,
): Point {
    val left = rect.center.x - rect.width / 2
    val right = rect.center.x + rect.width / 2
    val top = rect.center.y - rect.height / 2
    val bottom = rect.center.y + rect.height / 2

    // Проверяем пересечение с каждой стороной прямоугольника
    val intersections =
        listOfNotNull(
            intersectWithVerticalLine(from, to, left, top, bottom),
            intersectWithVerticalLine(from, to, right, top, bottom),
            intersectWithHorizontalLine(from, to, top, left, right),
            intersectWithHorizontalLine(from, to, bottom, left, right),
        )

    // Выбираем ближайшую точку пересечения к from
    return intersections.minByOrNull { it.distanceTo(from) } ?: to
}

private fun intersectWithSegment(
    from: Point,
    to: Point,
    segment: Segment,
): Point {
    val (x1, y1) = from
    val (x2, y2) = to
    val (x3, y3) = segment.p1
    val (x4, y4) = segment.p2

    val denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)

    if (abs(denominator) < 1e-10) {
        return to
    }

    val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denominator
    val u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denominator

    if (t in 0.0..1.0 && u in 0.0..1.0) {
        return Point(x1 + t * (x2 - x1), y1 + t * (y2 - y1))
    }

    return to
}

private fun intersectWithPolyline(
    from: Point,
    to: Point,
    points: List<Point>,
): Point {
    // Проверяем пересечение с каждым сегментом полилинии
    val intersections =
        points.windowed(2).mapNotNull { segmentPoints ->
            val segment = Segment(segmentPoints[0], segmentPoints[1])
            val intersection = intersectWithSegment(from, to, segment)
            if (intersection != to) intersection else null
        }

    // Выбираем ближайшую точку пересечения к from
    return intersections.minByOrNull { it.distanceTo(from) } ?: to
}

private fun intersectWithVerticalLine(
    from: Point,
    to: Point,
    x: Double,
    yMin: Double,
    yMax: Double,
): Point? {
    if (from.x == to.x) return null // вертикальная линия

    val t = (x - from.x) / (to.x - from.x)
    if (t !in 0.0..1.0) return null

    val y = from.y + t * (to.y - from.y)
    if (y in yMin..yMax) {
        return Point(x, y)
    }
    return null
}

private fun intersectWithHorizontalLine(
    from: Point,
    to: Point,
    y: Double,
    xMin: Double,
    xMax: Double,
): Point? {
    if (from.y == to.y) return null // горизонтальная линия

    val t = (y - from.y) / (to.y - from.y)
    if (t !in 0.0..1.0) return null

    val x = from.x + t * (to.x - from.x)
    if (x in xMin..xMax) {
        return Point(x, y)
    }
    return null
}

private fun Point.distanceTo(other: Point): Double {
    return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
}
