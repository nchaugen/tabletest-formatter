package io.github.nchaugen.tabletest.formatter.spotless;

import com.diffplug.spotless.FormatterStep;
import io.github.nchaugen.tabletest.formatter.config.IndentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableTestFormatterStepTest {

    @Test
    void shouldCreateValidFormatterStep() {
        FormatterStep step = TableTestFormatterStep.create();

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("tabletest");
    }

    @Test
    void shouldCreateFormatterStepWithCustomIndentSize() {
        FormatterStep step = TableTestFormatterStep.create(2);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("tabletest");
    }

    @Test
    void shouldCreateFormatterStepWithCustomIndentTypeAndIndentSize() {
        FormatterStep step = TableTestFormatterStep.create(IndentType.TAB, 4);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("tabletest");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNegativeIndentSize() {
        assertThatThrownBy(() -> TableTestFormatterStep.create(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indentSize must not be negative");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNullIndentType() {
        assertThatThrownBy(() -> TableTestFormatterStep.create(null, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indentType must not be null");
    }
}
