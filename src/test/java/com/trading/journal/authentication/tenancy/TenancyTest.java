package com.trading.journal.authentication.tenancy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenancyTest {

    @DisplayName("Lower usage to Zero, if equal to Zero do not lower below it")
    @Test
    void lowerZero() {
        Tenancy tenancy = Tenancy.builder().userUsage(2).build();
        tenancy.lowerUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(1);
        tenancy.lowerUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(0);
        tenancy.lowerUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(0);
    }

    @DisplayName("Increase usage to Two")
    @Test
    void increase() {
        Tenancy tenancy = Tenancy.builder().userLimit(2).userUsage(0).build();
        tenancy.increaseUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(1);
        tenancy.increaseUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(2);
        tenancy.increaseUsage();
        assertThat(tenancy.getUserUsage()).isEqualTo(2);
    }

    @DisplayName("Increase usage is allowed because limit is far from usage")
    @Test
    void increaseAllowed() {
        Tenancy tenancy = Tenancy.builder().userLimit(10).userUsage(2).build();
        assertThat(tenancy.increaseUsageAllowed()).isTrue();
        tenancy.increaseUsage();
        tenancy.increaseUsage();
        assertThat(tenancy.increaseUsageAllowed()).isTrue();
        assertThat(tenancy.getUserUsage()).isEqualTo(4);
    }

    @DisplayName("Increase usage is allowed for one more user")
    @Test
    void increaseAllowedOne() {
        Tenancy tenancy = Tenancy.builder().userLimit(3).userUsage(2).build();
        assertThat(tenancy.increaseUsageAllowed()).isTrue();
    }

    @DisplayName("Increase usage is not allowed")
    @Test
    void increaseNotAllowed() {
        Tenancy tenancy = Tenancy.builder().userLimit(3).userUsage(3).build();
        assertThat(tenancy.increaseUsageAllowed()).isFalse();
    }
}