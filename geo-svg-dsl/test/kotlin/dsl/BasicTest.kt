package ru.itmo.ct.geosvg.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestMethodOrder
import ru.itmo.ct.geosvg.model.Color
import ru.itmo.ct.geosvg.model.Point

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Order(1)
class BasicTest {
    @Test
    @Order(1)
    fun `point creation and operations`() {
        val p1 = p(1, 2)
        val p2 = 3 at 4
        val shiftedPoint = p1 + p2
        assertEquals(Point(1.0, 2.0), p1)
        assertEquals(Point(3.0, 4.0), p2)
        assertEquals(Point(4.0, 6.0), shiftedPoint)
    }

    @Test
    @Order(2)
    fun `color creation and extensions`() {
        assertEquals(Color("#00aa66"), color(0x00AA66))
        assertEquals(Color("#00aa66"), "#00aa66".color)
        assertEquals(Color("#00aa66"), 0x00AA66.color)
    }
}
