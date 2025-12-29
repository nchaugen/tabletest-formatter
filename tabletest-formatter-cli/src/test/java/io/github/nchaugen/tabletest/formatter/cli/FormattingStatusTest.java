package io.github.nchaugen.tabletest.formatter.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FormattingStatusTest {

    @Test
    void shouldAggregateSuccessfulResults() {
        FormattingStatus status = new FormattingStatus();

        Path file1 = Path.of("test1.table");
        Path file2 = Path.of("test2.java");
        Path file3 = Path.of("test3.kt");

        status.addResult(new FormattingResult(file1, true, "formatted1"));
        status.addResult(new FormattingResult(file2, false, "unchanged"));
        status.addResult(new FormattingResult(file3, true, "formatted2"));

        assertThat(status.filesChecked()).isEqualTo(3);
        assertThat(status.filesChanged()).isEqualTo(2);
        assertThat(status.hasChanges()).isTrue();
    }

    @Test
    void shouldTrackChangedFiles() {
        FormattingStatus status = new FormattingStatus();

        Path changed1 = Path.of("changed1.table");
        Path unchanged = Path.of("unchanged.java");
        Path changed2 = Path.of("changed2.kt");

        status.addResult(new FormattingResult(changed1, true, "formatted1"));
        status.addResult(new FormattingResult(unchanged, false, "unchanged"));
        status.addResult(new FormattingResult(changed2, true, "formatted2"));

        assertThat(status.changedFiles()).hasSize(2).containsExactly(changed1, changed2);
    }

    @Test
    void shouldTrackNoChanges() {
        FormattingStatus status = new FormattingStatus();

        Path file1 = Path.of("test1.table");
        Path file2 = Path.of("test2.java");

        status.addResult(new FormattingResult(file1, false, "unchanged1"));
        status.addResult(new FormattingResult(file2, false, "unchanged2"));

        assertThat(status.filesChecked()).isEqualTo(2);
        assertThat(status.filesChanged()).isEqualTo(0);
        assertThat(status.hasChanges()).isFalse();
        assertThat(status.changedFiles()).isEmpty();
    }
}
