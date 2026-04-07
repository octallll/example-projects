package ru.itmo.ct.geosvg.render

import kotlin.math.sqrt
import ru.itmo.ct.geosvg.model.*
import ru.itmo.ct.geosvg.model.Colors.BLACK

class SvgRenderer {
    private val definedMarkers = mutableSetOf<ArrowMarker>()

    fun render(scene: Scene): String {
        val svg =
            xmlTag("svg") {
                attr("xmlns", "http://www.w3.org/2000/svg")
                attr("width", scene.width)
                attr("height", scene.height)
                attr("viewBox", "0 0 ${scene.width} ${scene.height}")

                // Добавляем все элементы сцены
                scene.elements.forEach { drawable ->
                    tag(renderDrawable(drawable))
                }

                createMarkersDefinition()?.let { tag(it) }
            }

        val renderer = XmlRenderer()
        return renderer.render(svg)
    }

    private fun createMarkersDefinition(): XmlTag? {
        if (definedMarkers.isEmpty()) return null

        return xmlTag("defs") {
            definedMarkers.forEach { marker ->
                tag(createMarker(marker))
            }
        }
    }

    private fun renderDrawable(drawable: Drawable): XmlTag =
        when (drawable) {
            is Segment -> renderSegment(drawable)
            is Circle -> renderCircle(drawable)
            is Rectangle -> renderRectangle(drawable)
            is Polygon -> renderPolygon(drawable)
            is Polyline -> renderCurve(drawable)
            is Text -> renderText(drawable)
            is Group -> renderGroup(drawable)
            else -> throw IllegalArgumentException("Unsupported drawable type ${drawable.javaClass.canonicalName}")
        }

    private fun renderGroup(group: Group): XmlTag =
        xmlTag("g") {
            attr("transform", "translate(${group.offset.t})")
            group.elements.forEach { element -> tag(renderDrawable(element)) }
        }

    private fun renderText(text: Text): XmlTag =
        xmlTag("text") {
            attr("x", text.point.x)
            attr("y", text.point.y)
            attr(
                "text-anchor",
                when (text.textAnchor) {
                    Alignment.START -> "start"
                    Alignment.MIDDLE -> "middle"
                    Alignment.END -> "end"
                },
            )
            attr(
                "dominant-baseline",
                when (text.dominantBaseline) {
                    Alignment.START -> "start"
                    Alignment.MIDDLE -> "middle"
                    Alignment.END -> "end"
                },
            )
            attr("font-family", text.fontFamily)
            attr("font-size", text.fontSize)
            if (text.fontBold) {
                attr("font-weight", "bold")
            }
            text(text.text)
            addFill(text.fill)
        }

    private fun renderSegment(segment: Segment): XmlTag =
        xmlTag("path") {
            val dx = segment.p2.x - segment.p1.x
            val dy = segment.p2.y - segment.p1.y
            val length = sqrt(dx * dx + dy * dy)

            val unitDx = dx / length
            val unitDy = dy / length

            var p1 = segment.p1
            var p2 = segment.p2

            segment.stroke?.let { stroke ->
                val w = stroke.width ?: 2.0
                stroke.arrowStart.let { marker ->
                    val offset = w * marker.size * (1.5 - getMarkerOffset(marker))
                    p1 = segment.p1.shiftTo(unitDx * offset, unitDy * offset)
                }
                stroke.arrowEnd.let { marker ->
                    val offset = w * marker.size * (1.5 - getMarkerOffset(marker))
                    p2 = segment.p2.shiftTo(-unitDx * offset, -unitDy * offset)
                }
            }

            val midX = (p1.x + p2.x) / 2
            val midY = (p1.y + p2.y) / 2

            val qPoint =
                if (segment.deviation != 0.0 && length > 0) {
                    val controlX = midX + (-dy / length) * segment.deviation
                    val controlY = midY + (dx / length) * segment.deviation
                    "Q $controlX,$controlY "
                } else if (segment.deviation != 0.0) {
                    "Q $midX,${midY + segment.deviation}"
                } else {
                    ""
                }
            attr("d", "M ${p1.t} $qPoint${p2.t}")
            attr("fill", "none")
            addStroke(segment.stroke)
        }

    private fun renderCircle(circle: Circle): XmlTag =
        xmlTag("circle") {
            attr("cx", circle.center.x)
            attr("cy", circle.center.y)
            attr("r", circle.r)
            addStroke(circle.stroke)
            addFill(circle.fill)
        }

    private fun renderRectangle(rect: Rectangle): XmlTag =
        xmlTag("rect") {
            attr("x", rect.center.x - rect.width / 2)
            attr("y", rect.center.y - rect.height / 2)
            attr("width", rect.width)
            attr("height", rect.height)
            addFill(rect.fill)
            addStroke(rect.stroke)
        }

