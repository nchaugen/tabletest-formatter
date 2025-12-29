package example;

import io.github.nchaugen.tabletest.TableTest;

class SimpleTest {

    @TableTest("""
name  | age | city
Alice | 30  | New York
Bob   | 25  | Los Angeles
    """)
    void shouldProcessUserData(String name, int age, String city) {
        // test implementation
    }
}
