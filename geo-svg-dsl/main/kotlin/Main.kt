package ru.itmo.ct.geosvg

import java.io.File
import ru.itmo.ct.geosvg.dsl.*
import ru.itmo.ct.geosvg.model.Colors
import ru.itmo.ct.geosvg.model.Colors.BLACK
import ru.itmo.ct.geosvg.model.Colors.BLUE
import ru.itmo.ct.geosvg.model.Colors.MAGENTA
import ru.itmo.ct.geosvg.model.Colors.YELLOW
import ru.itmo.ct.geosvg.render.SvgRenderer

fun main() {
    val renderer = SvgRenderer()
    val s =
        scene {
            rectangle(1 at 1, 20.0, 10.0) {
                fill(0x0011AA.color)
                // rectangle(1 at 1, 20.0, 10.0) {
                // }
            }
        }

    renderer.render(s)

    File("out.svg").writeText(renderer.render(s))
}
