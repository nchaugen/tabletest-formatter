package example;

import org.tabletest.TableTest;

class NamedParameterTest {

    @TableTest(value = """
    name|age
    Alice|30
    Bob|25
    """)
    void shouldHandleNamedParameter(String name, int age) {
        // test implementation
    }

    @TableTest(value="""
    product|price
    Book|15
    Pen|2
    """)
    void shouldHandleNamedParameterWithoutSpaces(String product, int price) {
        // test implementation
    }
}