    private fun renderPolygon(rect: Polygon): XmlTag =
        xmlTag("polygon") {
            attr("points", rect.points.joinToString(" ") { "${it.x},${it.y}" })
            addFill(rect.fill)
            addStroke(rect.stroke)
        }

    private fun renderCurve(polyline: Polyline): XmlTag {
        if (polyline.points.size < 2) {
            return xmlTag("g")
        }

        val pathData =
            buildString {
                append("M ${polyline.points.first().x} ${polyline.points.first().y}")
                for (i in 1 until polyline.points.size) {
                    append(" L ${polyline.points[i].x} ${polyline.points[i].y}")
                }
            }

        return xmlTag("path") {
            attr("d", pathData)
            addFill(polyline.fill)
            addStroke(polyline.stroke)
        }
    }

    private fun XmlTag.addStroke(stroke: Stroke?) {
        attr("stroke", (stroke?.color ?: BLACK).color)
        val strokeWidth = stroke?.width ?: 2.0
        attr("stroke-width", strokeWidth)
        attr("stroke-opacity", stroke?.opacity ?: 1.0)
        stroke?.arrowStart?.let { arrow ->
            if (arrow.shape != ArrowShape.NONE) {
                attr("marker-start", getMarkerUrl(arrow))
            }
        }
        stroke?.arrowEnd?.let { arrow ->
            if (arrow.shape != ArrowShape.NONE) {
                attr("marker-end", getMarkerUrl(arrow))
            }
        }
        if (stroke?.dashed == true) {
            attr("stroke-dasharray", 2 * strokeWidth)
        }
    }

    private fun XmlTag.addFill(fill: Fill?) {
        attr("fill", (fill?.color?.color ?: "none"))
        attr("fill-opacity", fill?.opacity ?: 1.0)
    }

    private fun getMarkerUrl(arrow: ArrowMarker): String {
        definedMarkers.add(arrow)
        return "url(#marker-${arrow.javaClass.simpleName}${arrow.hashCode()})"
    }

    private fun getMarkerOffset(marker: ArrowMarker): Double =
        when (marker.shape) {
            ArrowShape.CIRCLE, ArrowShape.RECTANGLE, ArrowShape.DIAMOND -> 0.5
            ArrowShape.TRIANGLE -> if (marker.fill) 0.5 else 1.5
            ArrowShape.STEALTH -> if (marker.fill) 0.75 else 0.75
            else -> 0.0
        }

    private fun createMarker(marker: ArrowMarker): XmlTag =
        xmlTag("marker") {
            attr("id", "marker-${marker.javaClass.simpleName}${marker.hashCode()}")
            attr("markerWidth", marker.size * 2)
            attr("markerHeight", marker.size * 2)
            attr("orient", if (marker.reversed) "auto-start-reverse" else "auto")
            attr("stroke", "context-stroke")
            attr("stroke-width", "1")
            attr("stroke-linecap", "round")
            if (marker.fill) {
                attr("fill", "context-stroke")
            } else {
                attr("fill", "none")
            }
            attr("refX", marker.size * getMarkerOffset(marker))
            attr("refY", marker.size)
            val s = marker.size
            val inner =
                when (marker.shape) {
                    ArrowShape.CIRCLE ->
                        xmlTag("circle") {
                            attr("cx", marker.size)
                            attr("cy", marker.size)
                            attr("r", marker.size / 2)
                        }

                    ArrowShape.RECTANGLE ->
                        xmlTag("rect") {
                            attr("x", 0.25 * marker.size)
                            attr("y", 0.25 * marker.size)
                            attr("width", marker.size * 1.5)
                            attr("height", marker.size * 1.5)
                        }

                    ArrowShape.TRIANGLE ->
                        xmlTag("path") {
                            val q = if (marker.fill) " Z" else ""
                            attr(
                                "d",
                                "M ${s / 2},${s / 2} L ${3 * s / 2},$s L ${s / 2},${s * 3 / 2}$q",
                            )
                        }

                    ArrowShape.STEALTH ->
                        xmlTag("path") {
                            attr(
                                "d",
                                "M ${s / 2},${s / 2} L ${s + s / 2},${s / 2 + s / 2} L ${s / 2},${s / 2 + s} L ${s / 2 + s / 4},$s Z",
                            )
                        }

                    ArrowShape.DIAMOND ->
                        xmlTag("polygon") {
                            attr(
                                "points",
                                "${0.5 * s} $s, $s ${0.7 * s}, ${1.5 * s} $s, $s ${1.3 * s}",
                            )
                        }

                    else -> null
                }
            inner?.let { tag(inner) }
        }
}

private fun Point.shiftTo(
    dx: Double,
    dy: Double,
) = Point(x + dx, y + dy)

private val Point.t: String
    get() = "$x,$y"
