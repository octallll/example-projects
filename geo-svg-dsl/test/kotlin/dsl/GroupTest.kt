package ru.itmo.ct.geosvg.dsl

import kotlin.test.assertIs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestMethodOrder
import ru.itmo.ct.geosvg.model.Circle
import ru.itmo.ct.geosvg.model.Group
import ru.itmo.ct.geosvg.model.Point
import ru.itmo.ct.geosvg.model.Rectangle

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class GroupTest {
    @Test
    @Order(401)
    fun `group with nested shapes`() {
        val s =
            scene {
                circle(p(0, 0), 10.0)
                group(100 at 200) {
                    circle(p(0, 0), 5.0)
                    rectangle(p(10, 10), 5.0, 7.0)
                }
            }

        assertEquals(2, s.elements.size)
        val g = assertIs<Group>(s.elements[1])
        assertEquals(Point(100.0, 200.0), g.offset)
        assertEquals(2, g.elements.size)
        assertEquals(Circle(0.0 at 0.0, 5.0), g.elements[0])
        assertEquals(Rectangle(10.0 at 10.0, 5.0, height = 7.0), g.elements[1])
    }
}
