package io.github.nchaugen.tabletest.formatter.spotless;

import com.diffplug.spotless.FormatterStep;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableTestFormatterStepTest {

    @Test
    void shouldCreateValidFormatterStep() {
        FormatterStep step = TableTestFormatterStep.create();

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("tabletest");
    }
}
