package org.tabletest.formatter.core;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.util.JavacTask;
import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;

import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConcatenatedRowsReproducerTest {

    @Test
    void concatenatedStringsRemainInTheirOriginalRows() throws Exception {
        String input = """
                @TableTest({"a | " + "b", "1 | " + "2"})
                class ConcatenatedRows {}
                """;
        SourceFileFormatter formatter = new SourceFileFormatter();

        String output = formatter.format(input, Config.SPACES_4);

        assertThat(rowValues(input)).containsExactly("a | b", "1 | 2");
        assertThat(output).isEqualTo(input);
        assertThat(formatter.format(output, Config.SPACES_4)).isEqualTo(output);
        assertThat(rowValues(output)).containsExactly("a | b", "1 | 2");
    }

    private static List<String> rowValues(String source) throws Exception {
        var file =
                new SimpleJavaFileObject(
                        URI.create("string:///ConcatenatedRows.java"), SimpleJavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return source;
                    }
                };
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var files = compiler.getStandardFileManager(null, null, null)) {
            var parser = (JavacTask) compiler.getTask(null, files, null, List.of("-proc:none"), null, List.of(file));
            var unit = parser.parse().iterator().next();
            var type = (ClassTree) unit.getTypeDecls().get(0);
            var rows = (NewArrayTree)
                    type.getModifiers().getAnnotations().get(0).getArguments().get(0);
            // javac folds constant string concatenations, preserving the annotation's actual row values.
            return rows.getInitializers().stream()
                    .map(row -> (String) ((LiteralTree) row).getValue())
                    .toList();
        }
    }
}
