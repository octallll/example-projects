package ru.itmo.ct.geosvg.dsl

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IncorrectContextTests {
    @Test
    fun testShapeInShape() {
        assertCompilationFails(
            """
            |import ru.itmo.ct.geosvg.dsl.*
            |
            |fun main() {
            |    scene {
            |        rectangle(1 at 1, 20.0, 10.0) {
            |            fill(0x0011AA.color)
            |            rectangle(1 at 1, 20.0, 10.0) {
            |            }
            |        }
            |    }
            |}
            """.trimMargin(),
        )
    }

    @Test
    fun testShapeInText() {
        assertCompilationFails(
            """
            |import ru.itmo.ct.geosvg.dsl.*
            |
            |fun main() {
            |    scene {
            |        text("this code sholudn't compiled", 34 at 12) {
            |            fill(0x0011AA.color)
            |            rectangle(1 at 1, 20.0, 10.0) {
            |            }
            |        }
            |    }
            |}
            """.trimMargin(),
        )
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun assertCompilationFails(
        @Language(value = "kotlin") source: String,
    ) {
        val result =
            KotlinCompilation()
                .apply {
                    jvmTarget = "21"
                    sources = listOf(SourceFile.kotlin("BadCode.kt", source))
                    inheritClassPath = true
                }.compile()

        Assertions.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    }
}
