package io.github.nchaugen.tabletest.formatter.spotless;

import com.diffplug.spotless.FormatterStep;
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
    void shouldCreateFormatterStepWithCustomTabSize() {
        FormatterStep step = TableTestFormatterStep.create(2);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("tabletest");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidTabSize() {
        assertThatThrownBy(() -> TableTestFormatterStep.create(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tabSize must be at least 1");
    }
}
