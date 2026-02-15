package example;

import org.tabletest.TableTest;

class TabIndentedTest {

	@TableTest("""
	name|age
	Alice|30
	Bob|25
	""")
	void shouldHandleTabIndentation(String name, int age) {
		// test implementation
	}
}
