package br.edu.lms.module.reporting.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DashboardPeriodTest {

    @Test
    void constructor_fromAfterTo_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new DashboardPeriod(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_fromEqualsTo_isValid() {
        var date = LocalDate.of(2026, 1, 15);
        var period = new DashboardPeriod(date, date);

        assertThat(period.getFrom()).isEqualTo(date);
        assertThat(period.getTo()).isEqualTo(date);
    }

    @Test
    void endExclusive_isStartOfDayAfterTo() {
        var period = new DashboardPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(period.endExclusive()).isEqualTo(LocalDate.of(2026, 2, 1).atStartOfDay());
        assertThat(period.startInclusive()).isEqualTo(LocalDate.of(2026, 1, 1).atStartOfDay());
    }
}
