package com.trading.journal.authentication.jwt.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConstantsTest {

    @DisplayName("Check constants values")
    @Test
    void check() {
        assertThat(JwtConstants.TOKEN_PREFIX).isEqualTo("Bearer ");
        assertThat(JwtConstants.TOKEN_TYPE).isEqualTo("JWT");
        assertThat(JwtConstants.SCOPES).isEqualTo("scopes");
        assertThat(JwtConstants.TENANCY_ID).isEqualTo("tenancy");
        assertThat(JwtConstants.HEADER_TYP).isEqualTo("typ");
        assertThat(JwtConstants.REFRESH_TOKEN).isEqualTo("REFRESH_TOKEN");
        assertThat(JwtConstants.TEMPORARY_TOKEN).isEqualTo("TEMPORARY_TOKEN");
    }
}