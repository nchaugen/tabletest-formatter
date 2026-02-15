package example;

import org.tabletest.TableTest;

class ShallowIndentTest {

  @TableTest("""
      name  | age
      Alice | 30
      Bob   | 25
      """)
  void shouldHandleShallowIndentation(String name, int age) {
    // test implementation
  }
}
