package ru.itmo.ct.geosvg.dsl

import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.itmo.ct.geosvg.model.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TextTest {
    @Test
    @Order(301)
    fun `text builder and alignment`() {
        val s =
            scene {
                text("Hello", 10 at 10) {
                    size(20.0)
                    bold()
                    centered(vertical = true)
                    fill(Color("#0000ee"))
                }
            }

        val t = assertIs<Text>(s.elements.single())
        assertEquals(Point(10.0, 10.0), t.point)
        assertEquals("Hello", t.text)
        assertEquals(20.0, t.fontSize)
        assertTrue(t.fontBold)
        assertEquals(Alignment.MIDDLE, t.textAnchor)
        assertEquals(Alignment.MIDDLE, t.dominantBaseline)
        assertEquals(Color("#0000ee"), t.fill?.color)
    }

    @Test
    @Order(302)
    fun `text infix fill and size`() {
        val s =
            scene {
                text("A", 0 at 0) fill Color("#00ff00") size 23.0
            }
        val t = assertIs<Text>(s.elements.single())
        assertEquals(Point(0.0, 0.0), t.point)
        assertEquals(Color("#00ff00"), t.fill?.color)
        assertEquals(23.0, t.fontSize)
    }

    @Test
    @Order(303)
    fun `text extension property`() {
        val s =
            scene {
                text("A", 0 at 0) {
                    centeredVertical()
                }.bold size 23.0

                text("A", 200 at 100).alignBottom.alignRight size 17.0
            }
        assertEquals(2, s.elements.size)

        assertIs<Text>(s.elements[0]).let { t ->
            assertEquals(Point(0.0, 0.0), t.point)
            assertEquals(23.0, t.fontSize)
            assertEquals(Alignment.START, t.textAnchor)
            assertEquals(Alignment.MIDDLE, t.dominantBaseline)
            assertTrue(t.fontBold)
            assertEquals(Colors.BLACK, t.fill?.color) { "default fill should be black" }
        }

        assertIs<Text>(s.elements[1]).let { t ->
            assertEquals(Point(200.0, 100.0), t.point)
            assertEquals(Alignment.END, t.textAnchor)
            assertEquals(Alignment.END, t.dominantBaseline)
            assertFalse(t.fontBold)
            assertEquals(17.0, t.fontSize)
            assertEquals(Colors.BLACK, t.fill?.color) { "default fill should be black" }
        }
    }
}
