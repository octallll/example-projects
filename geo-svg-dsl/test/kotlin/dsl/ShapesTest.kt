package ru.itmo.ct.geosvg.dsl

import kotlin.test.*
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestMethodOrder
import ru.itmo.ct.geosvg.model.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ShapesTest {
    @Test
    @Order(101)
    fun `scene basic shapes`() {
        val s =
            scene(width = 600, height = 400) {
                circle(p(100, 100), 50.0)
                rectangle(200 at 100, 100.0, 50.0)
                polygon(0 at 0, 10 at 10, 0 at 20)
                line(10 at 10, 20 at 20)
                polyline(100 at 0, 100 at 10, 90 at 10)
            }

        assertEquals(600, s.width)
        assertEquals(400, s.height)
        val elements = s.elements
        assertEquals(5, elements.size)

        elements[0].let { shape ->
            assertIs<Circle>(shape)
            assertEquals(Point(100.0, 100.0), shape.center)
            assertEquals(50.0, shape.r)
        }

        elements[1].let { shape ->
            assertIs<Rectangle>(shape)
            assertEquals(Point(200.0, 100.0), shape.center)
            assertEquals(100.0, shape.width)
            assertEquals(50.0, shape.height)
        }

        elements[2].let { shape ->
            assertIs<Polygon>(shape)
            assertEquals(
                listOf(
                    Point(0.0, 0.0),
                    Point(10.0, 10.0),
                    Point(0.0, 20.0),
                ),
                shape.points,
            )
        }

        elements[3].let { shape ->
            assertIs<Segment>(shape)
            assertEquals(Point(10.0, 10.0), shape.p1)
            assertEquals(Point(20.0, 20.0), shape.p2)
        }
        elements[4].let { shape ->
            assertIs<Polyline>(shape)
            assertEquals(
                listOf(
                    Point(100.0, 0.0),
                    Point(100.0, 10.0),
                    Point(90.0, 10.0),
                ),
                shape.points,
            )
        }
    }

    @Test
    @Order(102)
    fun `fill and stroke variants`() {
        val s =
            scene {
                circle(p(10, 10), 5.0)
                    .fill(Color("#ff0000"))

                rectangle(p(20, 20), 10.0, 5.0)
                    .stroke(Color("#00ff00"))

                polygon(0 at 0, 10 at 0, 5 at 10)
                    .fill(Color("#aaaaaa"), opacity = 0.4)

                line(0 at 0, 10 at 10)
                    .stroke(Color("#0000ff"), width = 3.0)

                circle(p(50, 50), 10.0)
                    .fill(Color("#123456"), 0.7)
                    .stroke(Color("#654321"), width = 2.5)

                rectangle(p(50, 50), 60.0, 40.0)
                    .stroke(Color("#654321"), width = 2.5)
                    .fill(Color("#123456"), 0.7)
            }

        val c1 = assertIs<Circle>(s.elements[0])
        assertEquals(Color("#ff0000"), c1.fill?.color)
        assertNull(c1.fill?.opacity, "fill opacity should be null")
        assertNull(c1.stroke, "stroke should be null")

        val r2 = assertIs<Rectangle>(s.elements[1])
        assertEquals(Color("#00ff00"), r2.stroke?.color)
        assertNull(r2.stroke?.width, "stroke width should be null")
        assertNull(r2.stroke?.opacity, "stroke opacity should be null")
        assertEquals(ArrowMarker.None, r2.stroke?.arrowStart, "stroke arrowStart should be null")
        assertEquals(ArrowMarker.None, r2.stroke?.arrowEnd, "stroke arrowEnd should be null")
        assertEquals(false, r2.stroke?.dashed, "stroke should be not dashed by default")
        assertNull(r2.fill, "fill should be null")

        val p3 = assertIs<Polygon>(s.elements[2])
        assertEquals(Color("#aaaaaa"), p3.fill?.color)
        assertEquals(0.4, p3.fill?.opacity)
        assertNull(p3.stroke, "stroke не должен быть задан")

        val l4 = assertIs<Segment>(s.elements[3])
        assertEquals(Color("#0000ff"), l4.stroke?.color)
        assertEquals(3.0, l4.stroke?.width)

        val c5 = assertIs<Circle>(s.elements[4])
        assertEquals(Color("#123456"), c5.fill?.color)
        assertEquals(0.7, c5.fill?.opacity)
        assertEquals(Color("#654321"), c5.stroke?.color)
        assertEquals(2.5, c5.stroke?.width)

        val r6 = assertIs<Rectangle>(s.elements[5])
        assertEquals(Color("#123456"), r6.fill?.color)
        assertEquals(0.7, r6.fill?.opacity)
        assertEquals(Color("#654321"), r6.stroke?.color)
        assertEquals(2.5, r6.stroke?.width)
    }

    @Test
    @Order(103)
    fun `infix stroke syntax`() {
        val s =
            scene {
                circle(p(100, 100), 50.0) stroke color("#00ff00")
                rectangle(p(100, 100), 100.0, 50.0) stroke "#00ff00".color
                line(100 at 100, 200 at 100) stroke 0x00ff00.color
            }

        val elements = s.elements.filterIsInstance<Contourable>()
        assertEquals(3, elements.size)

        for (element in elements) {
            assertEquals(Color("#00ff00"), element.stroke?.color, "stroke should has valid color for $element")
            assertNull(element.stroke?.width, "stroke shouldn't has width for $element")
            assertNull(element.stroke?.opacity, "stroke shouldn't has opacity for $element")
            assertTrue(element.stroke?.dashed == false, "stroke shouldn't has dashed for $element")
        }
    }

    @Test
    @Order(104)
    fun `infix fill syntax`() {
        val s =
            scene {
                circle(p(100, 100), 50.0) fill color("#00ff00")
                rectangle(p(100, 100), 100.0, 50.0) fill "#00ff00".color
                polygon(100 at 100, 200 at 100, 200 at 200) fill 0x00ff00.color
            }

        val elements = s.elements.filterIsInstance<Fillable>()
        assertEquals(3, elements.size)

        for (element in elements) {
            assertEquals(Color("#00ff00"), element.fill?.color, "fill should has valid color for $element")
            assertNull(element.fill?.opacity, "fill shouldn't has opacity for $element")
            assertNull(element.stroke, "stroke shouldn't be for $element")
        }
    }

    @Test
    @Order(105)
    fun `stroke DASHED infix`() {
        val s =
            scene {
                circle(p(50, 50), 10.0) stroke DASHED
                polyline(p(10, 20), 30 at 40) stroke DASHED
            }

        val elements = s.elements.filterIsInstance<Contourable>()
        assertEquals(2, elements.size)

        for (element in elements) {
            val stroke = element.stroke
            assertNotNull(stroke, "stroke should be initialized for $element")
            assertTrue(stroke.dashed, "stroke should be dashed for $element")
            assertNull(stroke.color, "dashed stroke without color should have null color for $element")
            assertNull(stroke.width, "dashed stroke without width should have null width for $element")
        }
    }

    @Test
    @Order(106)
    fun `noStroke disables stroke`() {
        val s =
            scene {
                line(p(10, 10), 5.0 at 15).noStroke
                circle(p(10, 10), 5.0).fill("blue".color).noStroke
                val r = rectangle(p(20, 20), 10.0, 5.0) fill "blue".color
                r.noStroke
                rectangle(p(20, 20), 10.0, 5.0).noStroke fill "blue".color
            }

        val l1 = assertIs<Segment>(s.elements[0])
        assertEquals(NONE_STROKE, l1.stroke)

        val c2 = assertIs<Circle>(s.elements[1])
        assertEquals(NONE_STROKE, c2.stroke)
        assertEquals(Color("blue"), c2.fill?.color)

        val r3 = assertIs<Rectangle>(s.elements[2])
        assertEquals(NONE_STROKE, r3.stroke)
        assertEquals(Color("blue"), r3.fill?.color)

        val r4 = assertIs<Rectangle>(s.elements[3])
        assertEquals(NONE_STROKE, r4.stroke)
        assertEquals(Color("blue"), r4.fill?.color)
    }

    @Test
    @Order(111)
    fun `shape builders`() {
        val s =
            scene {
                circle(p(10, 10), 5.0) {
                    fill(Color("#ff0000"))
                }

                rectangle(p(20, 20), 10.0, 5.0) {
                    stroke(Color("#00ff00"))
                }

                polygon(0 at 0, 10 at 0, 5 at 10) {
                    fill(Color("#aaaaaa"), opacity = 0.4)
                }

                line(0 at 0, 10 at 10) {
                    stroke(Color("#0000ff"), width = 3.0)
                }

                polyline(100 at 0, 100 at 10, 90 at 10) {
                    fill(Color("#123456"), 0.7)
                    stroke(Color("#654321"), width = 2.5)
                }

                rectangle(p(50, 50), 60.0, 40.0)
                    .stroke(Color("#654321"), width = 2.5)
                    .fill(Color("#123456"), 0.7)
            }

        val elements = s.elements
        assertEquals(6, elements.size)

        assertIs<Circle>(elements[0]).let { c ->
            assertEquals(Color("#ff0000"), c.fill?.color)
            assertNull(c.fill?.opacity)
            assertNull(c.stroke)
        }

        assertIs<Rectangle>(elements[1]).let { r ->
            assertEquals(Color("#00ff00"), r.stroke?.color)
            assertNull(r.stroke?.width)
            assertNull(r.fill)
        }

        assertIs<Polygon>(elements[2]).let { p ->
            assertEquals(Color("#aaaaaa"), p.fill?.color)
            assertEquals(0.4, p.fill?.opacity)
            assertNull(p.stroke)
        }

        assertIs<Segment>(elements[3]).let { l ->
            assertEquals(Color("#0000ff"), l.stroke?.color)
            assertEquals(3.0, l.stroke?.width)
        }

        assertIs<Polyline>(elements[4]).let { pl ->
            assertEquals(Color("#123456"), pl.fill?.color, "$pl")
            assertEquals(0.7, pl.fill?.opacity)
            assertEquals(Color("#654321"), pl.stroke?.color)
            assertEquals(2.5, pl.stroke?.width)
        }

        assertIs<Rectangle>(elements[5]).let { r ->
            assertEquals(Color("#123456"), r.fill?.color)
            assertEquals(0.7, r.fill?.opacity)
            assertEquals(Color("#654321"), r.stroke?.color)
            assertEquals(2.5, r.stroke?.width)
        }
    }

    @Test
    @Order(112)
    fun `shape builders and infix`() {
        val s =
            scene {
                circle(p(10, 10), 5.0) {
                    fill(Color("#ff0000"))
                } stroke "#000000".color

                rectangle(p(20, 20), 10.0, 5.0) {
                    stroke(Color("#00ff00"))
                } fill color(0xFF0000)

                polygon(0 at 0, 10 at 0, 5 at 10) {
                    fill(Color("#aaaaaa"), opacity = 0.4)
                }.noStroke
            }

        assertEquals(3, s.elements.size)

        assertIs<Circle>(s.elements[0]).let { c ->
            assertEquals(Color("#ff0000"), c.fill?.color)
            assertEquals(Color("#000000"), c.stroke?.color)
        }

        assertIs<Rectangle>(s.elements[1]).let { r ->
            assertEquals(Color("#00ff00"), r.stroke?.color)
            assertEquals(Color("#ff0000"), r.fill?.color)
        }

        assertIs<Polygon>(s.elements[2]).let { p ->
            assertEquals(Color("#aaaaaa"), p.fill?.color)
            assertEquals(0.4, p.fill?.opacity)
            assertEquals(NONE_STROKE, p.stroke)
        }
    }

    @Test
    @Order(121)
    fun `polyline builder adds points`() {
        val s =
            scene {
                polyline {
                    point(0 at 0)
                    point(10 at 10)
                    point(20 at 0)
                    stroke(color = Color("#111111"))
                }
            }

        val curve = s.elements.single() as Polyline
        assertEquals(3, curve.points.size)
        assertEquals(
            listOf(Point(0.0, 0.0), Point(10.0, 10.0), Point(20.0, 0.0)),
            curve.points,
        )
        assertEquals(Color("#111111"), curve.stroke?.color)
    }

    @Test
    fun `polyline builder with functions`() {
        val s =
            scene {
                polyline {
                    point(0 at 0)
                    point(10 at 10)
                    point(20 at 0)
                } fill "blue".color stroke "red".color
            }

        val curve = s.elements.single() as Polyline
        assertEquals(3, curve.points.size)
        assertEquals(
            listOf(Point(0.0, 0.0), Point(10.0, 10.0), Point(20.0, 0.0)),
            curve.points,
        )
        assertEquals(Color("red"), curve.stroke?.color)
        assertEquals(Color("blue"), curve.fill?.color)
    }
}
