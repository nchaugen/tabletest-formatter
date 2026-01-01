package example;

import io.github.nchaugen.tabletest.TableTest;

class VariedWhitespaceTest {

    @TableTest  (  """
    name|age
    Alice|30
    """  )
    void shouldHandleExtraSpaces(String name, int age) {
        // test implementation
    }

    @TableTest(
        """
        city|country
        London|UK
        Paris|France
        """
    )
    void shouldHandleQuotesOnSeparateLine(String city, String country) {
        // test implementation
    }
}
