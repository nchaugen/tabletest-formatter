package example;

import io.github.nchaugen.tabletest.TableTest;

class OuterClass {

    class MiddleClass {

        class InnerClass {

            @TableTest("""
            name|age
            Alice|30
            Bob|25
            """)
            void shouldHandleDeepNesting(String name, int age) {
                // test implementation
            }
        }
    }
}
