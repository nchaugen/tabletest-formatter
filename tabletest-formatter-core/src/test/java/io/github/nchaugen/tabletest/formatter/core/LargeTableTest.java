package io.github.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.formatter.config.StaticConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stress tests for large tables to verify performance and memory usage.
 */
class LargeTableTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @Test
    void shouldFormatTableWith1000Rows() {
        // Generate table with 1000 rows
        String header = "id|name|age\n";
        String rows = IntStream.range(1, 1001)
                .mapToObj(i -> i + "|Person" + i + "|" + (20 + i % 50))
                .collect(Collectors.joining("\n"));
        String input = header + rows + "\n";

        long startTime = System.currentTimeMillis();
        Objects.requireNonNull(input, "tableText must not be null");
        String result = formatter.format(input, "", StaticConfigProvider.NO_INDENT);
        long duration = System.currentTimeMillis() - startTime;

        // Verify formatting completed successfully
        assertThat(result).isNotNull();
        assertThat(result).contains("id");
        assertThat(result).contains("Person1");
        assertThat(result).contains("Person1000");

        // Verify all rows are present (1000 data rows + 1 header)
        assertThat(result.split("\n")).hasSize(1001);

        // Performance check: should complete in reasonable time (< 5 seconds)
        assertThat(duration).as("Formatting 1000 rows should complete quickly").isLessThan(5000);
    }

    @Test
    void shouldFormatTableWith50Columns() {
        // Generate table with 50 columns
        String header = IntStream.range(1, 51).mapToObj(i -> "col" + i).collect(Collectors.joining("|"));
        String row1 = IntStream.range(1, 51).mapToObj(i -> "value" + i).collect(Collectors.joining("|"));
        String row2 = IntStream.range(1, 51).mapToObj(i -> "data" + i).collect(Collectors.joining("|"));
        String input = header + "\n" + row1 + "\n" + row2 + "\n";

        long startTime = System.currentTimeMillis();
        Objects.requireNonNull(input, "tableText must not be null");
        String result = formatter.format(input, "", StaticConfigProvider.NO_INDENT);
        long duration = System.currentTimeMillis() - startTime;

        // Verify formatting completed successfully
        assertThat(result).isNotNull();
        assertThat(result).contains("col1");
        assertThat(result).contains("col50");
        assertThat(result).contains("value1");
        assertThat(result).contains("data50");

        // Verify proper column separation with pipes
        String[] lines = result.split("\n");
        assertThat(lines).hasSize(3);
        assertThat(lines[0].split("\\|")).hasSize(50);

        // Performance check: should complete quickly (< 2 seconds)
        assertThat(duration).as("Formatting 50 columns should complete quickly").isLessThan(2000);
    }

    @Test
    void shouldFormatTableWithVeryWideCells() {
        // Generate table with very wide cells (1000+ characters)
        String longText1 = "A".repeat(1000);
        String longText2 = "B".repeat(1500);
        String longText3 = "C".repeat(2000);

        String input = "name|description\n"
                + "Item1|" + longText1 + "\n"
                + "Item2|" + longText2 + "\n"
                + "Item3|" + longText3 + "\n";

        long startTime = System.currentTimeMillis();
        Objects.requireNonNull(input, "tableText must not be null");
        String result = formatter.format(input, "", StaticConfigProvider.NO_INDENT);
        long duration = System.currentTimeMillis() - startTime;

        // Verify formatting completed successfully
        assertThat(result).isNotNull();
        assertThat(result).contains("Item1");
        assertThat(result).contains("Item2");
        assertThat(result).contains("Item3");

        // Verify the long text is preserved
        assertThat(result).contains(longText1);
        assertThat(result).contains(longText2);
        assertThat(result).contains(longText3);

        // Verify table structure is maintained
        assertThat(result.split("\n")).hasSize(4);

        // Performance check: should handle wide cells efficiently (< 1 second)
        assertThat(duration)
                .as("Formatting very wide cells should complete quickly")
                .isLessThan(1000);
    }

    @Test
    void shouldHandleCombinationOfLargeRowsAndColumns() {
        // Generate table with 100 rows and 20 columns
        String header = IntStream.range(1, 21).mapToObj(i -> "column" + i).collect(Collectors.joining("|"));

        String rows = IntStream.range(1, 101)
                .mapToObj(row -> IntStream.range(1, 21)
                        .mapToObj(col -> "R" + row + "C" + col)
                        .collect(Collectors.joining("|")))
                .collect(Collectors.joining("\n"));

        String input = header + "\n" + rows + "\n";

        long startTime = System.currentTimeMillis();
        Objects.requireNonNull(input, "tableText must not be null");
        String result = formatter.format(input, "", StaticConfigProvider.NO_INDENT);
        long duration = System.currentTimeMillis() - startTime;

        // Verify formatting completed successfully
        assertThat(result).isNotNull();
        assertThat(result).contains("column1");
        assertThat(result).contains("column20");
        assertThat(result).contains("R1C1");
        assertThat(result).contains("R100C20");

        // Verify all rows are present (100 data rows + 1 header)
        assertThat(result.split("\n")).hasSize(101);

        // Performance check: should complete in reasonable time (< 3 seconds)
        assertThat(duration)
                .as("Formatting 100 rows x 20 columns should complete quickly")
                .isLessThan(3000);
    }

    @Test
    void shouldHandleTableWithMixedCellWidths() {
        // Test a table where cell widths vary significantly
        String input = """
            short|medium|very_long_column_name
            a|bb|ccc
            dddd|e|ff
            ggggggg|hh|i
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        String result = formatter.format(input, "", StaticConfigProvider.NO_INDENT);

        // Verify formatting is correct
        assertThat(result).isEqualTo("""
                short   | medium | very_long_column_name
                a       | bb     | ccc
                dddd    | e      | ff
                ggggggg | hh     | i
                """);
    }
}
