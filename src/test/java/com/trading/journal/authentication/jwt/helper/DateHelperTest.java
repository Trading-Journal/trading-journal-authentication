package com.trading.journal.authentication.jwt.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateHelperTest {

    @DisplayName("Date UTC")
    @Test
    void dateUtc() {
        Date date = DateHelper.getUTCDatetimeAsDate();
        assertThat(date).isBefore(new Date());
    }

    @DisplayName("String Date time UTC")
    @Test
    void dateTimeUtc() {
        String datetime = DateHelper.getUTCDatetimeAsString();
        assertThat(datetime).isNotBlank();
    }

    @DisplayName("String To Date")
    @Test
    void stringToDate() {
        String value = "2022-02-03 23:25:21";
        Date date = DateHelper.stringDateToDate(value);
        assertThat(date).isBefore(new Date());
    }

    @DisplayName("String To Date invalid format return exception")
    @Test
    void stringToDateEx() {
        String value = "xx-02-2022 23:25:21";
        assertThrows(DateTimeException.class, () -> DateHelper.stringDateToDate(value), "Invalid date");
    }
}