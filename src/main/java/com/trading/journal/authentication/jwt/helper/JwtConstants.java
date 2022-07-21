package com.trading.journal.authentication.jwt.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtConstants {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String SCOPES = "scopes";
    public static final String TENANCY_ID = "tenancy";

    public static final String TENANCY_NAME = "cia";
    public static final String HEADER_TYP = "typ";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    public static final String TEMPORARY_TOKEN = "TEMPORARY_TOKEN";
}
