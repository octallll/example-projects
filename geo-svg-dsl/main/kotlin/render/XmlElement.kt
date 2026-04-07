package ru.itmo.ct.geosvg.render

sealed interface XmlElement

data class XmlTag(
    val name: String,
    val attrs: MutableMap<String, String> = mutableMapOf(),
    val children: MutableList<XmlElement> = mutableListOf(),
) : XmlElement

data class XmlText(
    val text: String,
) : XmlElement

class XmlRenderer {
    fun render(element: XmlElement): String {
        val builder = StringBuilder()
        renderElement(element, builder, 0)
        return builder.toString()
    }

    fun renderMinified(element: XmlElement): String {
        val builder = StringBuilder()
        renderElement(element, builder, -1)
        return builder.toString()
    }

    private fun renderElement(
        element: XmlElement,
        builder: StringBuilder,
        indent: Int,
    ) {
        when (element) {
            is XmlTag -> renderTag(element, builder, indent)
            is XmlText -> renderText(element, builder, indent)
        }
    }

    private fun renderTag(
        tag: XmlTag,
        builder: StringBuilder,
        indent: Int,
    ) {
        val hasChildren = tag.children.isNotEmpty()
        val isInline = !hasChildren || indent < 0

        appendIndent(builder, indent)

        builder.append("<").append(tag.name)
        renderAttributes(tag.attrs, builder)

        if (!hasChildren) {
            builder.append("/>")
            if (indent >= 0) builder.appendLine()
            return
        }

        builder.append(">")

        if (!isInline) {
            builder.appendLine()
        }

        val childIndent = if (indent >= 0) indent + 1 else -1
        for (child in tag.children) {
            renderElement(child, builder, childIndent)
        }

        if (!isInline) {
            appendIndent(builder, indent)
        }
        builder.append("</").append(tag.name).append(">")
        if (indent >= 0) {
            builder.appendLine()
        }
    }

    private fun renderText(
        text: XmlText,
        builder: StringBuilder,
        indent: Int,
    ) {
        appendIndent(builder, indent)
        builder.append(escapeXml(text.text))
        if (indent >= 0) builder.appendLine()
    }

    private fun renderAttributes(
        attrs: Map<String, String>,
        builder: StringBuilder,
    ) {
        for ((key, value) in attrs) {
            builder.append(" ").append(key).append("=\"")
            builder.append(escapeXmlAttribute(value))
            builder.append("\"")
        }
    }

    private fun appendIndent(
        builder: StringBuilder,
        indent: Int,
    ) {
        if (indent >= 0) {
            for (i in 0 until indent) {
                builder.append("  ")
            }
        }
    }

    private fun escapeXml(text: String): String =
        buildString {
            for (char in text) {
                when (char) {
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '&' -> append("&amp;")
                    else -> append(char)
                }
            }
        }

    private fun escapeXmlAttribute(value: String): String =
        buildString {
            for (char in value) {
                when (char) {
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '&' -> append("&amp;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(char)
                }
            }
        }
}

fun xmlTag(
    name: String,
    block: XmlTag.() -> Unit = {},
): XmlTag = XmlTag(name).apply(block)

fun XmlTag.attr(
    key: String,
    value: String,
) {
    attrs[key] = value
}

fun XmlTag.attr(
    key: String,
    value: Number,
) {
    attrs[key] = "$value"
}

fun XmlTag.text(content: String) {
    children.add(XmlText(content))
}

fun XmlTag.tag(child: XmlTag) {
    children.add(child)
}
