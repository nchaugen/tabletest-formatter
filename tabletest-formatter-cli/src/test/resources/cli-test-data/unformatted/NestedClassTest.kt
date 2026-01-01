package example

import io.github.nchaugen.tabletest.TableTest

class OuterKotlinClass {

    class InnerKotlinClass {

        @TableTest("""
        x|y|result
        1|2|3
        5|3|8
        """)
        fun shouldHandleNestedKotlinClass(x: Int, y: Int, result: Int) {
            // test implementation
        }

        class DeeplyNestedClass {

            @TableTest("""
            operation|input|output
            double|5|10
            triple|3|9
            """)
            fun shouldHandleDeeplyNestedKotlinClass(operation: String, input: Int, output: Int) {
                // test implementation
            }
        }
    }
}
