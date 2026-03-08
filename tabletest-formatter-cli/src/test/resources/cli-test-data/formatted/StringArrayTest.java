package example;

import org.tabletest.TableTest;

class StringArrayTest {

    @TableTest({
        "name  | age",
        "Alice | 30 ",
        "Bob   | 7  "
    })
    void shouldHandleInlineStringArray(String name, int age) {
        // test implementation
    }

    @TableTest(value = {
        "product | price",
        "Book    | 15   ",
        "Pen     | 2    "
    })
    void shouldHandleNamedStringArray(String product, int price) {
        // test implementation
    }

    @TableTest({
        "city   | country",
        "London | UK     ",
        "Paris  | France "
    })
    void shouldHandleMultiLineStringArray(String city, String country) {
        // test implementation
    }
}
