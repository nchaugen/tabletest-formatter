package com.example;

import io.github.nchaugen.tabletest.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTest {

    @TableTest("""
        a|b|sum
        1|2|3
        5|3|8
        10|20|30
        """)
    void testAddition(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest(value = """
        x|y|product
        2|3|6
        4|5|20
        7|8|56
        """)
    void testMultiplication(int x, int y, int product) {
        assertEquals(product, x * y);
    }

    @TableTest(resource = "data.csv", value = """
        name|age|valid
        Alice|30|true
        Bob|17|false
        Charlie|25|true
        """, encoding = "UTF-8")
    void testAgeValidation(String name, int age, boolean valid) {
        assertEquals(valid, age >= 18);
    }
}
