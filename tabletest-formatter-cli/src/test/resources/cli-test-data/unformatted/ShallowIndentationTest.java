package example;

import io.github.nchaugen.tabletest.TableTest;

class ShallowIndentTest {

  @TableTest("""
  name|age
  Alice|30
  Bob|25
  """)
  void shouldHandleShallowIndentation(String name, int age) {
    // test implementation
  }
}
