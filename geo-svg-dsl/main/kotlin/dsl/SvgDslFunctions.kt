package ru.itmo.ct.geosvg.dsl

import ru.itmo.ct.geosvg.model.Alignment
import ru.itmo.ct.geosvg.model.ArrowMarker
import ru.itmo.ct.geosvg.model.Color
import ru.itmo.ct.geosvg.model.Contourable
import ru.itmo.ct.geosvg.model.Fill
import ru.itmo.ct.geosvg.model.Fillable
import ru.itmo.ct.geosvg.model.NONE_STROKE
import ru.itmo.ct.geosvg.model.Point
import ru.itmo.ct.geosvg.model.Stroke
import ru.itmo.ct.geosvg.model.Text

fun p(
    x: Number,
    y: Number,
): Point = Point(x.toDouble(), y.toDouble())

infix fun Number.at(y: Number): Point = Point(this.toDouble(), y.toDouble())

operator fun Point.plus(p: Point): Point = Point(this.x + p.x, this.y + p.y)

fun color(color: String): Color = Color(color)

fun color(color: Int): Color = Color(color)

val String.color: Color
    get() = Color(this)

val Int.color: Color
    get() = Color(this)

fun <T : Contourable> T.stroke(stroke: Stroke): T {
    this.stroke = stroke
    return this
}

fun <T : Contourable> T.stroke(
    color: Color? = null,
    width: Double? = null,
    opacity: Double? = null,
    dashed: Boolean = false,
): T {
    this.stroke = Stroke(color, width, opacity, ArrowMarker.None, ArrowMarker.None, dashed)
    return this
}

fun <T : Fillable> T.fill(color: Fill): T {
    this.fill = color
    return this
}

fun <T : Fillable> T.fill(
    color: Color,
    opacity: Double? = null,
): T {
    this.fill = Fill(color, opacity)
    return this
}

infix fun <T : Contourable> T.stroke(color: Color): T {
    this.stroke = this.stroke?.copy(color = color) ?: Stroke(color = color)
    return this
}

infix fun <T : Fillable> T.fill(color: Color): T {
    this.fill = this.fill?.copy(color = color) ?: Fill(color = color)
    return this
}

object DASHED

infix fun <T : Contourable> T.stroke(dashed: DASHED): T {
    this.stroke = this.stroke?.copy(dashed = true) ?: Stroke(dashed = true)
    return this
}

val <T : Contourable> T.noStroke: T
    get() {
        this.stroke = NONE_STROKE
        return this
    }

fun Text.bold(): Text {
    this.fontBold = true
    return this
}

fun Text.anchor(a: Alignment): Text {
    this.textAnchor = a
    return this
}

fun Text.baseline(a: Alignment): Text {
    this.dominantBaseline = a
    return this
}

fun Text.centered(vertical: Boolean = false): Text {
    this.textAnchor = Alignment.MIDDLE

    if (vertical) {
        this.dominantBaseline = Alignment.MIDDLE
    }

    return this
}

fun Text.centeredVertical(): Text {
    this.dominantBaseline = Alignment.MIDDLE
    return this
}

fun Text.fill(
    color: Color,
    opacity: Double = 1.0,
): Text {
    this.fill = Fill(color, opacity)
    return this
}

infix fun Text.fill(color: Color): Text {
    this.fill = this.fill?.copy(color = color) ?: Fill(color)
    return this
}

infix fun Text.size(fontSize: Double): Text {
    this.fontSize = fontSize
    return this
}

val Text.bold: Text
    get() {
        this.fontBold = true
        return this
    }

val Text.alignLeft: Text
    get() {
        this.textAnchor = Alignment.START
        return this
    }

val Text.alignCenter: Text
    get() {
        this.textAnchor = Alignment.MIDDLE
        return this
    }

val Text.alignRight: Text
    get() {
        this.textAnchor = Alignment.END
        return this
    }

val Text.alignTop: Text
    get() {
        this.dominantBaseline = Alignment.START
        return this
    }

val Text.alignMiddle: Text
    get() {
        this.dominantBaseline = Alignment.MIDDLE
        return this
    }

val Text.alignBottom: Text
    get() {
        this.dominantBaseline = Alignment.END
        return this
    }
