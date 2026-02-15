package example

import org.tabletest.TableTest

class MultiTableTest {

    @TableTest("""
    x|y|sum
    1|2|3
    10|20|30
    """)
    fun shouldAddNumbers(x: Int, y: Int, sum: Int) {
        // test implementation
    }

    @TableTest("""
    input|expected|description
    foo|FOO|lowercase to uppercase
    BAR|BAR|already uppercase
    """)
    fun shouldConvertCase(input: String, expected: String, description: String) {
        // test implementation
    }
}
