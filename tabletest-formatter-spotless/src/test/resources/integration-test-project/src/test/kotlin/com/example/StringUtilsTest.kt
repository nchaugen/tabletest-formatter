package com.example

import io.github.nchaugen.tabletest.TableTest
import org.junit.jupiter.api.Assertions.assertEquals

class StringUtilsTest {

    @TableTest("""
        input|expected
        hello|HELLO
        world|WORLD
        test|TEST
        """)
    fun testUpperCase(input: String, expected: String) {
        assertEquals(expected, input.uppercase())
    }

    @TableTest(value = """
        text|reversed
        abc|cba
        hello|olleh
        kotlin|niltok
        """)
    fun testReverse(text: String, reversed: String) {
        assertEquals(reversed, text.reversed())
    }
}
